package com.pation.awardnomination.policy;

/**
 * MODULE 6 - where the user ticked "I agree" (consent_type column).
 *  REGISTRATION : the sign-up form
 *  VOTING       : the consent box of the voting page
 *  SIGN_IN      : the "please accept" page shown after sign-in (new account, withdrawn consent or a major update)
 */
public enum ConsentType {
    REGISTRATION("Registration form", "when you created your account"),
    VOTING("Voting page", "on the voting page"),
    SIGN_IN("Sign-in", "when you signed in");

    private final String displayName;
    private final String phrase;

    ConsentType(String displayName, String phrase) {
        this.displayName = displayName;
        this.phrase = phrase;
    }

    public String getDisplayName() { return displayName; }

    /** For sentences: "You agreed to this version on 1 Oct 2026 at 14:05, when you created your account." */
    public String getPhrase() { return phrase; }

    /** "VOTING" -> VOTING. Returns null for anything else. */
    public static ConsentType parse(String value) {
        if (value == null) {
            return null;
        }
        for (ConsentType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return null;
    }
}
