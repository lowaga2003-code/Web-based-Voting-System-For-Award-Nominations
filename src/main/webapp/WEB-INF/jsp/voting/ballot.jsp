<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, java.time.LocalDateTime" %>
<%@ page import="com.pation.awardnomination.voting.*" %>
<%@ page import="com.pation.awardnomination.category.*" %>
<%
    String vtTitle = "My ballot";
    String vtTab = "ballot";
    List<BallotViews.Card> cards = (List<BallotViews.Card>) request.getAttribute("cards");
    if (cards == null) cards = new ArrayList<BallotViews.Card>();
    LocalDateTime now = (LocalDateTime) request.getAttribute("now");
    int voted = 0;
    int open = 0;
    for (BallotViews.Card k : cards) {
        if (k.isVoted()) voted++;
        if (k.isCanVote()) open++;
    }
%>
<%@ include file="_vtTop.jspf" %>

<section class="vt-hero">
  <div>
    <h1><% if (vtVoter != null) { %>Hi <%= VotingHtml.h(vtVoter.getUsername()) %>,<br><% } %><% if (cards.isEmpty()) { %>your <span class="vt-mark">ballot</span> opens soon.<% } else { %>your <span class="vt-mark">ballot</span> is ready.<% } %></h1>
    <p>Pick one nominee in each award category. You can vote <b>once per category</b> and a vote cannot be changed, so take your time.</p>
  </div>
  <div class="vt-progress" aria-label="Your progress">
    <div class="vt-progress-top"><b><%= voted %> / <%= cards.size() %></b><span><%= open == 0 ? "nothing waiting for you" : open + " ballot" + (open == 1 ? "" : "s") + " open for you" %></span></div>
    <div class="vt-stamps">
      <% int i = 0; for (BallotViews.Card k : cards) { i++; %>
        <span class="vt-stamp <%= k.isVoted() ? "on" : "" %>" title="<%= VotingHtml.h(k.getCategory().getName()) %>"><% if (k.isVoted()) { %><svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="3.2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12.5l4.5 4.5L19 7"/></svg><% } else { %><%= i %><% } %></span>
      <% } %>
    </div>
  </div>
</section>

<% if (cards.isEmpty()) { %>
  <div class="vt-empty"><b>No award categories yet</b>The Programme Manager has not opened any categories. Check the award calendar for dates.</div>
<% } else { %>
<div class="vt-cards">
  <% for (BallotViews.Card k : cards) {
       AwardCategory c = k.getCategory();
       CategoryPhase ph = k.getPhase();
       String band;
       String bandText;
       String bandRight = "";
       if (k.isVoted()) { band = "voted"; bandText = "&#10003; You voted"; bandRight = VotingHtml.h(k.getMyVote().getReceiptCode()); }
       else if (k.isCanVote()) { band = "open"; bandText = "Voting open"; bandRight = "closes in <b data-vt-until=\"" + VotingHtml.iso(c.getVotingEnd()) + "\">" + VotingHtml.span(now, c.getVotingEnd()) + "</b>"; }
       else if (ph == CategoryPhase.VOTING_OPEN) { band = "idle"; bandText = "Ballot not ready"; }
       else if (ph == CategoryPhase.RESULTS) { band = "results"; bandText = "Voting closed"; bandRight = "results out"; }
       else { band = "soon"; bandText = "Voting opens"; bandRight = "in <b data-vt-until=\"" + VotingHtml.iso(c.getVotingStart()) + "\">" + VotingHtml.span(now, c.getVotingStart()) + "</b>"; } %>
  <article class="vt-card">
    <div class="vt-band <%= band %>"><span><%= bandText %></span><span><%= bandRight %></span></div>
    <div class="vt-card-body">
      <h2><%= VotingHtml.h(c.getName()) %></h2>
      <p><%= VotingHtml.h(VotingHtml.excerpt(c.getDescription(), 150)) %></p>
      <div class="vt-meta">
        <span class="vt-chip"><%= k.getNominees() %> nominee<%= k.getNominees() == 1 ? "" : "s" %></span>
        <span class="vt-chip"><%= ph == CategoryPhase.RESULTS ? "Closed " + VotingHtml.brief(c.getVotingEnd()) : (ph == CategoryPhase.VOTING_OPEN ? "Until " + VotingHtml.brief(c.getVotingEnd()) : "From " + VotingHtml.brief(c.getVotingStart())) %></span>
      </div>
      <div class="vt-card-actions">
        <% if (k.isCanVote()) { %>
          <a class="vt-btn vt-btn-pink" href="<%= cx %>/voter/ballot/<%= c.getId() %>">Vote now <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round"><path d="M5 12h14M13 6l6 6-6 6"/></svg></a>
        <% } else if (k.isVoted()) { %>
          <a class="vt-btn" href="<%= cx %>/voter/ballot/receipt/<%= VotingHtml.h(k.getMyVote().getReceiptCode()) %>">View receipt</a>
          <a class="vt-btn" href="<%= cx %>/voter/ballot/<%= c.getId() %>">See ballot</a>
        <% } else if (ph == CategoryPhase.RESULTS) { %>
          <a class="vt-btn" href="<%= cx %>/voter/ballot/results">See results</a>
        <% } else { %>
          <a class="vt-btn" href="<%= cx %>/voter/ballot/<%= c.getId() %>">Preview nominees</a>
        <% } %>
      </div>
    </div>
  </article>
  <% } %>
</div>
<% } %>

<%@ include file="_vtBottom.jspf" %>
