package com.pation.awardnomination.policy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

/**
 * MODULE 6 - one version of the Privacy Policy or of the Terms & Conditions.
 * SQL Server table: policy_documents
 *
 * (සිංහලෙන්: Legal Officer edit කරන කාලේ version එක DRAFT. Publish කළාම 1.0, 1.1, 2.0 වගේ version
 *  number එකක් ලැබෙනවා, පරණ version එක ARCHIVED වෙනවා. Publish කරපු version එකක් කවදාවත් වෙනස්
 *  වෙන්නේ නැහැ - users ලාගේ consent records ඒ exact version එකට point කරන නිසා.)
 */
@Entity
@Table(name = "policy_documents")
public class PolicyDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false, length = 30)
    private PolicyType policyType;

    @Nationalized
    @Column(nullable = false, length = 200)
    private String title;

    /** Formatted text from the rich-text editor, cleaned by PolicyHtml.sanitize() before it is saved. */
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "version_major")
    private Integer versionMajor; // null while the version is a DRAFT

    @Column(name = "version_minor")
    private Integer versionMinor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PolicyStatus status;

    /** true = every user must accept this version again (first version or a major update). */
    @Column(name = "requires_reacceptance", nullable = false)
    private boolean requiresReacceptance;

    /** "What changed" - shown to users when they are asked to accept the new version. */
    @Nationalized
    @Column(name = "change_summary", length = 500)
    private String changeSummary;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_by", length = 100)
    private String publishedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    protected PolicyDocument() {
        // for JPA
    }

    public PolicyDocument(PolicyType policyType, String title, String content, String createdBy) {
        this.policyType = policyType;
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.status = PolicyStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    // ---------------- helpers ----------------

    public boolean isDraft() { return status == PolicyStatus.DRAFT; }

    /** The version users see right now. */
    public boolean isCurrent() { return status == PolicyStatus.PUBLISHED; }

    /** "1.0", "1.1", "2.0" ... or "Draft". */
    public String getVersionLabel() {
        if (versionMajor == null) {
            return "Draft";
        }
        return versionMajor + "." + (versionMinor == null ? 0 : versionMinor);
    }

    /** 1.0 -> 1000, 1.1 -> 1001, 2.0 -> 2000 (drafts -> -1). Used to compare versions. */
    public int versionOrder() {
        if (versionMajor == null) {
            return -1;
        }
        return versionMajor * 1000 + (versionMinor == null ? 0 : versionMinor);
    }

    /** Same number for a stored label such as "1.1" (consent_records keeps the label). */
    public static int versionOrder(String label) {
        if (label == null || !label.matches("\\d{1,6}\\.\\d{1,3}")) {
            return -1;
        }
        String[] parts = label.split("\\.");
        return Integer.parseInt(parts[0]) * 1000 + Integer.parseInt(parts[1]);
    }

    /** Short reference shown on pages and in the transparency ledger, e.g. POL-0003. */
    public String getCode() { return PolicyHtml.code("POL", id, 4); }

    // ---------------- getters / setters ----------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PolicyType getPolicyType() { return policyType; }
    public void setPolicyType(PolicyType policyType) { this.policyType = policyType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getVersionMajor() { return versionMajor; }
    public void setVersionMajor(Integer versionMajor) { this.versionMajor = versionMajor; }

    public Integer getVersionMinor() { return versionMinor; }
    public void setVersionMinor(Integer versionMinor) { this.versionMinor = versionMinor; }

    public PolicyStatus getStatus() { return status; }
    public void setStatus(PolicyStatus status) { this.status = status; }

    public boolean isRequiresReacceptance() { return requiresReacceptance; }
    public void setRequiresReacceptance(boolean requiresReacceptance) { this.requiresReacceptance = requiresReacceptance; }

    public String getChangeSummary() { return changeSummary; }
    public void setChangeSummary(String changeSummary) { this.changeSummary = changeSummary; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getPublishedBy() { return publishedBy; }
    public void setPublishedBy(String publishedBy) { this.publishedBy = publishedBy; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
