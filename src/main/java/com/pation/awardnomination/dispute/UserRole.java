package com.pation.awardnomination.dispute;

/**
 * Defines the actor roles in the system.
 */
public enum UserRole {
    VOTER("Registered User"),
    COMPLIANCE_AUDITOR("Compliance Auditor"),
    SYSTEM_ADMIN("System Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
