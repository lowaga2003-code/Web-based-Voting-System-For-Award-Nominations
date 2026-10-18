package com.pation.awardnomination.evaluation;

import com.pation.awardnomination.dispute.AuditTransparencyLog;
import com.pation.awardnomination.dispute.AuditTransparencyLogRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * [INTEGRATION - new] MODULE 5 -> MODULE 4 connection.
 *
 * (සිංහලෙන්: PDF එකේ "සිදුවන සෑම ක්‍රියාවක්ම Module 4 ගේ Transparency Log එකේ සටහන් වේ" කියන එක.
 *  Judge පත් කිරීම / ඉවත් කිරීම, disqualify, ලකුණු re-evaluation, Winner තෝරීම වගේ දේවල්
 *  audit_transparency_logs table එකට (/dispute/logs page) ලියනවා. Module 4 code එක වෙනස් කළේ නැහැ.)
 *
 * Dispute tickets have positive ids and Module 6 uses -1 .. -4 000 000 000, so Module 5 rows use
 * large negative, time-based ids that never clash with either.
 */
@Component
public class EvaluationAuditLogger {

    static final String RECORDED_BY = "Evaluation module";
    private static final AtomicLong SEQUENCE = new AtomicLong(System.currentTimeMillis() * 1000L);

    private final AuditTransparencyLogRepository ledger;

    public EvaluationAuditLogger(AuditTransparencyLogRepository ledger) {
        this.ledger = ledger;
    }

    /** @param code short reference shown in the ledger, e.g. "EVL-ENTRY-12" */
    public void log(String code, String action, String details) {
        ledger.save(new AuditTransparencyLog(-SEQUENCE.incrementAndGet(), safe(code, 30), safe(action, 1000),
                RECORDED_BY, safe("By " + actor() + ".", 2000), safe(details, 2000)));
    }

    static String actor() {
        Authentication auth = SecurityContextHolder.getContext() == null ? null
                : SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            return "system";
        }
        return auth.getName();
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
