package com.pation.awardnomination.voting;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Voting - small helpers used by the voting JSP pages. */
public final class VotingHtml {

    private static final DateTimeFormatter FULL = DateTimeFormatter.ofPattern("EEE d MMM yyyy, HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("d MMM yyyy  HH:mm:ss", Locale.ENGLISH);

    private VotingHtml() { }

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

    public static String full(LocalDateTime t) { return t == null ? "-" : FULL.format(t); }
    public static String brief(LocalDateTime t) { return t == null ? "-" : SHORT.format(t); }
    public static String stamp(LocalDateTime t) { return t == null ? "-" : STAMP.format(t); }
    public static String iso(LocalDateTime t) { return t == null ? "" : t.withNano(0).toString(); }

    /** "4d 11h", "3h 20m", "12m" */
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

    /** First "max" characters of a text, cut at a word boundary. */
    public static String excerpt(String text, int max) {
        if (text == null) return "";
        String t = text.trim().replaceAll("\\s+", " ");
        if (t.length() <= max) return t;
        int cut = t.lastIndexOf(' ', max);
        if (cut < max / 2) cut = max;
        return t.substring(0, cut).trim() + "...";
    }

    /** Up to two initials for the nominee badge, e.g. "Nimal Perera" gives "NP". */
    public static String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty() && Character.isLetterOrDigit(p.charAt(0))) sb.append(Character.toUpperCase(p.charAt(0)));
            if (sb.length() == 2) break;
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    /** One of six badge colours, chosen from the name so it is stable between visits. */
    public static int tone(String name) {
        return name == null ? 0 : Math.abs(name.hashCode() % 6);
    }

    /** Decorative bar code for the receipt, drawn from the seal hash (pure HTML spans). */
    public static String barcode(String hash) {
        if (hash == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(hash.length(), 48); i++) {
            int v = Character.digit(hash.charAt(i), 16);
            if (v < 0) v = 0;
            int w = 1 + (v % 3);
            sb.append("<i style=\"width:").append(w).append("px;margin-right:").append(1 + (v / 6)).append("px\"></i>");
        }
        return sb.toString();
    }

    /** "3fa9c2...91be" */
    public static String shortHash(String hash) {
        if (hash == null || hash.length() < 16) return hash == null ? "" : hash;
        return hash.substring(0, 10) + "..." + hash.substring(hash.length() - 6);
    }
}
