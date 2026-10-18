package com.pation.awardnomination.evaluation;

import com.pation.awardnomination.nominee.Nominee;
import com.pation.awardnomination.nominee.NomineeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShortlistService {

    private final ShortlistRepository repo;
    private final NomineeRepository nominees;              // [INTEGRATION] Module 3 (read only)
    private final JudgeAssignmentRepository assignRepo;    // [INTEGRATION] conflict warning
    private final List<ConflictChecker> checkers;
    private final EvaluationScoreRepository scoreRepo;
    private final EvaluationAuditLogger audit;             // [INTEGRATION] Module 4 ledger

    public ShortlistService(ShortlistRepository repo, NomineeRepository nominees, JudgeAssignmentRepository assignRepo,
                            List<ConflictChecker> checkers, EvaluationScoreRepository scoreRepo,
                            EvaluationAuditLogger audit) {
        this.repo = repo;
        this.nominees = nominees;
        this.assignRepo = assignRepo;
        this.checkers = checkers;
        this.scoreRepo = scoreRepo;
        this.audit = audit;
    }

    public List<ShortlistEntry> findAll(String category) {
        return (category == null || category.isBlank()) ? repo.findAll()
                : repo.findByCategoryNameIgnoreCase(category.trim());
    }

    public ShortlistEntry findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Shortlist entry " + id + " not found"));
    }

    /**
     * [INTEGRATION] Nominations from Module 3 that can be shortlisted ("Under Review" or "Approved").
     * A "Draft" has not been submitted for review yet.
     */
    public List<Nominee> nomineeOptions() {
        List<Nominee> list = new ArrayList<>();
        for (Nominee n : nominees.findAll()) {
            if (canBeShortlisted(n)) list.add(n);
        }
        return list;
    }

    static boolean canBeShortlisted(Nominee n) {
        return n.getStatus() != null && !"Draft".equalsIgnoreCase(n.getStatus().trim());
    }

    /**
     * [INTEGRATION] The Chairperson picks a Module 3 nomination and a category; the name and the
     * organization are copied from the nominees table (the member's design: "store only the nominee's ID + display copies").
     * @return conflict-of-interest warnings with judges already on this category's panel (empty = none)
     */
    @Transactional
    public List<String> add(Long nomineeId, String categoryName) {
        if (nomineeId == null) throw new BusinessRuleException("Choose a nominee");
        if (categoryName == null || categoryName.isBlank()) throw new BusinessRuleException("Category is required");
        Nominee n = nominees.findById(nomineeId.intValue())
                .orElseThrow(() -> new ResourceNotFoundException("Nominee #" + nomineeId + " was not found in Module 3"));
        if (!canBeShortlisted(n))
            throw new BusinessRuleException("Nominee #" + nomineeId + " is still a Draft. Only nominations that are"
                    + " Under Review or Approved can be shortlisted.");

        ShortlistEntry e = new ShortlistEntry();
        e.setNomineeId(nomineeId);
        e.setNomineeName(blank(n.getFullName()) ? "Nominee #" + nomineeId : n.getFullName().trim());
        e.setOrganization(blank(n.getCompanyName()) ? "Not stated" : n.getCompanyName().trim());
        e.setCategoryName(categoryName.trim());
        ShortlistEntry saved = add(e);

        List<String> warnings = new ArrayList<>();
        for (JudgeAssignment a : assignRepo.findByCategoryNameIgnoreCase(saved.getCategoryName()))
            for (ConflictChecker checker : checkers)
                if (checker.hasConflict(a.getJudge(), saved))
                    warnings.add(checker.describe(a.getJudge(), saved));
        return warnings;
    }

    @Transactional
    public ShortlistEntry add(ShortlistEntry e) {
        if (repo.existsByNomineeIdAndCategoryNameIgnoreCase(e.getNomineeId(), e.getCategoryName()))
            throw new BusinessRuleException("Nominee is already shortlisted in this category");
        ShortlistEntry saved = repo.save(e);
        audit.log("EVL-ENTRY-" + saved.getId(), "NOMINEE_SHORTLISTED: Nominee #" + saved.getNomineeId()
                + " in " + saved.getCategoryName(), "Added to the final round by the Selection Committee.");
        return saved;
    }

    @Transactional
    public void disqualify(Long id, String reason) {
        if (reason == null || reason.isBlank()) throw new BusinessRuleException("A reason is required to disqualify");
        ShortlistEntry e = findById(id);
        if (e.getStatus() == ShortlistStatus.WINNER)   // [INTEGRATION]
            throw new BusinessRuleException("This candidate is the winner. Revoke the winner status first.");
        e.disqualify(reason.trim());
        audit.log("EVL-ENTRY-" + id, "NOMINEE_DISQUALIFIED: Nominee #" + e.getNomineeId() + " in " + e.getCategoryName(),
                "The reason is kept in the shortlist_entries table.");
    }

    @Transactional
    public void reinstate(Long id) {
        ShortlistEntry e = findById(id);
        if (e.getStatus() != ShortlistStatus.DISQUALIFIED)   // [INTEGRATION]
            throw new BusinessRuleException("Only disqualified candidates can be reinstated");
        e.reinstate();
        audit.log("EVL-ENTRY-" + id, "NOMINEE_REINSTATED: Nominee #" + e.getNomineeId() + " in " + e.getCategoryName(), "");
    }

    @Transactional
    public void delete(Long id) {
        ShortlistEntry e = findById(id);
        scoreRepo.deleteByEntryId(id);   // [INTEGRATION] the candidate's marks go first
        repo.delete(e);
        audit.log("EVL-ENTRY-" + id, "SHORTLIST_ENTRY_REMOVED: Nominee #" + e.getNomineeId() + " from " + e.getCategoryName(),
                "The candidate and their marks were removed from the evaluation.");
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
}
