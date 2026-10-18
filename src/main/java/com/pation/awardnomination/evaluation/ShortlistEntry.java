package com.pation.awardnomination.evaluation;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Stores only the nominee's ID (+ display copies). We do NOT touch the nominee package.
 * When merged, the service can look the nominee up with NomineeRepository.findById(nomineeId).
 *
 * [INTEGRATION] merged: ShortlistService now fills nomineeName / organization from Module 3's
 * nominees table (NomineeRepository.findById), so the Chairperson only picks the nominee and the category.
 */
@Entity
@Table(name = "shortlist_entries")
public class ShortlistEntry extends BaseEntity {

    @NotNull(message = "Nominee ID is required")
    private Long nomineeId;
    @NotBlank(message = "Nominee name is required")
    private String nomineeName;
    @NotBlank(message = "Organization is required")
    private String organization;
    @NotBlank(message = "Category is required")
    private String categoryName;

    @Enumerated(EnumType.STRING)
    private ShortlistStatus status = ShortlistStatus.SHORTLISTED;
    private String reason;

    // ENCAPSULATION: state changes go through methods that protect the rules
    public void disqualify(String reason) {
        this.status = ShortlistStatus.DISQUALIFIED;
        this.reason = reason;
    }
    public void reinstate() {
        this.status = ShortlistStatus.SHORTLISTED;
        this.reason = null;
    }
    // [INTEGRATION] "Update: change a candidate's status to Winner"
    public void markWinner() {
        this.status = ShortlistStatus.WINNER;
        this.reason = null;
    }
    public void revokeWinner() {
        this.status = ShortlistStatus.SHORTLISTED;
    }
    public boolean isActive() { return status != ShortlistStatus.DISQUALIFIED; }

    public Long getNomineeId() { return nomineeId; }
    public void setNomineeId(Long nomineeId) { this.nomineeId = nomineeId; }
    public String getNomineeName() { return nomineeName; }
    public void setNomineeName(String nomineeName) { this.nomineeName = nomineeName; }
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public ShortlistStatus getStatus() { return status; }
    public String getReason() { return reason; }
}
