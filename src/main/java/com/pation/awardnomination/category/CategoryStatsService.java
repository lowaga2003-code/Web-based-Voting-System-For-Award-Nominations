package com.pation.awardnomination.category;

import com.pation.awardnomination.admin.Role;
import com.pation.awardnomination.admin.RoleName;
import com.pation.awardnomination.admin.User;
import com.pation.awardnomination.admin.UserRepository;
import com.pation.awardnomination.nominee.Nominee;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Module 2 - real-time statistics for the Programme Manager:
 * nominations received (Module 3), ballots (Module 2), votes and turnout (Voting / Module 1).
 */
@Service
public class CategoryStatsService {

    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final ObjectProvider<BallotCounter> counters;

    public CategoryStatsService(CategoryService categoryService, UserRepository userRepository,
                                ObjectProvider<BallotCounter> counters) {
        this.categoryService = categoryService;
        this.userRepository = userRepository;
        this.counters = counters;
    }

    public LiveStats snapshot() {
        LiveStats s = new LiveStats();
        LocalDateTime now = categoryService.now();
        s.generatedAt = now;

        // Module 3 - nominations received
        for (Nominee n : categoryService.allNominees()) {
            s.nominationsTotal++;
            String status = n.getStatus() == null ? "Draft" : n.getStatus().trim();
            if ("Draft".equalsIgnoreCase(status)) s.drafts++;
            else if ("Under Review".equalsIgnoreCase(status)) s.underReview++;
            else if ("Approved".equalsIgnoreCase(status)) s.approved++;
            else s.otherStatus++;
        }

        // Module 1 - registered voters
        for (User u : userRepository.findAll()) {
            if (u.getRoles() == null) continue;
            for (Role r : u.getRoles()) {
                if (r.getName() == RoleName.ROLE_VOTER) {
                    s.registeredVoters++;
                    break;
                }
            }
        }

        // Module 2 + Voting - per category
        BallotCounter counter = counters.getIfAvailable();
        for (AwardCategory c : categoryService.activeCategories()) {
            CategoryPhase phase = c.phaseAt(now);
            s.activeCategories++;
            s.byPhase.merge(phase, 1, Integer::sum);
            List<CategoryEntry> entries = categoryService.entries(c.getId());
            s.entriesOnBallots += entries.size();
            Map<Integer, Long> tally = counter == null ? Collections.emptyMap() : counter.tally(c.getId());
            long votes = 0;
            for (Long v : tally.values()) votes += v;
            s.votesTotal += votes;

            long top = 0;
            for (Long v : tally.values()) top = Math.max(top, v);
            List<LiveStats.NomineeLive> rows = new ArrayList<>();
            Set<Integer> listed = new HashSet<>();
            for (CategoryEntry e : entries) {
                long v = tally.getOrDefault(e.getNomineeId(), 0L);
                rows.add(new LiveStats.NomineeLive(e.getNomineeId(), e.getNomineeName(), e.getOrganisation(), v,
                        percent(v, votes), v > 0 && v == top));
                listed.add(e.getNomineeId());
            }
            // votes for a nominee that was later removed from Module 3 still count
            for (Map.Entry<Integer, Long> t : tally.entrySet()) {
                if (!listed.contains(t.getKey())) {
                    rows.add(new LiveStats.NomineeLive(t.getKey(), "Nominee #" + t.getKey() + " (removed)", null,
                            t.getValue(), percent(t.getValue(), votes), t.getValue() == top));
                }
            }
            rows.sort((a, b) -> Long.compare(b.getVotes(), a.getVotes()));
            s.categories.add(new LiveStats.CategoryLive(c, phase, entries.size(), votes, rows));
        }

        if (counter != null) {
            s.distinctVoters = counter.distinctVoters();
            LocalDateTime from = now.minusHours(24);
            for (LocalDateTime t : counter.castTimesSince(from)) {
                long hoursAgo = Duration.between(t, now).toHours();
                if (hoursAgo >= 0 && hoursAgo < 24) s.lastHours[23 - (int) hoursAgo]++;
            }
        }
        return s;
    }

    static int percent(long part, long total) {
        if (total <= 0) return 0;
        return (int) Math.round(part * 100.0 / total);
    }
}
