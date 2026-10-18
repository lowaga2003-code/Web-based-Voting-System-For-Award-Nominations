package com.pation.awardnomination.dispute;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Main domain entity representing a Dispute Ticket raised by a Registered User.
 */
@Entity
@Table(name = "dispute_tickets")
public class DisputeTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String ticketCode; // e.g., DSP-2026-001

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Registered User who created ticket

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DisputeCategory category;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(length = 2000)
    private String evidenceText;

    @Column(length = 255)
    private String evidenceFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // --- Auditor Investigation Details ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "auditor_id")
    private User auditor; // Compliance Auditor assigned/handling

    @Column(length = 2000)
    private String investigationFindings;

    @Column(length = 2000)
    private String actionsTaken;

    @Column(length = 2000)
    private String finalRemarks;

    private LocalDateTime resolvedAt;

    // --- Additional Investigation / Extension 6a fields ---
    private boolean additionalEvidenceRequested;

    @Column(length = 1000)
    private String evidenceRequestNotes;

    @Column(length = 2000)
    private String additionalEvidenceProvided;

    // --- Step 10: User Acknowledgement ---
    private boolean acknowledgedByUser;

    private LocalDateTime acknowledgedAt;

    public DisputeTicket() {
        this.status = TicketStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.additionalEvidenceRequested = false;
        this.acknowledgedByUser = false;
    }

    public DisputeTicket(String ticketCode, User user, DisputeCategory category, String title, String description, String evidenceText) {
        this();
        this.ticketCode = ticketCode;
        this.user = user;
        this.category = category;
        this.title = title;
        this.description = description;
        this.evidenceText = evidenceText;
    }

    // Domain business methods
    public void escalateToExtendedInvestigation(User auditor, String requestNotes) {
        this.status = TicketStatus.UNDER_INVESTIGATION;
        this.auditor = auditor;
        this.additionalEvidenceRequested = true;
        this.evidenceRequestNotes = requestNotes;
        this.updatedAt = LocalDateTime.now();
    }

    public void provideAdditionalEvidence(String newEvidence) {
        this.additionalEvidenceProvided = newEvidence;
        this.additionalEvidenceRequested = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void resolveAndArchive(User auditor, String findings, String actions, String remarks) {
        this.status = TicketStatus.RESOLVED;
        this.auditor = auditor;
        this.investigationFindings = findings;
        this.actionsTaken = actions;
        this.finalRemarks = remarks;
        this.resolvedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void acknowledgeResolution() {
        this.acknowledgedByUser = true;
        this.acknowledgedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DisputeCategory getCategory() {
        return category;
    }

    public void setCategory(DisputeCategory category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvidenceText() {
        return evidenceText;
    }

    public void setEvidenceText(String evidenceText) {
        this.evidenceText = evidenceText;
    }

    public String getEvidenceFileName() {
        return evidenceFileName;
    }

    public void setEvidenceFileName(String evidenceFileName) {
        this.evidenceFileName = evidenceFileName;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getAuditor() {
        return auditor;
    }

    public void setAuditor(User auditor) {
        this.auditor = auditor;
    }

    public String getInvestigationFindings() {
        return investigationFindings;
    }

    public void setInvestigationFindings(String investigationFindings) {
        this.investigationFindings = investigationFindings;
    }

    public String getActionsTaken() {
        return actionsTaken;
    }

    public void setActionsTaken(String actionsTaken) {
        this.actionsTaken = actionsTaken;
    }

    public String getFinalRemarks() {
        return finalRemarks;
    }

    public void setFinalRemarks(String finalRemarks) {
        this.finalRemarks = finalRemarks;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public boolean isAdditionalEvidenceRequested() {
        return additionalEvidenceRequested;
    }

    public void setAdditionalEvidenceRequested(boolean additionalEvidenceRequested) {
        this.additionalEvidenceRequested = additionalEvidenceRequested;
    }

    public String getEvidenceRequestNotes() {
        return evidenceRequestNotes;
    }

    public void setEvidenceRequestNotes(String evidenceRequestNotes) {
        this.evidenceRequestNotes = evidenceRequestNotes;
    }

    public String getAdditionalEvidenceProvided() {
        return additionalEvidenceProvided;
    }

    public void setAdditionalEvidenceProvided(String additionalEvidenceProvided) {
        this.additionalEvidenceProvided = additionalEvidenceProvided;
    }

    public boolean isAcknowledgedByUser() {
        return acknowledgedByUser;
    }

    public void setAcknowledgedByUser(boolean acknowledgedByUser) {
        this.acknowledgedByUser = acknowledgedByUser;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }
}
