<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, java.time.LocalDateTime" %>
<%@ page import="com.pation.awardnomination.voting.*" %>
<%@ page import="com.pation.awardnomination.category.AwardCategory" %>
<%
    String vtTitle = "Results";
    String vtTab = "results";
    List<BallotViews.Result> results = (List<BallotViews.Result>) request.getAttribute("results");
    LocalDateTime now = (LocalDateTime) request.getAttribute("now");
%>
<%@ include file="_vtTop.jspf" %>

<section class="vt-hero" style="grid-template-columns:minmax(0,1fr)">
  <div>
    <h1>Public vote <span class="vt-mark">results</span></h1>
    <p>Counts are published only after a category's voting window closes, so nobody is influenced while voting is still open.</p>
  </div>
</section>

<% if (results.isEmpty()) { %>
  <div class="vt-empty"><b>Nothing to show yet</b>Results appear here once voting has started in a category.</div>
<% } else { %>
<div class="vt-results">
  <% for (BallotViews.Result res : results) {
       AwardCategory c = res.getCategory(); %>
  <article class="vt-result">
    <div class="vt-band <%= res.isPublished() ? "results" : "open" %>"><span><%= res.isPublished() ? "Voting closed" : "Voting in progress" %></span><span><%= VotingHtml.h(c.getCode()) %></span></div>
    <div class="vt-result-body">
      <h2><%= VotingHtml.h(c.getName()) %></h2>
      <% if (res.isPublished()) { %>
        <div class="vt-result-total"><%= res.getTotal() %> vote<%= res.getTotal() == 1 ? "" : "s" %> &middot; closed <%= VotingHtml.brief(c.getVotingEnd()) %></div>
        <% if (res.getRows().isEmpty()) { %><p>No nominees were on this ballot.</p><% } %>
        <% for (BallotViews.Row row : res.getRows()) { %>
        <div class="vt-row <%= row.isLeader() ? "lead" : "" %> <%= row.getVotes() == 0 ? "zero" : "" %>">
          <div class="vt-row-top">
            <b><%= VotingHtml.h(row.getName()) %><% if (row.isLeader()) { %><span class="vt-leader">&#9733; Public vote leader</span><% } %></b>
            <span><%= row.getVotes() %> &middot; <%= row.getPercent() %>%</span>
          </div>
          <div class="vt-meter"><i style="width:<%= row.getPercent() %>%"></i></div>
        </div>
        <% } %>
      <% } else { %>
        <div class="vt-sealed">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></svg>
          <div><b>Sealed until <%= VotingHtml.full(c.getVotingEnd()) %></b>Results open in <span data-vt-until="<%= VotingHtml.iso(c.getVotingEnd()) %>"><%= VotingHtml.span(now, c.getVotingEnd()) %></span>.</div>
        </div>
      <% } %>
    </div>
  </article>
  <% } %>
</div>
<% } %>

<div class="vt-note"><b>Public votes are one part of the decision.</b> The Selection Committee (Module 5) chooses each winner using the judges' scores, the nominees' evidence and these public vote counts.</div>

<%@ include file="_vtBottom.jspf" %>
