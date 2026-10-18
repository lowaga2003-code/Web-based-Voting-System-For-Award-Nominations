package com.pation.awardnomination.policy;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MODULE 6 - small text / HTML helpers used by the Policy & Consent code and pages.
 *
 *  esc()      : escape any text before printing it in a JSP (stops cross-site scripting).
 *  sanitize() : clean the rich-text policy HTML. Only simple formatting tags survive, every attribute
 *               except a safe link address is removed, and scripts / styles / frames are dropped.
 */
public final class PolicyHtml {

    private static final Set<String> ALLOWED_TAGS = Set.of(
            "p", "br", "b", "strong", "i", "em", "u", "h2", "h3", "h4",
            "ul", "ol", "li", "a", "blockquote", "hr", "div", "span");
    private static final Set<String> VOID_TAGS = Set.of("br", "hr");
    /** These tags are removed together with everything inside them. */
    private static final Set<String> DROP_WITH_CONTENT = Set.of(
            "script", "style", "iframe", "object", "embed", "template", "noscript", "textarea", "title",
            "xmp", "noembed", "noframes", "svg", "math", "select", "head", "frameset", "frame", "applet");

    private static final Pattern TAG = Pattern.compile("<(/?)([a-zA-Z][a-zA-Z0-9]*)([^>]*)>");
    private static final Pattern HREF = Pattern.compile("(?i)\\bhref\\s*=\\s*(\"([^\"]*)\"|'([^']*)'|([^\\s>]+))");
    private static final Pattern SAFE_URL = Pattern.compile("(?i)^(https?://|mailto:|/(?!/)|#)[^\\s\"'<>`\\\\]*$");
    private static final Pattern ENTITY = Pattern.compile("&(#[0-9]{1,7}|#[xX][0-9a-fA-F]{1,6}|[a-zA-Z][a-zA-Z0-9]{1,31});");

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

    private PolicyHtml() {
    }

    /** HTML-escape text for JSP output (element content and attribute values). */
    public static String esc(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Keeps only safe formatting from the rich-text editor (see class comment). */
    public static String sanitize(String html) {
        if (html == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(html.length());
        int i = 0;
        int n = html.length();
        while (i < n) {
            char c = html.charAt(i);
            if (c == '<') {
                if (html.startsWith("<!--", i)) {                       // comment -> drop
                    int end = html.indexOf("-->", i + 4);
                    i = end < 0 ? n : end + 3;
                    continue;
                }
                if (i + 1 < n && (html.charAt(i + 1) == '!' || html.charAt(i + 1) == '?')) { // <!DOCTYPE ...>, <?xml ...?>
                    int end = html.indexOf('>', i);
                    i = end < 0 ? n : end + 1;
                    continue;
                }
                Matcher tag = TAG.matcher(html);
                tag.region(i, n);
                if (tag.lookingAt()) {
                    boolean closing = !tag.group(1).isEmpty();
                    String name = tag.group(2).toLowerCase(Locale.ROOT);
                    String attributes = tag.group(3);
                    i = tag.end();
                    if (!closing && DROP_WITH_CONTENT.contains(name)) {
                        Matcher close = Pattern.compile("</" + name + "\\s*>", Pattern.CASE_INSENSITIVE).matcher(html);
                        close.region(i, n);
                        i = close.find() ? close.end() : n;
                        continue;
                    }
                    if (!ALLOWED_TAGS.contains(name)) {
                        continue;                                           // unknown tag: drop it, keep its text
                    }
                    if (closing) {
                        if (!VOID_TAGS.contains(name)) {
                            out.append("</").append(name).append('>');
                        }
                    } else if (name.equals("a")) {
                        String href = safeHref(attributes);
                        if (href != null) {
                            out.append("<a href=\"").append(esc(href)).append("\" rel=\"noopener noreferrer\" target=\"_blank\">");
                        } else {
                            out.append("<a>");
                        }
                    } else {
                        out.append('<').append(name).append('>');           // all other attributes are removed
                    }
                    continue;
                }
                out.append("&lt;");
                i++;
                continue;
            }
            if (c == '>') {
                out.append("&gt;");
                i++;
                continue;
            }
            if (c == '&') {
                Matcher entity = ENTITY.matcher(html);
                entity.region(i, n);
                if (entity.lookingAt()) {
                    out.append(entity.group());
                    i = entity.end();
                } else {
                    out.append("&amp;");
                    i++;
                }
                continue;
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    /** Link address from an a-tag, only if it is http(s), mailto, a same-site path or a #anchor. */
    private static String safeHref(String attributes) {
        Matcher m = HREF.matcher(attributes == null ? "" : attributes);
        if (!m.find()) {
            return null;
        }
        String value = m.group(2) != null ? m.group(2) : m.group(3) != null ? m.group(3) : m.group(4);
        value = value.trim().replace("&amp;", "&");
        return SAFE_URL.matcher(value).matches() ? value : null;
    }

    /** Text without tags (for previews and checks), cut to maxLength characters. */
    public static String toPlainText(String html, int maxLength) {
        if (html == null) {
            return "";
        }
        String text = html.replaceAll("<[^>]*>", " ")
                .replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&#39;", "'").replace("&amp;", "&")
                .replaceAll("\\s+", " ").trim();
        return text.length() > maxLength ? text.substring(0, Math.max(0, maxLength - 1)).trim() + "\u2026" : text;
    }

    public static String dateTime(LocalDateTime time) {
        return time == null ? "" : DATE_TIME.format(time);
    }

    public static String date(LocalDateTime time) {
        return time == null ? "" : DATE.format(time);
    }

    /** For sentences: "1 Oct 2026 at 14:05". */
    public static String when(LocalDateTime time) {
        return time == null ? "" : DATE.format(time) + " at " + TIME.format(time);
    }

    public static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public static String limit(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }

    /** Reference codes such as CNS-000012, DDR-0004, POL-0003. */
    public static String code(String prefix, Long id, int digits) {
        if (id == null) {
            return prefix + "-new";
        }
        String number = String.valueOf(id);
        StringBuilder sb = new StringBuilder(prefix).append('-');
        for (int k = number.length(); k < digits; k++) {
            sb.append('0');
        }
        return sb.append(number).toString();
    }

    public static String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
