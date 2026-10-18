package com.pation.awardnomination.policy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

/**
 * MODULE 6 - a user's request to withdraw consent or delete personal data (the "Delete" part of the CRUD).
 * SQL Server table: data_deletion_requests
 *
 * Nothing is deleted straight away: the request is tracked with a status (Pending / Approved / Rejected)
 * and the Legal & Governance Officer (or the System Administrator) decides.
 */
@Entity
@Table(name = "data_deletion_requests", indexes = @Index(name = "ix_deletion_requests_user", columnList = "user_id"))
public class DeletionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** users.id of the main login account (Module 1). */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 30)
    private DeletionRequestType requestType;

    @Nationalized
    @Column(length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Nationalized
    @Column(name = "review_note", length = 1000)
    private String reviewNote;

    /** What the system did after the decision, e.g. "2 consent records withdrawn ...". */
    @Nationalized
    @Column(length = 500)
    private String outcome;

    protected DeletionRequest() {
        // for JPA
    }

    public DeletionRequest(Long userId, DeletionRequestType requestType, String reason) {
        this.userId = userId;
        this.requestType = requestType;
        this.reason = reason;
        this.status = RequestStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    public boolean isPending() { return status == RequestStatus.PENDING; }

    /** Short reference shown on pages and in the transparency ledger, e.g. DDR-0004. */
    public String getCode() { return PolicyHtml.code("DDR", id, 4); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }

    public DeletionRequestType getRequestType() { return requestType; }

    public String getReason() { return reason; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
}
