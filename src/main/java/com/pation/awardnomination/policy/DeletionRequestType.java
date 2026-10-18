package com.pation.awardnomination.policy;

/**
 * MODULE 6 - what a user can ask the Legal & Governance Officer to do ("Delete" part of the CRUD).
 */
public enum DeletionRequestType {
    WITHDRAW_CONSENT("Withdraw my consent",
            "Stop using my data on the basis of my consent. I will have to agree again before I can use the system."),
    DELETE_MY_DATA("Delete my personal data",
            "Remove my personal details and close my account. My consent records are kept without personal details, as proof.");

    private final String displayName;
    private final String description;

    DeletionRequestType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    /** "DELETE_MY_DATA" -> DELETE_MY_DATA. Returns null for anything else. */
    public static DeletionRequestType parse(String value) {
        if (value == null) {
            return null;
        }
        for (DeletionRequestType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return null;
    }
}
