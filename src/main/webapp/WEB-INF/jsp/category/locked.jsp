<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, java.time.LocalDateTime" %>
<%@ page import="com.pation.awardnomination.category.*" %>
<%
    String cmTitle = "Nominations locked";
    String cmTab = "calendar";
    boolean cmPublic = true;
    String reason = (String) request.getAttribute("reason");
    LocalDateTime deadline = (LocalDateTime) request.getAttribute("deadline");
    List<AwardCategory> cats = (List<AwardCategory>) request.getAttribute("categories");
    LocalDateTime now = (LocalDateTime) request.getAttribute("now");
    boolean closed = "closed".equals(reason);
    AwardCategory upcoming = null;
    for (AwardCategory c : cats) {
        if (now.isBefore(c.getNominationStart()) && (upcoming == null || c.getNominationStart().isBefore(upcoming.getNominationStart()))) upcoming = c;
    }
%>
<%@ include file="_cmTop.jspf" %>

<div class="cm-lock">
  <div class="cm-lock-top">
    <div class="cm-status arch"><span class="cm-led"></span><span class="cm-flaps"><%= CategoryHtml.flaps(closed ? "CLOSED" : "LOCKED") %></span></div>
    <h1><%= closed ? "Nominations are closed" : "This profile is locked" %></h1>
  </div>
  <div class="cm-lock-body">
    <% if (closed) { %>
      <p>No award category is taking nominations right now, so new nomination forms cannot be submitted.</p>
      <% if (upcoming != null) { %>
        <p><strong><%= CategoryHtml.h(upcoming.getName()) %></strong> opens for nominations on <span class="cm-mono"><%= CategoryHtml.full(upcoming.getNominationStart()) %></span> (in <span data-cm-until="<%= CategoryHtml.iso(upcoming.getNominationStart()) %>"><%= CategoryHtml.span(now, upcoming.getNominationStart()) %></span>).</p>
      <% } %>
    <% } else { %>
      <p>The nomination deadline<%= deadline == null ? "" : " (<span class=\"cm-mono\">" + CategoryHtml.full(deadline) + "</span>)" %> has passed. As the award rules say, the profile was locked automatically and is now <strong>Under Review</strong> by the administrator.</p>
      <p>You can still view your profile and book an interview slot.</p>
    <% } %>
    <p style="font-size:13px;color:var(--cm-muted)">Deadlines are set by the Programme Manager in Module 2 (Award Category Management).</p>
    <div class="cm-lock-actions">
      <a class="cm-btn cm-btn-dark" href="<%= cx %>/nominee/profile">My nominee profile</a>
      <a class="cm-btn" href="<%= cx %>/timeline">Award calendar</a>
      <a class="cm-btn" href="<%= cx %>/dashboard">Main dashboard</a>
    </div>
  </div>
</div>

<%@ include file="_cmBottom.jspf" %>
