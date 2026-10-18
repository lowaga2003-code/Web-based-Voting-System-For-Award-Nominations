package com.pation.awardnomination.dispute;

/**
 * Lifecycle states of a Dispute Ticket.
 * Matches Use Case Scenario 6 & UML Activity Diagram:
 * - PENDING: Newly submitted by voter.
 * - UNDER_INVESTIGATION: Extended investigation triggered (Step 6a).
 * - RESOLVED: Auditor completes findings and archives to Transparency Log (Step 8).
 */
public enum TicketStatus {
    PENDING("Pending Review", "Waiting for Compliance Auditor review"),
    UNDER_INVESTIGATION("Under Investigation", "Extended technical investigation in progress"),
    RESOLVED("Resolved & Archived", "Dispute addressed and stored in immutable transparency log");

    private final String label;
    private final String description;

    TicketStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
