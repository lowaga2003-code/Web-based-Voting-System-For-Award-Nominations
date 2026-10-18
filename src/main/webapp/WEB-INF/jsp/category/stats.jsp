<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, java.time.LocalDateTime" %>
<%@ page import="com.pation.awardnomination.category.*" %>
<%
    String cmTitle = "Live statistics";
    String cmTab = "stats";
    boolean cmPublic = false;
    LiveStats stats = (LiveStats) request.getAttribute("stats");
    LocalDateTime now = (LocalDateTime) request.getAttribute("now");
    long nomTotal = Math.max(1, stats.getNominationsTotal());
    long[] hours = stats.getLastHours();
    long peak = Math.max(1, stats.getPeakHour());
%>
<%@ include file="_cmTop.jspf" %>

<div class="cm-head">
  <div>
    <div class="cm-kicker">Read &middot; real-time statistics</div>
    <h1>Live statistics</h1>
    <p>Nominations received (Module 3) and votes cast, refreshed every 10 seconds without reloading the page.</p>
  </div>
  <div class="cm-head-actions"><span class="cm-live-dot" data-cm-updated>LIVE &middot; <%= CategoryHtml.clock(now) %></span></div>
</div>

<div class="cm-kpis" data-cm-live-url="<%= cx %>/manager/awards/api/live">
  <div class="cm-kpi" style="--k:var(--ph-nom)"><span>Nominations received</span><strong data-cm-live="nominations"><%= stats.getNominationsTotal() %></strong><small>all statuses</small></div>
  <div class="cm-kpi" style="--k:var(--ph-prep)"><span>On ballots</span><strong data-cm-live="entries"><%= stats.getEntriesOnBallots() %></strong><small>placed by the Programme Manager</small></div>
  <div class="cm-kpi" style="--k:var(--ph-live)"><span>Votes cast</span><strong data-cm-live="votes"><%= stats.getVotesTotal() %></strong><small>active categories</small></div>
  <div class="cm-kpi" style="--k:var(--ph-done)"><span>Voters who voted</span><strong data-cm-live="voters"><%= stats.getDistinctVoters() %></strong><small>of <span data-cm-live="registered"><%= stats.getRegisteredVoters() %></span> registered</small></div>
  <div class="cm-kpi" style="--k:#06b6d4"><span>Voting live</span><strong data-cm-live="live"><%= stats.countInPhase(CategoryPhase.VOTING_OPEN) %></strong><small>categories right now</small></div>
</div>

<div class="cm-grid cm-grid-2">
  <section class="cm-card">
    <div class="cm-card-h"><h2>Nominations by status</h2><span class="cm-count">Module 3</span></div>
    <div class="cm-stack" aria-label="Nominations by status">
      <i style="width:<%= CategoryHtml.num(stats.getApproved() * 100.0 / nomTotal) %>%;background:var(--ph-live)" data-cm-stack="approved"></i>
      <i style="width:<%= CategoryHtml.num(stats.getUnderReview() * 100.0 / nomTotal) %>%;background:var(--ph-prep)" data-cm-stack="underReview"></i>
      <i style="width:<%= CategoryHtml.num(stats.getDrafts() * 100.0 / nomTotal) %>%;background:#94a3b8" data-cm-stack="drafts"></i>
    </div>
    <div class="cm-stack-legend">
      <span><i style="background:var(--ph-live)"></i>Approved <b data-cm-live="approved"><%= stats.getApproved() %></b></span>
      <span><i style="background:var(--ph-prep)"></i>Under Review <b data-cm-live="underReview"><%= stats.getUnderReview() %></b></span>
      <span><i style="background:#94a3b8"></i>Draft <b data-cm-live="drafts"><%= stats.getDrafts() %></b></span>
    </div>
    <h3 style="font-size:14px;margin:22px 0 4px">Votes in the last 24 hours</h3>
    <div class="cm-hours" aria-label="Votes per hour">
      <% for (int i = 0; i < hours.length; i++) { %><i class="<%= hours[i] == 0 ? "zero" : "" %>" data-cm-hour="<%= i %>" style="height:<%= CategoryHtml.num(hours[i] == 0 ? 2 : Math.max(4, hours[i] * 100.0 / peak)) %>%" title="<%= hours[i] %> vote(s)"></i><% } %>
    </div>
    <div class="cm-hours-axis"><span>24h ago</span><span>12h ago</span><span>now</span></div>
  </section>

  <section class="cm-card" style="text-align:center">
    <div class="cm-card-h"><h2>Turnout</h2><span class="cm-count">Module 1 voters</span></div>
    <div class="cm-gauge" data-cm-gauge style="--p:<%= stats.getTurnoutPercent() %>">
      <div><div><strong><b data-cm-live="turnout" style="font-weight:inherit"><%= stats.getTurnoutPercent() %></b>%</strong><em>have voted</em></div></div>
    </div>
    <p class="cm-sub" style="margin:0"><span data-cm-live="voters"><%= stats.getDistinctVoters() %></span> of <span data-cm-live="registered"><%= stats.getRegisteredVoters() %></span> registered voters have cast at least one vote.</p>
  </section>
</div>

<h2 style="font-size:18px;margin:26px 0 12px">Per category</h2>
<% if (stats.getCategories().isEmpty()) { %>
  <p class="cm-empty">No active categories.</p>
<% } %>
<div class="cm-grid cm-grid-even">
  <% for (LiveStats.CategoryLive cl : stats.getCategories()) {
       AwardCategory c = cl.getCategory(); %>
  <section class="cm-card">
    <div class="cm-card-h">
      <h2><a href="<%= cx %>/manager/awards/<%= c.getId() %>" style="color:inherit;text-decoration:none"><%= CategoryHtml.h(c.getName()) %></a></h2>
      <span class="cm-phase <%= cl.getPhase().getCss() %>"><%= CategoryHtml.h(cl.getPhase().getLabel()) %></span>
    </div>
    <p class="cm-sub"><span data-cm-cat-votes="<%= c.getId() %>"><%= cl.getVotes() %></span> votes &middot; <%= CategoryHtml.plural(cl.getEntries(), "nominee", "nominees") %> &middot; <%= CategoryHtml.h(CategoryHtml.nextStep(c, now)) %></p>
    <% if (cl.getNominees().isEmpty()) { %>
      <p class="cm-empty">The ballot is empty.</p>
    <% } else { %>
    <ul class="cm-lb">
      <% for (LiveStats.NomineeLive n : cl.getNominees()) { %>
      <li>
        <div class="cm-lb-row"><b><%= CategoryHtml.h(n.getName()) %></b><span><span data-cm-vote="<%= c.getId() %>:<%= n.getNomineeId() %>"><%= n.getVotes() %></span> &middot; <span data-cm-pct="<%= c.getId() %>:<%= n.getNomineeId() %>"><%= n.getPercent() %></span>%</span></div>
        <div class="cm-bar <%= n.isLeader() ? "lead" : "" %>"><i data-cm-bar="<%= c.getId() %>:<%= n.getNomineeId() %>" style="width:<%= n.getPercent() %>%"></i></div>
      </li>
      <% } %>
    </ul>
    <% } %>
  </section>
  <% } %>
</div>

<%@ include file="_cmBottom.jspf" %>
