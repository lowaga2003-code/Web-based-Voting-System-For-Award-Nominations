package com.pation.awardnomination.category;

/** Module 2 - one "needs attention" line on the Programme Manager's control board. */
public class BoardNotice {

    private final String level;   // "warn", "info" or "done"
    private final String message;
    private final String link;    // path inside the app, may be null

    public BoardNotice(String level, String message, String link) {
        this.level = level;
        this.message = message;
        this.link = link;
    }

    public String getLevel() { return level; }
    public String getMessage() { return message; }
    public String getLink() { return link; }
}
