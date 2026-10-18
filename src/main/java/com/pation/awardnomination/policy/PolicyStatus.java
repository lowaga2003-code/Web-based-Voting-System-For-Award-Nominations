package com.pation.awardnomination.policy;

/**
 * MODULE 6 - life cycle of one policy version.
 * DRAFT -> (publish) -> PUBLISHED -> (a newer version is published) -> ARCHIVED
 */
public enum PolicyStatus {
    DRAFT("Draft"),
    PUBLISHED("Current version"),
    ARCHIVED("Previous version");

    private final String label;

    PolicyStatus(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
}
