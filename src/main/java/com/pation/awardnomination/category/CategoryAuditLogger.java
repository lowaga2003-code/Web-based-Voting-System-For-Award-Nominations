package com.pation.awardnomination.category;

import com.pation.awardnomination.dispute.AuditTransparencyLog;
import com.pation.awardnomination.dispute.AuditTransparencyLogRepository;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Module 2 -> Module 4 bridge. Every Programme Manager action and every automatic timeline
 * event is written to the immutable transparency ledger (audit_transparency_logs).
 *
 * Ledger ticket ids are negative so they can never clash with real dispute tickets, and each
 * module has its own range. Module 2 uses -(8e18 + n).
 */
@Component
public class CategoryAuditLogger {

    public static final String RECORDED_BY = "Award Category module";

    private static final long RANGE = 8_000_000_000_000_000_000L;
    private static final AtomicLong SEQUENCE = new AtomicLong(System.currentTimeMillis() * 1000L);

    private final AuditTransparencyLogRepository ledger;

    public CategoryAuditLogger(AuditTransparencyLogRepository ledger) {
        this.ledger = ledger;
    }

    public static String ticketCode(AwardCategory category) {
        return "CAT-" + category.getCode();
    }

    /** Action done by a person, e.g. actor "manager (Programme Manager)". */
    public void log(AwardCategory category, String action, String details, String actor) {
        write(ticketCode(category), action, details, "Done by " + actor);
    }

    /** Action done by the timeline engine on its own. */
    public void system(String ticketCode, String action, String details) {
        write(ticketCode, action, details, "Automatic - Module 2 timeline engine");
    }

    private void write(String code, String action, String details, String findings) {
        try {
            long ticketId = -(RANGE + SEQUENCE.incrementAndGet());
            AuditTransparencyLog row = new AuditTransparencyLog(ticketId, safe(code, 30), safe(action, 1000),
                    RECORDED_BY, safe(details, 2000), safe(findings, 2000));
            ledger.save(row);
        } catch (RuntimeException e) {
            // The ledger must never stop the Programme Manager's own work.
            System.err.println("[Module 2] Could not write to the transparency log: " + e.getMessage());
        }
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
