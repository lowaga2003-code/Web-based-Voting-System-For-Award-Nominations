package com.pation.awardnomination.policy;

/**
 * MODULE 6 - a consent record is ACTIVE until an approved request withdraws it.
 * Records are never deleted: they are the proof of what the user agreed to and when.
 */
public enum ConsentStatus {
    ACTIVE("Active"),
    WITHDRAWN("Withdrawn");

    private final String label;

    ConsentStatus(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}
