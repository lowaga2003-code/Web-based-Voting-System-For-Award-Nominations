package com.pation.awardnomination.category;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Module 2 - Award Category Management (Programme Manager).
 *
 * One award category with its eligibility rules (EligibilityRule) and its timeline.
 * The timeline is two windows, both "start inclusive / end exclusive":
 *   nomination window  [nominationStart, nominationEnd)
 *   voting window      [votingStart, votingEnd)
 * with nominationEnd <= votingStart, so a ballot never changes while people vote.
 *
 * ENCAPSULATION: the phase rules live inside the object that owns the dates.
 */
@Entity
@Table(name = "award_categories")
public class AwardCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short permanent code, e.g. INNOVATOR-YEAR. Used as the ledger ticket code (CAT-...). */
    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1000)
    private String description;

    /** Maximum nominees allowed on this ballot; null means no limit. */
    private Integer ballotLimit;

    @Column(nullable = false)
    private LocalDateTime nominationStart;

    @Column(nullable = false)
    private LocalDateTime nominationEnd;

    @Column(nullable = false)
    private LocalDateTime votingStart;

    @Column(nullable = false)
    private LocalDateTime votingEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private CategoryStatus status = CategoryStatus.ACTIVE;

    @Column(length = 500)
    private String archiveReason;

    private LocalDateTime archivedAt;

    @Column(length = 120)
    private String archivedBy;

    @Column(length = 120)
    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Optimistic locking: if the timeline engine and the Programme Manager save the same
     * category at the same moment, the older copy is rejected instead of overwriting new dates.
     */
    @Version
    private Long version;

    /** The phase the timeline engine last wrote to the transparency log. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CategoryPhase lastPhase;

    public AwardCategory() { }

    // ------------------------------------------------------------------ behaviour

    public boolean isArchived() {
        return status == CategoryStatus.ARCHIVED;
    }

    /** Works out the phase from the dates; nothing else decides it. */
    public CategoryPhase phaseAt(LocalDateTime now) {
        if (isArchived()) return CategoryPhase.ARCHIVED;
        if (now.isBefore(nominationStart)) return CategoryPhase.SCHEDULED;
        if (now.isBefore(nominationEnd)) return CategoryPhase.NOMINATIONS_OPEN;
        if (now.isBefore(votingStart)) return CategoryPhase.BALLOT_PREP;
        if (now.isBefore(votingEnd)) return CategoryPhase.VOTING_OPEN;
        return CategoryPhase.RESULTS;
    }

    public boolean acceptsNominations(LocalDateTime now) {
        return phaseAt(now) == CategoryPhase.NOMINATIONS_OPEN;
    }

    public boolean isVotingOpen(LocalDateTime now) {
        return phaseAt(now) == CategoryPhase.VOTING_OPEN;
    }

    /** The next date on the timeline after "now", or null when everything has passed. */
    public LocalDateTime nextMilestone(LocalDateTime now) {
        if (isArchived()) return null;
        if (now.isBefore(nominationStart)) return nominationStart;
        if (now.isBefore(nominationEnd)) return nominationEnd;
        if (now.isBefore(votingStart)) return votingStart;
        if (now.isBefore(votingEnd)) return votingEnd;
        return null;
    }

    /** What happens at nextMilestone, e.g. "Voting closes". */
    public String nextMilestoneLabel(LocalDateTime now) {
        if (isArchived()) return "Archived";
        if (now.isBefore(nominationStart)) return "Nominations open";
        if (now.isBefore(nominationEnd)) return "Nominations close";
        if (now.isBefore(votingStart)) return "Voting opens";
        if (now.isBefore(votingEnd)) return "Voting closes";
        return "Voting closed";
    }

    public void archive(String reason, String by, LocalDateTime when) {
        this.status = CategoryStatus.ARCHIVED;
        this.archiveReason = reason;
        this.archivedBy = by;
        this.archivedAt = when;
    }

    public void restore() {
        this.status = CategoryStatus.ACTIVE;
        this.archiveReason = null;
        this.archivedBy = null;
        this.archivedAt = null;
        this.lastPhase = null;
    }

    // ------------------------------------------------------------------ getters / setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getBallotLimit() { return ballotLimit; }
    public void setBallotLimit(Integer ballotLimit) { this.ballotLimit = ballotLimit; }

    public LocalDateTime getNominationStart() { return nominationStart; }
    public void setNominationStart(LocalDateTime nominationStart) { this.nominationStart = nominationStart; }

    public LocalDateTime getNominationEnd() { return nominationEnd; }
    public void setNominationEnd(LocalDateTime nominationEnd) { this.nominationEnd = nominationEnd; }

    public LocalDateTime getVotingStart() { return votingStart; }
    public void setVotingStart(LocalDateTime votingStart) { this.votingStart = votingStart; }

    public LocalDateTime getVotingEnd() { return votingEnd; }
    public void setVotingEnd(LocalDateTime votingEnd) { this.votingEnd = votingEnd; }

    public CategoryStatus getStatus() { return status; }
    public void setStatus(CategoryStatus status) { this.status = status; }

    public String getArchiveReason() { return archiveReason; }
    public LocalDateTime getArchivedAt() { return archivedAt; }
    public String getArchivedBy() { return archivedBy; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public CategoryPhase getLastPhase() { return lastPhase; }
    public void setLastPhase(CategoryPhase lastPhase) { this.lastPhase = lastPhase; }
}
