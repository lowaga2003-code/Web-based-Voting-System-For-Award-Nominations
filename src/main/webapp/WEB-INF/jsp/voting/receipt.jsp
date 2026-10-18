<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="com.pation.awardnomination.voting.*" %>
<%@ page import="com.pation.awardnomination.category.AwardCategory" %>
<%@ page import="com.pation.awardnomination.dispute.AuditTransparencyLog" %>
<%
    BallotViews.Receipt r = (BallotViews.Receipt) request.getAttribute("receipt");
    Vote v = r.getVote();
    AwardCategory c = r.getCategory();
    AuditTransparencyLog ledger = r.getLedgerEntry();
    boolean fresh = Boolean.TRUE.equals(request.getAttribute("vtFresh"));
    String vtTitle = "Receipt " + v.getReceiptCode();
    String vtTab = "history";
%>
<%@ include file="_vtTop.jspf" %>

<a class="vt-back" href="<%= cx %>/voter/ballot"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round"><path d="M19 12H5M11 6l-6 6 6 6"/></svg>Back to my ballot</a>

<div class="vt-receipt-wrap">
  <div class="vt-receipt-shell">
    <div class="vt-sticker <%= fresh ? "pop" : "" %>" aria-hidden="true"><div>I<br>VOTED<small><%= VotingHtml.h(c == null ? "" : c.getCode()) %></small></div></div>
    <div class="vt-receipt-shadow">
    <div class="vt-receipt" aria-label="Voting receipt">
      <h1>Official voting receipt</h1>
      <div class="vt-receipt-code"><%= VotingHtml.h(v.getReceiptCode()) %></div>
      <div class="vt-receipt-sub">Keep this code. It is the only link between you and your sealed ballot.</div>
      <dl>
        <dt>Category</dt><dd><b><%= VotingHtml.h(c == null ? "Category #" + v.getCategoryId() : c.getName()) %></b></dd>
        <dt>Your choice</dt><dd><b><%= VotingHtml.h(r.getNomineeName()) %></b><% if (r.getOrganisation() != null && !r.getOrganisation().isBlank()) { %><br><%= VotingHtml.h(r.getOrganisation()) %><% } %></dd>
        <dt>Cast at</dt><dd><%= VotingHtml.stamp(v.getCastAt()) %></dd>
        <dt>Seal check</dt><dd><% if (r.isSealIntact()) { %><span class="vt-ok">&#10003; intact</span><% } else { %><span class="vt-bad">&#10007; does not match</span><% } %></dd>
        <dt>Public log</dt><dd><% if (ledger != null) { %><span class="vt-ok">&#10003; recorded <%= VotingHtml.brief(ledger.getTimestamp()) %></span><% } else { %><span class="vt-bad">not found</span><% } %></dd>
      </dl>
      <div class="vt-barcode" aria-hidden="true"><%= VotingHtml.barcode(v.getSealHash()) %></div>
      <div class="vt-hash">SHA-256 <%= VotingHtml.h(VotingHtml.shortHash(v.getSealHash())) %></div>
    </div>
    </div>
  </div>

  <div>
    <section class="vt-panel">
      <h2>Check it yourself</h2>
      <p>Every sealed ballot is written to the Module 4 transparency log. The log shows your receipt and the seal, but never who you are or whom you chose.</p>
      <ol class="vt-steps">
        <li>Open the <a href="<%= cx %>/dispute/logs">transparency log</a>.</li>
        <li>Find ticket code <code><%= VotingHtml.h(v.getReceiptCode()) %></code>.</li>
        <li>Its findings show the same seal: <code><%= VotingHtml.h(VotingHtml.shortHash(v.getSealHash())) %></code>.</li>
      </ol>
      <a class="vt-btn vt-btn-ink" href="<%= cx %>/dispute/logs">Open transparency log</a>
    </section>
    <section class="vt-panel">
      <h2>Something wrong?</h2>
      <p>If this receipt does not match what you chose, or you think someone else voted for you, raise a dispute with the Compliance Auditor.</p>
      <a class="vt-btn" href="<%= cx %>/dispute/form">Report a voting problem</a>
    </section>
  </div>
</div>

<%@ include file="_vtBottom.jspf" %>
