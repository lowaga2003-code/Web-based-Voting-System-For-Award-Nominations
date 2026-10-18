<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, java.time.LocalDateTime" %>
<%@ page import="com.pation.awardnomination.voting.*" %>
<%@ page import="com.pation.awardnomination.category.*" %>
<%
    BallotViews.Page b = (BallotViews.Page) request.getAttribute("ballot");
    LocalDateTime now = (LocalDateTime) request.getAttribute("now");
    AwardCategory c = b.getCategory();
    CategoryPhase ph = b.getPhase();
    String vtTitle = c.getName();
    String vtTab = "ballot";
    boolean open = b.isOpen();
    Integer mine = b.getMyVote() == null ? null : b.getMyVote().getNomineeId();
%>
<%@ include file="_vtTop.jspf" %>

<a class="vt-back" href="<%= cx %>/voter/ballot"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round"><path d="M19 12H5M11 6l-6 6 6 6"/></svg>All categories</a>

<div class="vt-ballot-head">
  <div>
    <span class="vt-chip code"><%= VotingHtml.h(c.getCode()) %></span>
    <h1><%= VotingHtml.h(c.getName()) %></h1>
    <p><%= VotingHtml.h(c.getDescription()) %></p>
    <% if (!b.getRules().isEmpty()) { %>
    <div class="vt-rules" aria-label="Eligibility rules"><% for (EligibilityRule r : b.getRules()) { %><span>&#10003; <%= VotingHtml.h(r.describe()) %></span><% } %></div>
    <% } %>
  </div>
  <% if (ph == CategoryPhase.VOTING_OPEN) { %>
  <div class="vt-clock"><span>Voting closes in</span><b data-vt-until="<%= VotingHtml.iso(c.getVotingEnd()) %>" data-vt-long="1"><%= VotingHtml.span(now, c.getVotingEnd()) %></b></div>
  <% } else if (ph == CategoryPhase.RESULTS) { %>
  <div class="vt-clock"><span>Voting closed</span><b><%= VotingHtml.brief(c.getVotingEnd()) %></b></div>
  <% } else { %>
  <div class="vt-clock"><span>Voting opens in</span><b data-vt-until="<%= VotingHtml.iso(c.getVotingStart()) %>" data-vt-long="1"><%= VotingHtml.span(now, c.getVotingStart()) %></b></div>
  <% } %>
</div>

<% if (b.getMyVote() != null) { %>
  <div class="vt-banner voted">
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round"><rect x="3" y="3" width="18" height="18" rx="4"/><path d="M7.5 12.5l3 3 6-6.5"/></svg>
    <div><b>You voted for <%= VotingHtml.h(b.getMyChoice()) %>.</b>Cast <%= VotingHtml.full(b.getMyVote().getCastAt()) %>. Your ballot is sealed and cannot be changed.</div>
    <a class="vt-btn" href="<%= cx %>/voter/ballot/receipt/<%= VotingHtml.h(b.getMyVote().getReceiptCode()) %>">Receipt <%= VotingHtml.h(b.getMyVote().getReceiptCode()) %></a>
  </div>
<% } else if (ph == CategoryPhase.RESULTS) { %>
  <div class="vt-banner closed"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"><circle cx="12" cy="12" r="9"/><path d="M8 12h8"/></svg><div><b>Voting has closed.</b>You did not vote in this category.</div><a class="vt-btn" href="<%= cx %>/voter/ballot/results">See results</a></div>
<% } else if (ph != CategoryPhase.VOTING_OPEN) { %>
  <div class="vt-banner wait"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"><circle cx="12" cy="13" r="8"/><path d="M12 9v4l2.5 2.5"/></svg><div><b>Voting opens on <%= VotingHtml.full(c.getVotingStart()) %>.</b>These are the nominees placed on the ballot so far. Come back when voting opens.</div></div>
<% } else if (b.getOptions().isEmpty()) { %>
  <div class="vt-banner wait"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16.5h.01"/></svg><div><b>The ballot is not ready yet.</b>No nominees have been placed on this ballot. Please check again later.</div></div>
<% } %>

<% if (b.getOptions().isEmpty()) { %>
  <div class="vt-empty"><b>No nominees yet</b>The Programme Manager places eligible nominees on the ballot before voting.</div>
<% } else { %>
<form method="post" action="<%= cx %>/voter/ballot/<%= c.getId() %>" data-vt-ballot>
  <div class="vt-options" role="radiogroup" aria-label="Nominees">
    <% for (BallotViews.Option o : b.getOptions()) {
         boolean isMine = mine != null && mine.equals(o.getNomineeId());
         String bio = o.getBiography() == null ? "" : o.getBiography(); %>
    <label class="vt-option <%= open ? "" : "is-locked" %> <%= isMine ? "is-mine" : "" %>">
      <input type="radio" name="nomineeId" value="<%= o.getNomineeId() %>" <%= open ? "required" : "disabled" %> data-vt-name="<%= VotingHtml.h(o.getName()) %>">
      <span class="vt-option-card">
        <span class="vt-avatar tone-<%= VotingHtml.tone(o.getName()) %>" aria-hidden="true"><%= VotingHtml.h(VotingHtml.initials(o.getName())) %></span>
        <span class="vt-option-text">
          <b><%= VotingHtml.h(o.getName()) %></b>
          <small><%= VotingHtml.h(o.getOrganisation() == null || o.getOrganisation().isBlank() ? "Independent" : o.getOrganisation()) %></small>
          <% if (!bio.isBlank()) { %>
            <p><%= VotingHtml.h(VotingHtml.excerpt(bio, 140)) %></p>
            <% if (bio.length() > 140) { %><details><summary>Read full profile</summary><p><%= VotingHtml.h(bio) %></p></details><% } %>
          <% } %>
          <span class="vt-badges">
            <% if (o.isEvidenceOnFile()) { %><span class="vt-badge">Evidence on file</span><% } %>
            <% if (isMine) { %><span class="vt-badge mine">Your vote</span><% } %>
          </span>
        </span>
        <span class="vt-tickbox" aria-hidden="true"><svg viewBox="0 0 34 34"><path d="M7 18l6.5 6.5L27 10"/></svg></span>
      </span>
    </label>
    <% } %>
  </div>

  <% if (open) { %>
  <div class="vt-castbar">
    <div class="vt-castbar-choice"><span>Your choice</span><b data-vt-choice>Nobody selected yet</b></div>
    <label class="vt-confirm"><input type="checkbox" name="confirm" value="yes" required> I understand I can vote only once in this category and cannot change it.</label>
    <button class="vt-btn vt-btn-pink vt-btn-lg" type="submit">Cast my vote</button>
  </div>
  <% } %>
</form>

<dialog class="vt-dialog" data-vt-dialog>
  <div class="vt-dialog-in">
    <h2>Cast this vote?</h2>
    <p>Your ballot for <b><%= VotingHtml.h(c.getName()) %></b> will be sealed. You will get a receipt code.</p>
    <div class="vt-dialog-pick"><span class="vt-mono" style="font-size:12px">YOUR CHOICE</span><b data-vt-dialog-name>-</b></div>
    <div class="vt-dialog-actions">
      <button class="vt-btn" type="button" data-vt-cancel>Go back</button>
      <button class="vt-btn vt-btn-pink" type="button" data-vt-go>Yes, seal my vote</button>
    </div>
  </div>
</dialog>
<% } %>

<%@ include file="_vtBottom.jspf" %>
