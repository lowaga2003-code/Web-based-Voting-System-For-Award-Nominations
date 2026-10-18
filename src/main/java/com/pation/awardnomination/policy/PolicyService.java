package com.pation.awardnomination.policy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MODULE 6 - Privacy Policy and Terms & Conditions versions ("Update" part of the CRUD).
 *
 *  createDraft -> INSERT  (a copy of the current text for the Legal & Governance Officer to edit)
 *  saveDraft   -> UPDATE  (only drafts can be edited)
 *  publish     -> UPDATE  (draft gets a version number, the previous version is archived)
 *  deleteDraft -> DELETE  (only drafts - published versions are kept because consents point to them)
 */
@Service
public class PolicyService {

    static final int MAX_CONTENT_LENGTH = 200_000;

    private final PolicyDocumentRepository policies;
    private final PolicyAuditLogger auditLogger;

    public PolicyService(PolicyDocumentRepository policies, PolicyAuditLogger auditLogger) {
        this.policies = policies;
        this.auditLogger = auditLogger;
    }

    // ---------------- read ----------------

    /** The version users see right now. */
    public Optional<PolicyDocument> current(PolicyType type) {
        return policies.findFirstByPolicyTypeAndStatus(type, PolicyStatus.PUBLISHED);
    }

    public Map<PolicyType, PolicyDocument> currentPolicies() {
        Map<PolicyType, PolicyDocument> map = new EnumMap<>(PolicyType.class);
        for (PolicyType type : PolicyType.values()) {
            current(type).ifPresent(doc -> map.put(type, doc));
        }
        return map;
    }

    public Optional<PolicyDocument> draft(PolicyType type) {
        return policies.findFirstByPolicyTypeAndStatus(type, PolicyStatus.DRAFT);
    }

    public Optional<PolicyDocument> find(Long id) {
        return id == null ? Optional.empty() : policies.findById(id);
    }

    /** Every version of one document: the open draft first, then newest version first. */
    public List<PolicyDocument> versions(PolicyType type) {
        List<PolicyDocument> list = new ArrayList<>(policies.findByPolicyType(type));
        list.sort(Comparator.comparing((PolicyDocument d) -> !d.isDraft())
                .thenComparing(PolicyDocument::versionOrder, Comparator.reverseOrder()));
        return list;
    }

    /** Published + archived versions only (the public version history), newest first. */
    public List<PolicyDocument> publishedVersions(PolicyType type) {
        List<PolicyDocument> list = new ArrayList<>();
        for (PolicyDocument doc : versions(type)) {
            if (!doc.isDraft()) {
                list.add(doc);
            }
        }
        return list;
    }

    /**
     * The oldest version a user may still have accepted: the newest version that asked everyone to accept again.
     * (Minor updates don't move it, so users who accepted 1.0 are still fine after 1.1.)
     */
    public Optional<PolicyDocument> minimumAccepted(PolicyType type) {
        List<PolicyDocument> published = publishedVersions(type);
        for (PolicyDocument doc : published) {
            if (doc.isRequiresReacceptance()) {
                return Optional.of(doc);
            }
        }
        return published.isEmpty() ? Optional.empty() : Optional.of(published.get(published.size() - 1));
    }

    public String nextVersionLabel(PolicyType type, boolean majorChange) {
        int[] next = nextVersion(type, majorChange);
        return next[0] + "." + next[1];
    }

    public boolean hasPublishedVersion(PolicyType type) {
        return !publishedVersions(type).isEmpty();
    }

    public long count() {
        return policies.count();
    }

    public long countByStatus(PolicyStatus status) {
        return policies.countByStatus(status);
    }

    // ---------------- create / update / delete ----------------

    /** Opens a draft for editing. If one is already open it is reused (one draft per document at a time). */
    @Transactional
    public PolicyDocument createDraft(PolicyType type, String editor) {
        Optional<PolicyDocument> open = draft(type);
        if (open.isPresent()) {
            return open.get();
        }
        PolicyDocument base = current(type).orElse(null);
        PolicyDocument draft = new PolicyDocument(type,
                base != null ? base.getTitle() : type.getDisplayName(),
                base != null ? base.getContent() : "<p></p>",
                PolicyHtml.limit(editor, 100));
        return policies.save(draft);
    }

