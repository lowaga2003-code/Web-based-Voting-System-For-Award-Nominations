package com.pation.awardnomination.category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Module 2 - result of checking one nominee against a category's eligibility rules. */
public class EligibilityReport {

    /** One rule and what happened to it. */
    public static class Line {
        private final EligibilityRule rule;
        private final boolean passed;
        private final String note;

        public Line(EligibilityRule rule, boolean passed, String note) {
            this.rule = rule;
            this.passed = passed;
            this.note = note;
        }

        public EligibilityRule getRule() { return rule; }
        public boolean isAutomatic() { return rule.isAutomatic(); }
        /** For a manual rule this is false until the Programme Manager confirms it. */
        public boolean isPassed() { return passed; }
        public String getNote() { return note; }
    }

    private final List<Line> lines = new ArrayList<>();

    void add(Line line) {
        lines.add(line);
    }

    public List<Line> getLines() {
        return Collections.unmodifiableList(lines);
    }

    /** True when every automatic rule passed (manual rules are confirmed separately). */
    public boolean isAutoPassed() {
        for (Line l : lines) {
            if (l.isAutomatic() && !l.isPassed()) return false;
        }
        return true;
    }

    public boolean hasManualRules() {
        for (Line l : lines) {
            if (!l.isAutomatic()) return true;
        }
        return false;
    }

    /** Wording of the automatic rules that failed, joined for an error message. */
    public String failedRules() {
        StringBuilder sb = new StringBuilder();
        for (Line l : lines) {
            if (l.isAutomatic() && !l.isPassed()) {
                if (sb.length() > 0) sb.append("; ");
                sb.append(l.getRule().describe());
            }
        }
        return sb.toString();
    }

    /** Short text stored with a ballot entry, e.g. "3/3 automatic rules passed, 1 manual rule confirmed". */
    public String summary(boolean manualConfirmed) {
        int auto = 0;
        int autoOk = 0;
        int manual = 0;
        for (Line l : lines) {
            if (l.isAutomatic()) {
                auto++;
                if (l.isPassed()) autoOk++;
            } else {
                manual++;
            }
        }
        StringBuilder sb = new StringBuilder();
        if (auto == 0 && manual == 0) return "No eligibility rules set for this category";
        if (auto > 0) sb.append(autoOk).append('/').append(auto).append(" automatic rules passed");
        if (manual > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(manual).append(manual == 1 ? " manual rule " : " manual rules ")
              .append(manualConfirmed ? "confirmed" : "not confirmed");
        }
        return sb.toString();
    }
}
