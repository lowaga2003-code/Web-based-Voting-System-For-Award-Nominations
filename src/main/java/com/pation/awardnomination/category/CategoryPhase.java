package com.pation.awardnomination.category;

/**
 * Module 2 - where a category is on its timeline. The phase is never stored by hand:
 * it is worked out from the four dates (see AwardCategory.phaseAt), which is what makes
 * Module 2 the "time engine" the other modules follow.
 */
public enum CategoryPhase {
    SCHEDULED("Scheduled", "SCHEDULED", "sched", "Nominations have not opened yet."),
    NOMINATIONS_OPEN("Nominations open", "NOMINATING", "nom", "Nominees can submit and edit their profiles (Module 3)."),
    BALLOT_PREP("Ballot preparation", "BALLOT PREP", "prep", "Nominations are closed and nominee profiles are locked. The ballot is prepared."),
    VOTING_OPEN("Voting live", "VOTING LIVE", "live", "Registered voters can cast one vote in this category."),
    RESULTS("Voting closed", "RESULTS IN", "done", "Voting has ended. Results go to the Selection Committee (Module 5)."),
    ARCHIVED("Archived", "ARCHIVED", "arch", "Cancelled by the Programme Manager. Kept for the audit trail.");

    private final String label;
    private final String boardText;
    private final String css;
    private final String help;

    CategoryPhase(String label, String boardText, String css, String help) {
        this.label = label;
        this.boardText = boardText;
        this.css = css;
        this.help = help;
    }

    public String getLabel() { return label; }
    /** Upper-case text shown on the departure-board style status tiles. */
    public String getBoardText() { return boardText; }
    /** Short CSS modifier used by the Module 2 and Voting pages. */
    public String getCss() { return css; }
    public String getHelp() { return help; }
}
