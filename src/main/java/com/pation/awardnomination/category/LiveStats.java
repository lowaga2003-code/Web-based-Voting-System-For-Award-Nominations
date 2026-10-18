package com.pation.awardnomination.category;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Module 2 - "Read: real-time statistics of received nominations and votes".
 * Built by CategoryStatsService; shown on the stats page and sent as JSON for live refresh.
 */
public class LiveStats {

    /** One nominee's live vote count inside a category. */
    public static class NomineeLive {
        private final Integer nomineeId;
        private final String name;
        private final String organisation;
        private final long votes;
        private final int percent;
        private final boolean leader;

        public NomineeLive(Integer nomineeId, String name, String organisation, long votes, int percent, boolean leader) {
            this.nomineeId = nomineeId;
            this.name = name;
            this.organisation = organisation;
            this.votes = votes;
            this.percent = percent;
            this.leader = leader;
        }

        public Integer getNomineeId() { return nomineeId; }
        public String getName() { return name; }
        public String getOrganisation() { return organisation; }
        public long getVotes() { return votes; }
        public int getPercent() { return percent; }
        public boolean isLeader() { return leader; }
    }

    /** One active category with its live numbers. */
    public static class CategoryLive {
        private final AwardCategory category;
        private final CategoryPhase phase;
        private final long entries;
        private final long votes;
        private final List<NomineeLive> nominees;

        public CategoryLive(AwardCategory category, CategoryPhase phase, long entries, long votes, List<NomineeLive> nominees) {
            this.category = category;
            this.phase = phase;
            this.entries = entries;
            this.votes = votes;
            this.nominees = nominees;
        }

        public AwardCategory getCategory() { return category; }
        public CategoryPhase getPhase() { return phase; }
        public long getEntries() { return entries; }
        public long getVotes() { return votes; }
        public List<NomineeLive> getNominees() { return nominees; }
    }

    LocalDateTime generatedAt;
    long nominationsTotal;
    long drafts;
    long underReview;
    long approved;
    long otherStatus;
    int activeCategories;
    final Map<CategoryPhase, Integer> byPhase = new EnumMap<>(CategoryPhase.class);
    long entriesOnBallots;
    long votesTotal;
    long distinctVoters;
    long registeredVoters;
    long[] lastHours = new long[24];
    final List<CategoryLive> categories = new ArrayList<>();

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public long getNominationsTotal() { return nominationsTotal; }
    public long getDrafts() { return drafts; }
    public long getUnderReview() { return underReview; }
    public long getApproved() { return approved; }
    public long getOtherStatus() { return otherStatus; }
    public int getActiveCategories() { return activeCategories; }
    public int countInPhase(CategoryPhase phase) { return byPhase.getOrDefault(phase, 0); }
    public long getEntriesOnBallots() { return entriesOnBallots; }
    public long getVotesTotal() { return votesTotal; }
    public long getDistinctVoters() { return distinctVoters; }
    public long getRegisteredVoters() { return registeredVoters; }
    /** Votes per hour for the last 24 hours, oldest hour first. */
    public long[] getLastHours() { return lastHours; }
    public List<CategoryLive> getCategories() { return categories; }

    /** Share of registered voters who have voted at least once (0-100). */
    public int getTurnoutPercent() {
        if (registeredVoters <= 0) return 0;
        return (int) Math.min(100, Math.round(distinctVoters * 100.0 / registeredVoters));
    }

    public long getPeakHour() {
        long max = 0;
        for (long v : lastHours) max = Math.max(max, v);
        return max;
    }

    /** Small hand-written JSON so the live refresh has no extra library dependency. */
    public String toJson() {
        StringBuilder sb = new StringBuilder(512);
        sb.append('{');
        field(sb, "generatedAt", generatedAt == null ? "" : generatedAt.toString()).append(',');
        sb.append("\"totals\":{");
        num(sb, "nominations", nominationsTotal).append(',');
        num(sb, "drafts", drafts).append(',');
        num(sb, "underReview", underReview).append(',');
        num(sb, "approved", approved).append(',');
        num(sb, "categories", activeCategories).append(',');
        num(sb, "entries", entriesOnBallots).append(',');
        num(sb, "votes", votesTotal).append(',');
        num(sb, "voters", distinctVoters).append(',');
        num(sb, "registered", registeredVoters).append(',');
        num(sb, "turnout", getTurnoutPercent()).append(',');
        num(sb, "live", countInPhase(CategoryPhase.VOTING_OPEN));
        sb.append("},\"hours\":[");
        for (int i = 0; i < lastHours.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(lastHours[i]);
        }
        sb.append("],\"categories\":[");
        for (int i = 0; i < categories.size(); i++) {
            CategoryLive c = categories.get(i);
            if (i > 0) sb.append(',');
            sb.append('{');
            num(sb, "id", c.getCategory().getId() == null ? 0 : c.getCategory().getId()).append(',');
            field(sb, "code", c.getCategory().getCode()).append(',');
            field(sb, "phase", c.getPhase().name()).append(',');
            field(sb, "phaseLabel", c.getPhase().getBoardText()).append(',');
            num(sb, "entries", c.getEntries()).append(',');
            num(sb, "votes", c.getVotes()).append(',');
            sb.append("\"nominees\":[");
            for (int j = 0; j < c.getNominees().size(); j++) {
                NomineeLive n = c.getNominees().get(j);
                if (j > 0) sb.append(',');
                sb.append('{');
                num(sb, "id", n.getNomineeId() == null ? 0 : n.getNomineeId()).append(',');
                num(sb, "votes", n.getVotes()).append(',');
                num(sb, "pct", n.getPercent()).append(',');
                sb.append("\"leader\":").append(n.isLeader());
                sb.append('}');
            }
            sb.append("]}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static StringBuilder num(StringBuilder sb, String key, long value) {
        return sb.append('"').append(key).append("\":").append(value);
    }

    private static StringBuilder field(StringBuilder sb, String key, String value) {
        sb.append('"').append(key).append("\":\"");
        String v = value == null ? "" : value;
        for (int i = 0; i < v.length(); i++) {
            char ch = v.charAt(i);
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '<': sb.append("\\u003c"); break;
                case '>': sb.append("\\u003e"); break;
                case '&': sb.append("\\u0026"); break;
                default:
                    if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
                    else sb.append(ch);
            }
        }
        return sb.append('"');
    }
}
