package com.pation.awardnomination.policy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * MODULE 6 - one timestamped "I agree" (the "Create" part of the CRUD).
 * SQL Server table: consent_records
 *
 * One row per document: agreeing to the Privacy Policy and the Terms at the same time stores two rows.
 * Rows are never deleted - an approved withdrawal only marks them WITHDRAWN - so they stay as proof.
 */
@Entity
@Table(name = "consent_records", indexes = @Index(name = "ix_consent_records_user", columnList = "user_id"))
public class ConsentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** users.id of the main login account (Module 1). No foreign key, so Module 1 can still delete accounts. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** The exact policy version the user agreed to (foreign key -> policy_documents.id). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private PolicyDocument policy;

    /** Read-only copy of the same column, so pages never have to load the policy text. */
    @Column(name = "policy_id", insertable = false, updatable = false)
    private Long policyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false, length = 30)
    private PolicyType policyType;

    /** Version label at the moment of agreeing, e.g. "1.0". */
    @Column(name = "policy_version", nullable = false, length = 20)
    private String policyVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false, length = 20)
    private ConsentType consentType;

    /** When the user ticked "I agree" (column name as in the specification: agreed_date_time). */
    @Column(name = "agreed_date_time", nullable = false)
    private LocalDateTime agreedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConsentStatus status;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    protected ConsentRecord() {
        // for JPA
    }

    public ConsentRecord(Long userId, PolicyDocument policy, ConsentType consentType, LocalDateTime agreedAt,
                         String ipAddress, String userAgent) {
        this.userId = userId;
        this.policy = policy;
        this.policyId = policy.getId();
        this.policyType = policy.getPolicyType();
        this.policyVersion = policy.getVersionLabel();
        this.consentType = consentType;
        this.agreedAt = agreedAt;
        this.status = ConsentStatus.ACTIVE;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public boolean isActive() { return status == ConsentStatus.ACTIVE; }

    /** Short reference shown on pages and in the transparency ledger, e.g. CNS-000012. */
    public String getCode() { return PolicyHtml.code("CNS", id, 6); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }

    public PolicyDocument getPolicy() { return policy; }

    public Long getPolicyId() { return policyId; }

    public PolicyType getPolicyType() { return policyType; }

    public String getPolicyVersion() { return policyVersion; }

    public ConsentType getConsentType() { return consentType; }

    public LocalDateTime getAgreedAt() { return agreedAt; }

    public ConsentStatus getStatus() { return status; }
    public void setStatus(ConsentStatus status) { this.status = status; }

    public LocalDateTime getWithdrawnAt() { return withdrawnAt; }
    public void setWithdrawnAt(LocalDateTime withdrawnAt) { this.withdrawnAt = withdrawnAt; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
