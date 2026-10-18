package com.pation.awardnomination.category;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Module 2 - a Module 3 nominee placed on a category's ballot.
 * Only the nominee id is stored (plus display copies), with no foreign key,
 * so the nominee package keeps working exactly as before.
 */
@Entity
@Table(name = "category_entries",
        uniqueConstraints = @UniqueConstraint(name = "uk_category_entry", columnNames = {"category_id", "nominee_id"}))
public class CategoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private AwardCategory category;

    @Column(name = "nominee_id", nullable = false)
    private Integer nomineeId;

    @Column(nullable = false, length = 255)
    private String nomineeName;

    @Column(length = 255)
    private String organisation;

    /** Eligibility result at the moment the nominee was placed on the ballot. */
    @Column(length = 500)
    private String eligibilityNote;

    @Column(length = 120)
    private String addedBy;

    private LocalDateTime addedAt;

    public CategoryEntry() { }

    public CategoryEntry(AwardCategory category, Integer nomineeId, String nomineeName, String organisation) {
        this.category = category;
        this.nomineeId = nomineeId;
        this.nomineeName = nomineeName;
        this.organisation = organisation;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AwardCategory getCategory() { return category; }
    public void setCategory(AwardCategory category) { this.category = category; }

    public Integer getNomineeId() { return nomineeId; }
    public void setNomineeId(Integer nomineeId) { this.nomineeId = nomineeId; }

    public String getNomineeName() { return nomineeName; }
    public void setNomineeName(String nomineeName) { this.nomineeName = nomineeName; }

    public String getOrganisation() { return organisation; }
    public void setOrganisation(String organisation) { this.organisation = organisation; }

    public String getEligibilityNote() { return eligibilityNote; }
    public void setEligibilityNote(String eligibilityNote) { this.eligibilityNote = eligibilityNote; }

    public String getAddedBy() { return addedBy; }
    public void setAddedBy(String addedBy) { this.addedBy = addedBy; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
