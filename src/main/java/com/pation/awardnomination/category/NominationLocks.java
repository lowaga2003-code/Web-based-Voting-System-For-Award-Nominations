package com.pation.awardnomination.category;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Module 2 - a snapshot of the nomination deadlines, used to lock Module 3.
 *
 * A nominee placed on one or more ballots is locked when the latest nomination deadline of
 * those categories has passed. A nominee on no ballot follows the overall deadline (the latest
 * nomination end among active categories). With no active categories nothing is locked, so
 * Module 3 behaves exactly as it did before Module 2 existed.
 */
public class NominationLocks {

    private final boolean configured;
    private final boolean anyWindowOpen;
    private final LocalDateTime overallDeadline;
    private final Map<Integer, LocalDateTime> perNominee;

    NominationLocks(boolean configured, boolean anyWindowOpen, LocalDateTime overallDeadline,
                    Map<Integer, LocalDateTime> perNominee) {
        this.configured = configured;
        this.anyWindowOpen = anyWindowOpen;
        this.overallDeadline = overallDeadline;
        this.perNominee = perNominee;
    }

    /** True when at least one active category exists. */
    public boolean isConfigured() {
        return configured;
    }

    /** New nominations (Module 3 form) are accepted while any category's window is open. */
    public boolean acceptingNew() {
        return !configured || anyWindowOpen;
    }

    /** The deadline that applies to one nominee, or null when nothing applies. */
    public LocalDateTime deadlineFor(Integer nomineeId) {
        if (!configured) return null;
        LocalDateTime own = nomineeId == null ? null : perNominee.get(nomineeId);
        return own != null ? own : overallDeadline;
    }

    public boolean isLocked(Integer nomineeId, LocalDateTime now) {
        LocalDateTime deadline = deadlineFor(nomineeId);
        return deadline != null && !now.isBefore(deadline);
    }

    public LocalDateTime getOverallDeadline() {
        return overallDeadline;
    }
}
