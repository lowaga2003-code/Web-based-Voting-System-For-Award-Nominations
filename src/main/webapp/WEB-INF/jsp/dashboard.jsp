<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.pation.awardnomination.dashboard.DashboardModule" %>
<%--
  [INTEGRATION] Main dashboard (system hub) - everyone lands here after the main login.
  One card per member's module. Admin sees every module; other roles see only their own part.
  Data comes from com.pation.awardnomination.dashboard.DashboardController.
--%>
<%!
    private static String esc(Object value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : String.valueOf(value).toCharArray()) {
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

    private static String num(Object value) {
        return value == null ? "0" : String.valueOf(value);
    }

    /** Outline icons (same icon family the admin member used in his sidebar). */
    private static String iconPath(String key) {
        if (key == null) key = "";
        switch (key) {
            case "users": return "M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z";
            case "category": return "M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z";
            case "nominee": return "M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z";
            case "audit": return "M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z";
            case "evaluation": return "M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z";
            case "policy": return "M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z";
            case "voting": return "M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z";
            default: return "M4 6a2 2 0 012-2h12a2 2 0 012 2v12a2 2 0 01-2 2H6a2 2 0 01-2-2V6z";
        }
    }

    /** HTML for one module card. */
    private static String cardHtml(DashboardModule m) {
        boolean live = m.isConnected();
        StringBuilder sb = new StringBuilder();
        sb.append("<article class=\"h-card mod-").append(esc(m.getKey())).append(live ? "" : " is-soon").append("\">");
        sb.append("<div class=\"h-card-top\">");
        sb.append("<span class=\"h-icon\" aria-hidden=\"true\"><svg fill=\"none\" stroke=\"currentColor\" viewBox=\"0 0 24 24\">")
          .append("<path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"1.8\" d=\"").append(iconPath(m.getKey())).append("\"/></svg></span>");
        sb.append("<span class=\"h-code\">").append(esc(m.getCode())).append("</span>");
        sb.append(live ? "<span class=\"h-chip live\">Connected</span>" : "<span class=\"h-chip soon\">Coming soon</span>");
        sb.append("</div>");
        sb.append("<h3>").append(esc(m.getTitle())).append("</h3>");
        sb.append("<p class=\"h-actor\">").append(esc(m.getActor())).append("</p>");
        sb.append("<p class=\"h-desc\">").append(esc(m.getDescription())).append("</p>");
        sb.append("<div class=\"h-actions\">");
        if (live) {
            boolean first = true;
            for (DashboardModule.Link link : m.getLinks()) {
                sb.append("<a class=\"h-btn").append(first ? "" : " ghost").append("\" href=\"").append(esc(link.getUrl())).append("\">")
                  .append(esc(link.getLabel()));
                if (first) {
                    sb.append("<svg fill=\"none\" stroke=\"currentColor\" viewBox=\"0 0 24 24\" aria-hidden=\"true\">")
                      .append("<path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2.2\" d=\"M13 7l5 5m0 0l-5 5m5-5H6\"/></svg>");
                }
                sb.append("</a>");
                first = false;
            }
        } else {
            sb.append("<span class=\"h-soon-note\">Opens here once this member's part is merged.</span>");
        }
        sb.append("</div></article>");
        return sb.toString();
    }
%>
<%
    String displayName = (String) request.getAttribute("displayName");
    String email = (String) request.getAttribute("email");
    String roleLabel = (String) request.getAttribute("roleLabel");
    boolean isAdmin = Boolean.TRUE.equals(request.getAttribute("isAdmin"));

    List<DashboardModule> modules = (List<DashboardModule>) request.getAttribute("modules");
    if (modules == null) modules = new ArrayList<DashboardModule>();
    List<DashboardModule> connected = new ArrayList<DashboardModule>();
    List<DashboardModule> waiting = new ArrayList<DashboardModule>();
    for (DashboardModule m : modules) {
        if (m.isConnected()) connected.add(m); else waiting.add(m);
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard · Award Nomination System</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/dashboard.css">
</head>
<body class="hub">

<header class="h-top">
    <a class="h-brand" href="/dashboard">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.2"/>
            <path d="M12 6.5L13.2 9.9L16.8 10.2L14 12.5L14.9 16L12 14.1L9.1 16L10 12.5L7.2 10.2L10.8 9.9Z" fill="currentColor"/>
        </svg>
        <span>Award Nomination System</span>
    </a>
    <div class="h-user">
        <div class="h-user-text">
            <strong><%= esc(displayName) %></strong>
            <span><%= esc(email) %></span>
        </div>
        <a class="h-logout" href="/logout">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/></svg>
            Logout
        </a>
    </div>
</header>

<main class="h-page">
    <section class="h-hero">
        <p class="h-eyebrow">Main dashboard</p>
        <h1>Welcome back, <%= esc(displayName) %></h1>
        <p>Signed in as <span class="h-role"><%= esc(roleLabel) %></span></p>
    </section>

    <% if (isAdmin) { %>
    <section class="h-stats" aria-label="System overview">
        <div class="h-stat">
            <span class="h-stat-label">Users</span>
            <span class="h-stat-value"><%= num(request.getAttribute("totalUsers")) %></span>
            <span class="h-stat-sub"><b><%= num(request.getAttribute("activeUsers")) %></b> active</span>
        </div>
        <div class="h-stat">
            <span class="h-stat-label">Nominees</span>
            <span class="h-stat-value"><%= num(request.getAttribute("totalNominees")) %></span>
            <span class="h-stat-sub"><b><%= num(request.getAttribute("approvedNominees")) %></b> approved</span>
        </div>
        <div class="h-stat">
            <span class="h-stat-label">Under review</span>
            <span class="h-stat-value"><%= num(request.getAttribute("reviewNominees")) %></span>
            <span class="h-stat-sub"><b><%= num(request.getAttribute("draftNominees")) %></b> still in draft</span>
        </div>
        <div class="h-stat">
            <span class="h-stat-label">Interview slots</span>
            <span class="h-stat-value"><%= num(request.getAttribute("bookedSlots")) %><small> / <%= num(request.getAttribute("totalSlots")) %></small></span>
            <span class="h-stat-sub">booked</span>
        </div>
    </section>

    <section class="h-section" aria-labelledby="connectedTitle">
        <div class="h-section-head">
            <h2 id="connectedTitle">Connected modules</h2>
            <p>Each card opens that member's own pages.</p>
        </div>
        <div class="h-grid">
            <% for (DashboardModule m : connected) { %>
            <%= cardHtml(m) %>
            <% } %>
        </div>
    </section>

    <% if (!waiting.isEmpty()) { %>
    <section class="h-section" aria-labelledby="waitingTitle">
        <div class="h-section-head">
            <h2 id="waitingTitle">Waiting to be merged</h2>
            <p><%= waiting.size() %> <%= waiting.size() == 1 ? "part" : "parts" %> · they switch on here when added to the project</p>
        </div>
        <div class="h-grid">
            <% for (DashboardModule m : waiting) { %>
            <%= cardHtml(m) %>
            <% } %>
        </div>
    </section>
    <% } %>

    <% } else { %>
    <section class="h-section" aria-labelledby="workspaceTitle">
        <div class="h-section-head">
            <h2 id="workspaceTitle">Your workspace</h2>
            <p>Pages available for your role.</p>
        </div>
        <% if (modules.isEmpty()) { %>
        <p class="h-soon-note">No module is linked to your role yet.</p>
        <% } else { %>
        <div class="h-grid">
            <% for (DashboardModule m : modules) { %>
            <%= cardHtml(m) %>
            <% } %>
        </div>
        <% } %>
    </section>
    <% } %>
</main>

</body>
</html>
