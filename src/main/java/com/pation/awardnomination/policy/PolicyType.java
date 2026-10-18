package com.pation.awardnomination.policy;

/**
 * MODULE 6 - Policy & Consent Management.
 * The two legal documents every user agrees to.
 */
public enum PolicyType {
    PRIVACY_POLICY("Privacy Policy", "privacy"),
    TERMS_AND_CONDITIONS("Terms & Conditions", "terms");

    private final String displayName;
    private final String slug;

    PolicyType(String displayName, String slug) {
        this.displayName = displayName;
        this.slug = slug;
    }

    public String getDisplayName() { return displayName; }

    /** Used in page addresses: /policy/privacy, /policy/terms */
    public String getSlug() { return slug; }

    /** "PRIVACY_POLICY" or "privacy" -> PRIVACY_POLICY. Returns null for anything else. */
    public static PolicyType parse(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        for (PolicyType type : values()) {
            if (type.name().equalsIgnoreCase(v) || type.slug.equalsIgnoreCase(v)) {
                return type;
            }
        }
        return null;
    }
}
