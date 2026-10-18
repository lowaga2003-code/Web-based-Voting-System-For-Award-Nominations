package com.pation.awardnomination.evaluation;

/**
 * OOP - ABSTRACTION: says WHAT a conflict check does, not HOW.
 * New rules (e.g. family relationship) = a new class implementing this; no other code changes.
 */
public interface ConflictChecker {
    boolean hasConflict(Judge judge, ShortlistEntry nominee);
    String describe(Judge judge, ShortlistEntry nominee);
}
