<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.pation.awardnomination.evaluation.*" %>
<%@ page import="com.pation.awardnomination.category.AwardCategory, com.pation.awardnomination.category.CategoryPhase" %>
<%--
  MODULE 5 - results of one category: candidates ranked by the judges' average mark, and the winner
  (one per category). Disqualified candidates are listed last without a rank.
--%>
<%
    String evTitle = "Results";
    String evNav = "results";
    List<String> categories = (List<String>) request.getAttribute("categories");
    String category = (String) request.getAttribute("category");
    List<CandidateResult> results = (List<CandidateResult>) request.getAttribute("results");
    List<Judge> panel = (List<Judge>) request.getAttribute("panel");
    if (categories == null) { categories = new ArrayList<String>(); }
    if (results == null) { results = new ArrayList<CandidateResult>(); }
    if (panel == null) { panel = new ArrayList<Judge>(); }
    AwardCategory awardCategory = (AwardCategory) request.getAttribute("awardCategory");    // [INTEGRATION v5] Module 2
    CategoryPhase votingPhase = (CategoryPhase) request.getAttribute("votingPhase");
    String votingEnds = awardCategory == null ? "" : java.time.format.DateTimeFormatter
            .ofPattern("d MMM yyyy, HH:mm", java.util.Locale.ENGLISH).format(awardCategory.getVotingEnd());
    boolean winnerLocked = awardCategory != null && votingPhase != CategoryPhase.RESULTS;   // Module 2: after voting closes
    StringBuilder panelNames = new StringBuilder();
    for (int i = 0; i < panel.size(); i++) {
        if (i > 0) panelNames.append(", ");
        panelNames.append(panel.get(i).getName());
    }
%>
<%@ include file="/WEB-INF/jsp/evaluation/_evTop.jspf" %>
<div class="ev-head">
 <div>
  <h1>Results and winners</h1>
  <p class="ev-lead">Ranked by the average of the judges who finished marking. One winner per category.</p>
 </div>
</div>
<% if (categories.isEmpty()) { %>
 <div class="ev-empty">No category yet. Start with the <a href="/evaluation/assignments">judge panel</a>, <a href="/evaluation/rubrics">rubrics</a> and <a href="/evaluation/shortlist">shortlist</a>.</div>
<% } else { %>
<nav class="ev-tabs" aria-label="Award categories">
 <% for (String c : categories) { %>
  <a href="/evaluation/results?category=<%= evUrl(c) %>"<%= c.equalsIgnoreCase(category) ? " aria-current=\"page\"" : "" %>><%= h(c) %></a>
 <% } %>
</nav>
<p class="ev-small ev-muted">Panel of <%= h(category) %>: <%= panel.isEmpty() ? "no judges yet" : h(panelNames) %>.</p>
<p class="ev-vote-info">
 <% if (awardCategory == null) { %>
  <b>Public vote:</b> "<%= h(category) %>" is not a Module 2 award category, so it has no public vote.
 <% } else if (votingPhase == CategoryPhase.RESULTS) { %>
  <b>Public vote:</b> voting closed on <%= h(votingEnds) %>. Each candidate shows the public votes beside the judges' average.
 <% } else { %>
  <b>Public vote:</b> voting <%= votingPhase == CategoryPhase.VOTING_OPEN ? "is open until" : "runs until" %> <%= h(votingEnds) %> (Module 2 timeline). The counts appear here after it closes, and the winner can be selected then.
 <% } %>
</p>

<% if (results.isEmpty()) { %>
 <div class="ev-empty">No shortlisted candidates in this category.</div>
<% } else { %>
<ol class="ev-ranking">
 <% for (CandidateResult r : results) {
        ShortlistEntry e = r.getEntry();
        String st = e.getStatus().name();
        boolean won = "WINNER".equals(st);
        boolean isOut = "DISQUALIFIED".equals(st);
        double pct = r.getAverage() == null ? 0 : r.getAverage(); %>
 <li class="ev-rank<%= won ? " won" : "" %><%= isOut ? " out" : "" %>">
  <div class="ev-rank-no" aria-label="Rank"><%= r.getRank() == 0 ? "-" : String.valueOf(r.getRank()) %></div>
  <div>
   <h3><a href="/evaluation/candidates/<%= e.getId() %>"><%= h(e.getNomineeName()) %></a></h3>
   <span class="ev-small ev-muted"><%= h(e.getOrganization()) %>, nominee #<%= e.getNomineeId() %></span>
   <% if (won) { %>&nbsp;<span class="ev-pill winner">Winner</span><% } else if (isOut) { %>&nbsp;<span class="ev-pill disqualified">Disqualified</span><% } %>
  </div>
  <div>
   <span class="ev-score-num"><%= evMark(r.getAverage()) %><small> / 100</small></span>
   <div class="ev-score-bar"><span style="width:<%= String.format(java.util.Locale.ENGLISH, "%.1f", pct) %>%"></span></div>
   <span class="ev-small ev-muted"><%= r.getCompleteSheets() %> of <%= r.getPanelSize() %> judges finished</span>
   <% if (r.getPublicVotes() != null) { %>
   <div class="ev-vote-line">Public vote <b><%= r.getPublicVotes() %></b> <span class="ev-muted">(<%= r.getVotePercent() %>%)</span></div>
   <div class="ev-vote-bar"><span style="width:<%= r.getVotePercent() %>%"></span></div>
   <% } %>
  </div>
  <div class="ev-actions">
   <% if ("SHORTLISTED".equals(st)) { %>
    <form class="ev-inline" method="post" action="/evaluation/candidates/<%= e.getId() %>/winner" onsubmit="return confirm('Select this candidate as the winner of the category?');">
     <button class="ev-btn small"<%= r.getAverage() == null ? " disabled title=\"No judge has finished marking\"" : (winnerLocked ? " disabled title=\"Public voting closes on " + h(votingEnds) + "\"" : "") %>>Select as winner</button>
    </form>
   <% } else if (won) { %>
    <form class="ev-inline" method="post" action="/evaluation/candidates/<%= e.getId() %>/revoke-winner" onsubmit="return confirm('Remove the winner status?');">
     <button class="ev-btn danger small">Revoke winner</button>
    </form>
   <% } %>
  </div>
 </li>
 <% } %>
</ol>
<% } %>
<p class="ev-note">The winner is chosen from the judges' marks, the nominee's evidence and the public vote (Voting).</p>
<% } %>
<%@ include file="/WEB-INF/jsp/evaluation/_evBottom.jspf" %>
