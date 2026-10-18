package com.pation.awardnomination.dispute;

/**
 * Dispute categories aligned with Award Nomination Voting anomalies.
 */
public enum DisputeCategory {
    VOTING_ANOMALY("Voting Anomaly", "Discrepancy observed while casting vote"),
    DUPLICATE_VOTE_DETECTED("Duplicate Vote Warning", "System reported vote already recorded"),
    RESULTS_DISCREPANCY("Results Discrepancy", "Category tally or vote summary mismatch"),
    CANDIDATE_IRREGULARITY("Nominee Profile Issue", "Nominee details or eligibility irregularity"),
    TRANSPARENCY_CONCERN("Transparency Concern", "Audit trail or voting receipt verification inquiry");

    private final String friendlyName;
    private final String description;

    DisputeCategory(String friendlyName, String description) {
        this.friendlyName = friendlyName;
        this.description = description;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getDescription() {
        return description;
    }
}
