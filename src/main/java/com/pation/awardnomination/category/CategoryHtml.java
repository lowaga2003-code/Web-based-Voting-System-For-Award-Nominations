package com.pation.awardnomination.category;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Module 2 - small helpers used by the JSP pages (escaping, dates, countdowns, timeline scale). */
public final class CategoryHtml {

    private static final DateTimeFormatter FULL = DateTimeFormatter.ofPattern("EEE d MMM yyyy, HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

    private CategoryHtml() { }

    /** Escapes text for HTML content and attribute values. */
    public static String h(Object value) {
        if (value == null) return "";
        String s = String.valueOf(value);
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Shortens long text for lists, e.g. ledger remarks on the board. */
    public static String cut(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max - 3).trim() + "..." : text;
    }

    public static String full(LocalDateTime t) { return t == null ? "-" : FULL.format(t); }
    public static String brief(LocalDateTime t) { return t == null ? "-" : SHORT.format(t); }
    public static String day(LocalDateTime t) { return t == null ? "-" : DAY.format(t); }
    public static String clock(LocalDateTime t) { return t == null ? "" : CLOCK.format(t); }

    /** ISO text for data-* attributes read by the countdown script. */
    public static String iso(LocalDateTime t) { return t == null ? "" : t.withNano(0).toString(); }

    /** Length of time between two moments, e.g. "4d 11h", "3h 20m", "12m". */
    public static String span(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) return "-";
        long minutes = Math.abs(Duration.between(from, to).toMinutes());
        long days = minutes / (60 * 24);
        long hours = (minutes / 60) % 24;
        long mins = minutes % 60;
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + mins + "m";
        return Math.max(mins, 1) + "m";
    }

    /** Countdown text for the next milestone of a category, e.g. "Voting closes in 4d 11h". */
    public static String nextStep(AwardCategory c, LocalDateTime now) {
        LocalDateTime next = c.nextMilestone(now);
        if (next == null) {
            return c.isArchived() ? "Archived" : "Voting closed " + span(c.getVotingEnd(), now) + " ago";
        }
        return c.nextMilestoneLabel(now) + " in " + span(now, next);
    }

    public static int percent(long part, long total) {
        if (total <= 0) return 0;
        return (int) Math.round(part * 100.0 / total);
    }

    /** Number for a CSS value, always with a dot ("12.50"), whatever the server locale is. */
    public static String num(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    /**
     * How far along the four milestones a category is, 0-100, for the progress line on the
     * detail page (the four stops are evenly spaced, the fill moves with time between them).
     */
    public static double progress(AwardCategory c, LocalDateTime now) {
        LocalDateTime[] stops = {c.getNominationStart(), c.getNominationEnd(), c.getVotingStart(), c.getVotingEnd()};
        if (now.isBefore(stops[0])) return 0;
        if (!now.isBefore(stops[3])) return 100;
        for (int i = 0; i < 3; i++) {
            if (!now.isBefore(stops[i]) && now.isBefore(stops[i + 1])) {
                long whole = Duration.between(stops[i], stops[i + 1]).toMinutes();
                double part = whole <= 0 ? 1 : Duration.between(stops[i], now).toMinutes() / (double) whole;
                return (i + part) / 3.0 * 100.0;
            }
        }
        return 100;
    }

    /** Index (0-3) of the next milestone, or 4 when all have passed. */
    public static int nextStop(AwardCategory c, LocalDateTime now) {
        LocalDateTime[] stops = {c.getNominationStart(), c.getNominationEnd(), c.getVotingStart(), c.getVotingEnd()};
        for (int i = 0; i < 4; i++) if (now.isBefore(stops[i])) return i;
        return 4;
    }

    public static String plural(long n, String one, String many) {
        return n + " " + (n == 1 ? one : many);
    }

    /** Departure-board tiles: one span per character. */
    public static String flaps(String text) {
        StringBuilder sb = new StringBuilder();
        String t = text == null ? "" : text.toUpperCase(Locale.ENGLISH);
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == ' ') sb.append("<span class=\"cm-flap cm-flap-gap\"></span>");
            else sb.append("<span class=\"cm-flap\">").append(h(String.valueOf(c))).append("</span>");
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ season timeline scale

    /** Maps dates to percentages across a season so the timeline bars can be drawn with CSS. */
    public static class Scale {
        private final LocalDateTime start;
        private final LocalDateTime end;
        private final long totalMinutes;

        Scale(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
            this.totalMinutes = Math.max(1, Duration.between(start, end).toMinutes());
        }

        public double pos(LocalDateTime t) {
            if (t == null) return 0;
            double p = Duration.between(start, t).toMinutes() * 100.0 / totalMinutes;
            return Math.max(0, Math.min(100, p));
        }

        public double width(LocalDateTime from, LocalDateTime to) {
            return Math.max(0.6, pos(to) - pos(from));
        }

        public boolean contains(LocalDateTime t) {
            return t != null && !t.isBefore(start) && !t.isAfter(end);
        }

        /** First day of every month inside the scale, for the axis labels. */
        public List<LocalDateTime> monthTicks() {
            List<LocalDateTime> ticks = new ArrayList<>();
            LocalDateTime t = start.withDayOfMonth(1).toLocalDate().atStartOfDay().plusMonths(1);
            while (!t.isAfter(end) && ticks.size() < 24) {
                ticks.add(t);
                t = t.plusMonths(1);
            }
            return ticks;
        }

        /** Week ticks (every 7 days from the start) when the season is short. */
        public List<LocalDateTime> weekTicks() {
            List<LocalDateTime> ticks = new ArrayList<>();
            LocalDateTime t = start.toLocalDate().atStartOfDay().plusDays(7);
            while (!t.isAfter(end) && ticks.size() < 30) {
                ticks.add(t);
                t = t.plusDays(7);
            }
            return ticks;
        }

        public String monthLabel(LocalDateTime t) { return MONTH.format(t); }
        public String dayLabel(LocalDateTime t) { return DAY.format(t); }
        public long days() { return Math.max(1, Duration.between(start, end).toDays()); }
        public LocalDateTime getStart() { return start; }
        public LocalDateTime getEnd() { return end; }
    }

    /** Scale covering every category on the list plus "now", with a little padding. */
    public static Scale scaleFor(List<AwardCategory> categories, LocalDateTime now) {
        LocalDateTime min = now;
        LocalDateTime max = now;
        for (AwardCategory c : categories) {
            if (c.getNominationStart() != null && c.getNominationStart().isBefore(min)) min = c.getNominationStart();
            if (c.getVotingEnd() != null && c.getVotingEnd().isAfter(max)) max = c.getVotingEnd();
        }
        long pad = Math.max(12 * 60, Duration.between(min, max).toMinutes() / 25);
        return new Scale(min.minusMinutes(pad), max.plusMinutes(pad));
    }
}
