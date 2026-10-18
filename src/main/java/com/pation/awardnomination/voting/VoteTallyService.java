package com.pation.awardnomination.voting;

import com.pation.awardnomination.category.BallotCounter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Voting -> Module 2. Implements Module 2's BallotCounter interface, so the Programme Manager's
 * real-time statistics can count votes without Module 2 depending on the voting classes.
 * POLYMORPHISM: Module 2 only sees a BallotCounter.
 */
@Service
public class VoteTallyService implements BallotCounter {

    private final VoteRepository votes;

    public VoteTallyService(VoteRepository votes) {
        this.votes = votes;
    }

    @Override
    public Map<Integer, Long> tally(Long categoryId) {
        return votes.tallyByNominee(categoryId);
    }

    @Override
    public long votesIn(Long categoryId) {
        return votes.countByCategoryId(categoryId);
    }

    @Override
    public long distinctVoters() {
        Set<Long> voters = new HashSet<>();
        for (Vote v : votes.findAll()) voters.add(v.getVoterId());
        return voters.size();
    }

    @Override
    public List<LocalDateTime> castTimesSince(LocalDateTime since) {
        List<LocalDateTime> times = new ArrayList<>();
        for (Vote v : votes.findByCastAtAfter(since)) times.add(v.getCastAt());
        return times;
    }

    /** {sealed ballots, ballots whose seal still matches}: a quick tamper check. */
    public long[] integrity() {
        long total = 0;
        long intact = 0;
        for (Vote v : votes.findAll()) {
            total++;
            if (v.isSealIntact()) intact++;
        }
        return new long[]{total, intact};
    }
}
