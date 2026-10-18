<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.pation.awardnomination.voting.*" %>
<%
    String vtTitle = "My receipts";
    String vtTab = "history";
    List<BallotViews.Receipt> receipts = (List<BallotViews.Receipt>) request.getAttribute("receipts");
    if (receipts == null) receipts = new ArrayList<BallotViews.Receipt>();
%>
<%@ include file="_vtTop.jspf" %>

<section class="vt-hero" style="grid-template-columns:minmax(0,1fr)">
  <div>
    <h1>My <span class="vt-mark">receipts</span></h1>
    <p>One receipt for every ballot you have sealed. Only you can see your choices here.</p>
  </div>
</section>

<% if (receipts.isEmpty()) { %>
  <div class="vt-empty"><b>No votes yet</b>When you cast a vote, its receipt appears here. <a href="<%= cx %>/voter/ballot">Go to my ballot</a></div>
<% } else { %>
<div class="vt-tickets">
  <% for (BallotViews.Receipt r : receipts) { %>
  <article class="vt-ticket">
    <div class="vt-ticket-main">
      <b><%= VotingHtml.h(r.getCategory() == null ? "Category #" + r.getVote().getCategoryId() : r.getCategory().getName()) %></b>
      <span>Voted for <strong><%= VotingHtml.h(r.getNomineeName()) %></strong></span>
      <small><%= VotingHtml.stamp(r.getVote().getCastAt()) %> &middot; <%= r.isSealIntact() ? "seal intact" : "seal mismatch" %></small>
    </div>
    <a class="vt-ticket-stub" href="<%= cx %>/voter/ballot/receipt/<%= VotingHtml.h(r.getVote().getReceiptCode()) %>">
      <span><%= VotingHtml.h(r.getVote().getReceiptCode()) %></span>
      <b>Open &rarr;</b>
    </a>
  </article>
  <% } %>
</div>
<% } %>

<%@ include file="_vtBottom.jspf" %>
