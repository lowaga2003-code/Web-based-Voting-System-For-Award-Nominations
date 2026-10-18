package com.pation.awardnomination.category;

import com.pation.awardnomination.nominee.Nominee;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Module 2 - checks a Module 3 nominee against a category's rules.
 * It only reads the nominee (status, evidence, organisation, biography); it never changes it.
 */
@Component
public class EligibilityChecker {

    public EligibilityReport check(Nominee nominee, List<EligibilityRule> rules) {
        EligibilityReport report = new EligibilityReport();
        for (EligibilityRule rule : rules) {
            report.add(evaluate(nominee, rule));
        }
        return report;
    }

    private EligibilityReport.Line evaluate(Nominee n, EligibilityRule rule) {
        String status = n.getStatus() == null ? "Draft" : n.getStatus().trim();
        switch (rule.getRuleType()) {
            case SUBMITTED: {
                boolean ok = !"Draft".equalsIgnoreCase(status);
                return new EligibilityReport.Line(rule, ok, ok ? "Status is " + status : "Still a Draft");
            }
            case APPROVED: {
                boolean ok = "Approved".equalsIgnoreCase(status);
                return new EligibilityReport.Line(rule, ok, ok ? "Approved" : "Status is " + status);
            }
            case EVIDENCE: {
                boolean ok = notBlank(n.getEvidenceFileName());
                return new EligibilityReport.Line(rule, ok, ok ? "File: " + n.getEvidenceFileName() : "No evidence file");
            }
            case ORGANISATION: {
                boolean ok = notBlank(n.getCompanyName());
                return new EligibilityReport.Line(rule, ok, ok ? n.getCompanyName() : "No organisation given");
            }
            case MIN_BIO_WORDS: {
                int words = countWords(n.getBiography());
                int min = rule.getMinimum() == null ? 0 : rule.getMinimum();
                boolean ok = words >= min;
                return new EligibilityReport.Line(rule, ok, words + " of " + min + " words");
            }
            case CUSTOM:
            default:
                return new EligibilityReport.Line(rule, false, "Checked by the Programme Manager");
        }
    }

    static int countWords(String text) {
        if (text == null) return 0;
        String t = text.trim();
        if (t.isEmpty()) return 0;
        return t.split("\\s+").length;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
