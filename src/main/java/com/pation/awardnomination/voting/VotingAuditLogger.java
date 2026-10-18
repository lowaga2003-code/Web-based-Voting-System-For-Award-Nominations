package com.pation.awardnomination.voting;

import com.pation.awardnomination.admin.User;
import com.pation.awardnomination.category.AwardCategory;
import com.pation.awardnomination.dispute.AuditTransparencyLog;
import com.pation.awardnomination.dispute.AuditTransparencyLogRepository;
import com.pation.awardnomination.dispute.SystemActivityLog;
import com.pation.awardnomination.dispute.SystemActivityLogRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Voting -> Module 4 (PDF: "every vote is recorded in the Module 4 transparency log").
 *
 *  - Transparency log (public): receipt code + category + seal hash. It does NOT say who voted
 *    or for whom, so ballot secrecy is kept while each voter can find their own receipt.
 *  - System activity log (auditor only): which account voted, from which IP / browser, and any
 *    duplicate attempt flagged as FLAGGED_ANOMALY. The auditor sees these on a dispute ticket.
 *
 * Ledger ticket ids use the Voting range -(9e18 + n).
 */
@Component
public class VotingAuditLogger {

    public static final String RECORDED_BY = "Voting module";

    private static final long RANGE = 9_000_000_000_000_000_000L;
    private static final AtomicLong SEQUENCE = new AtomicLong(System.currentTimeMillis() * 1000L);

    private final AuditTransparencyLogRepository ledger;
    private final SystemActivityLogRepository activity;

    public VotingAuditLogger(AuditTransparencyLogRepository ledger, SystemActivityLogRepository activity) {
        this.ledger = ledger;
        this.activity = activity;
    }

    public void ballotSealed(Vote vote, AwardCategory category, User voter, String ip, String agent) {
        activity(voter, "VOTE_CAST_SUBMISSION", ip, agent,
                "Ballot sealed in category " + category.getName() + " (receipt " + vote.getReceiptCode() + ").", "NORMAL");
        ledger(vote.getReceiptCode(), "BALLOT_SEALED: " + category.getName(),
                "One vote recorded in category " + category.getCode() + ". Who voted and the choice stay private; "
                        + "the holder of this receipt can match this entry.",
                "Seal SHA-256 " + vote.getSealHash());
    }

    public void duplicateAttempt(User voter, AwardCategory category, Vote existing, String ip, String agent) {
        String receipt = existing == null ? "unknown" : existing.getReceiptCode();
        activity(voter, "DUPLICATE_BALLOT_ATTEMPT", ip, agent,
                "Second vote attempted in category " + category.getName() + " and blocked. First receipt " + receipt + ".",
                "FLAGGED_ANOMALY");
        ledger("BLOCKED-" + receipt, "DUPLICATE_VOTE_BLOCKED: " + category.getName(),
                "A second ballot in category " + category.getCode() + " was rejected (one vote per category).",
                "Flagged for the Compliance Auditor in the system activity log");
    }

    /** The public ledger line of a receipt, if it exists. */
    public Optional<AuditTransparencyLog> ledgerEntry(String receiptCode) {
        try {
            return ledger.findByTicketCode(receiptCode);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private void activity(User voter, String action, String ip, String agent, String details, String status) {
        try {
            activity.save(new SystemActivityLog(cut(voter.getUsername(), 50), action, cut(ip, 45), cut(agent, 200),
                    cut(details, 1000), status));
        } catch (RuntimeException e) {
            System.err.println("[Voting] Could not write the system activity log: " + e.getMessage());
        }
    }

    private void ledger(String code, String action, String remarks, String findings) {
        try {
            long ticketId = -(RANGE + SEQUENCE.incrementAndGet());
            ledger.save(new AuditTransparencyLog(ticketId, safe(code, 30), safe(action, 1000), RECORDED_BY,
                    safe(remarks, 2000), safe(findings, 2000)));
        } catch (RuntimeException e) {
            System.err.println("[Voting] Could not write to the transparency log: " + e.getMessage());
        }
    }

    private static String cut(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }

    /** Module 4 prints ledger text as raw HTML, so markup characters are removed here. */
    static String safe(String text, int max) {
        if (text == null) return "-";
        String s = text.replace("&", " and ")
                .replaceAll("[<>\"'`\\\\]", "")
                .replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll(" {2,}", " ")
                .trim();
        if (s.isEmpty()) s = "-";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }
}
