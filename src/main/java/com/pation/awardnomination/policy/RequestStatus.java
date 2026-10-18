package com.pation.awardnomination.policy;

/**
 * MODULE 6 - status of a withdrawal / data deletion request (Pending / Approved / Rejected).
 */
public enum RequestStatus {
    PENDING("Pending review"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String label;

    RequestStatus(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }

    /** "APPROVED" -> APPROVED. Returns null for anything else (e.g. "ALL"). */
    public static RequestStatus parse(String value) {
        if (value == null) {
            return null;
        }
        for (RequestStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return null;
    }
}
