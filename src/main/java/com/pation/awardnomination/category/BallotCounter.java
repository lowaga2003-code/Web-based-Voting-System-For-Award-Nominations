package com.pation.awardnomination.category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Module 2 needs vote numbers (real-time statistics, "is the ballot sealed?") but must not
 * depend on how voting is built. ABSTRACTION / dependency inversion: Module 2 owns this
 * interface and the Voting part implements it (VoteTallyService).
 */
public interface BallotCounter {

    /** Votes in one category, keyed by Module 3 nominee id. */
    Map<Integer, Long> tally(Long categoryId);

    /** Number of votes cast in one category. */
    long votesIn(Long categoryId);

    /** Number of different voters who have voted at least once. */
    long distinctVoters();

    /** Moments at which votes were cast after "since" (for the live chart). */
    List<LocalDateTime> castTimesSince(LocalDateTime since);
}
