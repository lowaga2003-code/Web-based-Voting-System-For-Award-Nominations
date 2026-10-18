package com.pation.awardnomination.category;

import com.pation.awardnomination.nominee.Nominee;
import com.pation.awardnomination.nominee.NomineeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 2 - the "time engine". Runs every minute and makes the deadlines take effect:
 *
 *  1. When a category moves to a new phase (nominations close, voting opens, voting closes)
 *     the change is written to the Module 4 transparency log.
 *  2. When a nominee's nomination deadline has passed, a profile still in "Draft" is locked
 *     and moved to "Under Review" (PDF flow: "deadline passes -> profile locks -> Under Review").
 *     Editing after the deadline is blocked by NominationWindowGate.
 *
 * Module 3's code is not changed; the engine only updates the status value through its repository.
 */
@Component
public class TimelineEngine {

    private final AwardCategoryRepository categories;
    private final NomineeRepository nominees;
    private final CategoryService categoryService;
    private final CategoryAuditLogger audit;

    public TimelineEngine(AwardCategoryRepository categories, NomineeRepository nominees,
                          CategoryService categoryService, CategoryAuditLogger audit) {
        this.categories = categories;
        this.nominees = nominees;
        this.categoryService = categoryService;
        this.audit = audit;
    }

    @Scheduled(initialDelay = 15000, fixedDelay = 60000)
    public void tick() {
        try {
            runOnce();
        } catch (RuntimeException e) {
            System.err.println("[Module 2] Timeline engine run failed: " + e.getMessage());
        }
    }

    /** One pass of the engine. Returns how many Draft profiles were locked. */
    public int runOnce() {
        LocalDateTime now = categoryService.now();
        recordPhaseChanges(now);
        return lockExpiredDrafts(now);
    }

    private void recordPhaseChanges(LocalDateTime now) {
        for (AwardCategory c : categories.findAll()) {
            if (c.isArchived()) continue;
            CategoryPhase phase = c.phaseAt(now);
            CategoryPhase before = c.getLastPhase();
            if (phase == before) continue;
            c.setLastPhase(phase);
            try {
                categories.save(c);
            } catch (RuntimeException changedMeanwhile) {
                continue; // the Programme Manager saved this category just now; try again next minute
            }
            if (before == null) continue; // first run for this category: remember the phase quietly
            String details = c.getName() + " moved from " + before.getLabel() + " to " + phase.getLabel() + ". " + phase.getHelp();
            if (phase == CategoryPhase.VOTING_OPEN) {
                details += " Ballot: " + categoryService.entryCount(c.getId()) + " nominee(s).";
            }
            if (phase == CategoryPhase.RESULTS) {
                details += " Ballots sealed: " + categoryService.votes(c.getId()) + ".";
            }
            audit.system(CategoryAuditLogger.ticketCode(c), "PHASE_" + phase.name() + ": " + c.getName(), details);
        }
    }

    private int lockExpiredDrafts(LocalDateTime now) {
        NominationLocks locks = categoryService.locks();
        if (!locks.isConfigured()) return 0;
        List<String> moved = new ArrayList<>();
        for (Nominee n : nominees.findAll()) {
            String status = n.getStatus() == null ? "Draft" : n.getStatus().trim();
            if (!"Draft".equalsIgnoreCase(status)) continue;
            if (!locks.isLocked(n.getId(), now)) continue;
            n.setStatus("Under Review");
            try {
                nominees.save(n);
                moved.add(CategoryService.nomineeName(n));
            } catch (RuntimeException e) {
                System.err.println("[Module 2] Could not lock nominee #" + n.getId() + ": " + e.getMessage());
            }
        }
        if (!moved.isEmpty()) {
            audit.system("CAT-TIMELINE", "NOMINATION_LOCK: " + moved.size() + " draft profile(s) locked",
                    "Nomination deadline passed, so these Module 3 profiles were locked and moved to Under Review: "
                            + String.join(", ", moved));
        }
        return moved.size();
    }
}
