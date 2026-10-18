package com.pation.awardnomination.evaluation;

/**
 * [INTEGRATION - new] A judge on the panel who now has a conflict of interest with a candidate
 * (e.g. the candidate was shortlisted after the judge was assigned). Shown on the Judge Panel page
 * with a "Remove judge" button - specification "Delete: remove judges with conflicts of interest".
 */
public class PanelConflict {

    private final JudgeAssignment assignment;
    private final ShortlistEntry entry;
    private final String description;

    public PanelConflict(JudgeAssignment assignment, ShortlistEntry entry, String description) {
        this.assignment = assignment;
        this.entry = entry;
        this.description = description;
    }

    public JudgeAssignment getAssignment() { return assignment; }
    public ShortlistEntry getEntry() { return entry; }
    public String getDescription() { return description; }
}
