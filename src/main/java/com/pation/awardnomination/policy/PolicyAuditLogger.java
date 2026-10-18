package com.pation.awardnomination.policy;

import com.pation.awardnomination.dispute.AuditTransparencyLog;
import com.pation.awardnomination.dispute.AuditTransparencyLogRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * MODULE 6 -> MODULE 4 connection ("Audit connection" in the specification).
 *
 * (සිංහලෙන්: Consent එකක් save වෙද්දී, policy එකක් publish වෙද්දී, deletion request එකක් එද්දී/decide
 *  කරද්දී, ඒක Module 4 එකේ Transparency Ledger එකටත් (audit_transparency_logs table, /dispute/logs page)
 *  ලියනවා. Module 4 එකේ SHA-256 hash එකෙන්ම seal වෙනවා. Module 4 code එක වෙනස් කරලා නැහැ.)
 *
 * Dispute tickets always have POSITIVE ids, so Module 6 rows use NEGATIVE ticket ids (one range per event),
 * so Module 4's "delete the log of ticket X" can never remove a Module 6 row.
 * Users appear only as "User #id", because the ledger page is visible to every signed-in user.
 */
@Component
public class PolicyAuditLogger {

    static final String RECORDED_BY = "Policy and Consent module";

    private static final long CONSENT_RANGE = 0L;
    private static final long POLICY_RANGE = 1_000_000_000L;
    private static final long REQUEST_RANGE = 2_000_000_000L;
    private static final long REVIEW_RANGE = 3_000_000_000L;

    private final AuditTransparencyLogRepository ledger;

    public PolicyAuditLogger(AuditTransparencyLogRepository ledger) {
        this.ledger = ledger;
    }

    public void consentRecorded(Long userId, ConsentType type, List<ConsentRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<String> documents = new ArrayList<>();
        List<String> codes = new ArrayList<>();
        for (ConsentRecord record : records) {
            documents.add(record.getPolicyType().getDisplayName() + " v" + record.getPolicyVersion());
            codes.add(record.getCode());
        }
        ConsentRecord first = records.get(0);
        write(-(CONSENT_RANGE + first.getId()), first.getCode(),
                "CONSENT_RECORDED (" + type.getDisplayName() + "): " + String.join(", ", documents),
                "User #" + userId + " agreed to " + String.join(" and ", documents) + ".",
                "Stored in consent_records as " + String.join(", ", codes) + ".");
    }

    public void policyPublished(PolicyDocument doc) {
        write(-(POLICY_RANGE + doc.getId()), doc.getCode(),
                "POLICY_PUBLISHED: " + doc.getPolicyType().getDisplayName() + " v" + doc.getVersionLabel()
                        + (doc.isRequiresReacceptance() ? " (every user must accept it)" : " (minor update, users are informed)"),
                "Published by " + doc.getPublishedBy() + ".",
                doc.getChangeSummary() == null ? "" : "What changed: " + doc.getChangeSummary());
    }

    public void requestSubmitted(DeletionRequest request) {
        write(-(REQUEST_RANGE + request.getId()), request.getCode(),
                "DATA_REQUEST_SUBMITTED: " + request.getRequestType().getDisplayName(),
                "User #" + request.getUserId() + " sent the request to the Legal and Governance Officer.",
                "Status: Pending review.");
    }

    public void requestReviewed(DeletionRequest request) {
        write(-(REVIEW_RANGE + request.getId()), request.getCode(),
                "DATA_REQUEST_" + request.getStatus().name() + ": " + request.getRequestType().getDisplayName(),
                "Reviewed by " + request.getReviewedBy() + ".",
                request.getOutcome());
    }

    private void write(long ticketId, String code, String action, String remarks, String findings) {
        ledger.save(new AuditTransparencyLog(ticketId, safe(code, 30), safe(action, 1000), RECORDED_BY,
                safe(remarks, 2000), safe(findings, 2000)));
    }

    /** Module 4's ledger page prints values without HTML-escaping, so only plain, safe characters go in. */
    static String safe(String text, int max) {
        if (text == null) {
            return "";
        }
        String clean = text.replace("&", "and")
                .replaceAll("[<>\"'`\\\\]", "")
                .replaceAll("[\\r\\n\\t]+", " ")
                .trim();
        return clean.length() > max ? clean.substring(0, max) : clean;
    }
}
