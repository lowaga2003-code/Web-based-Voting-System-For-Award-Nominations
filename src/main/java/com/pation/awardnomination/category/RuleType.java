package com.pation.awardnomination.category;

/**
 * Module 2 - kinds of eligibility rule a category can have.
 * Automatic rules are checked against the nominee's Module 3 record;
 * a CUSTOM rule is confirmed by the Programme Manager when placing the nominee on the ballot.
 */
public enum RuleType {
    SUBMITTED("Nomination submitted (not Draft)", true),
    APPROVED("Nomination approved by the administrator", true),
    EVIDENCE("Evidence document uploaded", true),
    ORGANISATION("Organisation / company is named", true),
    MIN_BIO_WORDS("Biography has a minimum number of words", true),
    CUSTOM("Custom rule, confirmed by the Programme Manager", false);

    private final String label;
    private final boolean automatic;

    RuleType(String label, boolean automatic) {
        this.label = label;
        this.automatic = automatic;
    }

    public String getLabel() { return label; }
    public boolean isAutomatic() { return automatic; }
}
