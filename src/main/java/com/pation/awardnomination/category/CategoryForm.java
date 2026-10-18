package com.pation.awardnomination.category;

import java.time.LocalDateTime;

/**
 * Module 2 - what the Programme Manager typed on the create / edit form.
 * Dates arrive as text from &lt;input type="datetime-local"&gt; ("2026-10-12T18:00")
 * and are parsed by CategoryService, which also does all the validation.
 */
public class CategoryForm {

    private String name;
    private String code;
    private String description;
    private String ballotLimit;
    private String nominationStart;
    private String nominationEnd;
    private String votingStart;
    private String votingEnd;

    // Eligibility rules chosen while creating a category
    private boolean ruleSubmitted;
    private boolean ruleApproved;
    private boolean ruleEvidence;
    private boolean ruleOrganisation;
    private String minBioWords;
    private String customRules;

    public CategoryForm() { }

    /** Pre-fills the edit form from a saved category. */
    public static CategoryForm of(AwardCategory c) {
        CategoryForm f = new CategoryForm();
        f.name = c.getName();
        f.code = c.getCode();
        f.description = c.getDescription();
        f.ballotLimit = c.getBallotLimit() == null ? "" : String.valueOf(c.getBallotLimit());
        f.nominationStart = input(c.getNominationStart());
        f.nominationEnd = input(c.getNominationEnd());
        f.votingStart = input(c.getVotingStart());
        f.votingEnd = input(c.getVotingEnd());
        return f;
    }

    /** Suggested timeline for a new category: nominations for two weeks, then one week of voting. */
    public static CategoryForm blank(LocalDateTime now) {
        CategoryForm f = new CategoryForm();
        LocalDateTime start = now.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        f.nominationStart = input(start);
        f.nominationEnd = input(start.plusDays(14).withHour(17));
        f.votingStart = input(start.plusDays(15));
        f.votingEnd = input(start.plusDays(22).withHour(17));
        f.ruleSubmitted = true;
        f.ruleEvidence = true;
        return f;
    }

    static String input(LocalDateTime t) {
        if (t == null) return "";
        String s = t.withSecond(0).withNano(0).toString();
        return s.length() > 16 ? s.substring(0, 16) : s;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBallotLimit() { return ballotLimit; }
    public void setBallotLimit(String ballotLimit) { this.ballotLimit = ballotLimit; }
    public String getNominationStart() { return nominationStart; }
    public void setNominationStart(String nominationStart) { this.nominationStart = nominationStart; }
    public String getNominationEnd() { return nominationEnd; }
    public void setNominationEnd(String nominationEnd) { this.nominationEnd = nominationEnd; }
    public String getVotingStart() { return votingStart; }
    public void setVotingStart(String votingStart) { this.votingStart = votingStart; }
    public String getVotingEnd() { return votingEnd; }
    public void setVotingEnd(String votingEnd) { this.votingEnd = votingEnd; }
    public boolean isRuleSubmitted() { return ruleSubmitted; }
    public void setRuleSubmitted(boolean ruleSubmitted) { this.ruleSubmitted = ruleSubmitted; }
    public boolean isRuleApproved() { return ruleApproved; }
    public void setRuleApproved(boolean ruleApproved) { this.ruleApproved = ruleApproved; }
    public boolean isRuleEvidence() { return ruleEvidence; }
    public void setRuleEvidence(boolean ruleEvidence) { this.ruleEvidence = ruleEvidence; }
    public boolean isRuleOrganisation() { return ruleOrganisation; }
    public void setRuleOrganisation(boolean ruleOrganisation) { this.ruleOrganisation = ruleOrganisation; }
    public String getMinBioWords() { return minBioWords; }
    public void setMinBioWords(String minBioWords) { this.minBioWords = minBioWords; }
    public String getCustomRules() { return customRules; }
    public void setCustomRules(String customRules) { this.customRules = customRules; }
}