    @Transactional
    public PolicyDocument saveDraft(Long id, String title, String content, String changeSummary, String editor) {
        PolicyDocument doc = find(id).orElseThrow(() -> new IllegalArgumentException("This policy version no longer exists."));
        if (!doc.isDraft()) {
            throw new IllegalStateException("Published versions can't be changed. Open a new draft instead.");
        }
        String cleanTitle = PolicyHtml.limit(PolicyHtml.trim(title), 200);
        if (cleanTitle.isEmpty()) {
            throw new IllegalArgumentException("Please give the document a title.");
        }
        String cleanContent = PolicyHtml.sanitize(content);
        if (PolicyHtml.toPlainText(cleanContent, 20).isEmpty()) {
            throw new IllegalArgumentException("The policy text can't be empty.");
        }
        if (cleanContent.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("The policy text is too long.");
        }
        doc.setTitle(cleanTitle);
        doc.setContent(cleanContent);
        String summary = PolicyHtml.limit(PolicyHtml.trim(changeSummary), 500);
        doc.setChangeSummary(summary.isEmpty() ? null : summary);
        doc.setUpdatedBy(PolicyHtml.limit(editor, 100));
        doc.setUpdatedAt(LocalDateTime.now());
        return policies.save(doc);
    }

    /**
     * Publishes a draft. majorChange = true -> next major version (2.0) and every user must accept it again;
     * false -> next minor version (1.1), users are informed but don't have to accept again.
     */
    @Transactional
    public PolicyDocument publish(Long id, boolean majorChange, String editor) {
        PolicyDocument doc = find(id).orElseThrow(() -> new IllegalArgumentException("This policy version no longer exists."));
        if (!doc.isDraft()) {
            throw new IllegalStateException("This version is already published.");
        }
        boolean first = !hasPublishedVersion(doc.getPolicyType());
        if (!first && (doc.getChangeSummary() == null || doc.getChangeSummary().isBlank())) {
            throw new IllegalArgumentException("Please describe what changed - users see this when they are asked to accept the new version.");
        }
        int[] next = nextVersion(doc.getPolicyType(), majorChange);

        Optional<PolicyDocument> previous = current(doc.getPolicyType());
        if (previous.isPresent()) {
            previous.get().setStatus(PolicyStatus.ARCHIVED);
            policies.save(previous.get());
        }
        doc.setVersionMajor(next[0]);
        doc.setVersionMinor(next[1]);
        doc.setRequiresReacceptance(first || majorChange);
        doc.setStatus(PolicyStatus.PUBLISHED);
        doc.setPublishedBy(PolicyHtml.limit(editor, 100));
        doc.setPublishedAt(LocalDateTime.now());
        PolicyDocument saved = policies.save(doc);
        auditLogger.policyPublished(saved);
        return saved;
    }

    @Transactional
    public void deleteDraft(Long id) {
        PolicyDocument doc = find(id).orElseThrow(() -> new IllegalArgumentException("This policy version no longer exists."));
        if (!doc.isDraft()) {
            throw new IllegalStateException("Only drafts can be deleted. Published versions are kept because users' consent records point to them.");
        }
        policies.delete(doc);
    }

    /** Used once by PolicyDataSeeder: version 1.0 of a document, published straight away. */
    @Transactional
    public PolicyDocument seedFirstVersion(PolicyType type, String title, String content) {
        PolicyDocument doc = new PolicyDocument(type, title, PolicyHtml.sanitize(content), "system");
        doc.setChangeSummary("First version.");
        doc = policies.save(doc);
        return publish(doc.getId(), true, "system");
    }

    private int[] nextVersion(PolicyType type, boolean majorChange) {
        List<PolicyDocument> published = publishedVersions(type);
        if (published.isEmpty()) {
            return new int[]{1, 0};
        }
        PolicyDocument latest = published.get(0);
        int major = latest.getVersionMajor() == null ? 1 : latest.getVersionMajor();
        int minor = latest.getVersionMinor() == null ? 0 : latest.getVersionMinor();
        return majorChange ? new int[]{major + 1, 0} : new int[]{major, minor + 1};
    }
}
