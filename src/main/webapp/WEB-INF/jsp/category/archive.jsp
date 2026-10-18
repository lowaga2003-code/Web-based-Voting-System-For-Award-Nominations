<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, java.time.LocalDateTime" %>
<%@ page import="com.pation.awardnomination.category.*" %>
<%
    String cmTitle = "Archive";
    String cmTab = "archive";
    boolean cmPublic = false;
    List<AwardCategory> archived = (List<AwardCategory>) request.getAttribute("archived");
    Map<Long, Long> votes = (Map<Long, Long>) request.getAttribute("votes");
%>
<%@ include file="_cmTop.jspf" %>

<div class="cm-head">
  <div>
    <div class="cm-kicker">Delete &middot; archive</div>
    <h1>Archived categories</h1>
    <p>Cancelled awards are archived instead of erased, so the transparency log and any sealed ballots stay complete. A category that never received a vote can be deleted permanently.</p>
  </div>
</div>

<% if (archived.isEmpty()) { %>
  <section class="cm-card"><p class="cm-empty" style="margin:0">Nothing archived. Use "Archive" on a category page when an award is cancelled.</p></section>
<% } else { %>
<div class="cm-tickets">
  <% for (AwardCategory c : archived) {
       long v = votes.containsKey(c.getId()) ? votes.get(c.getId()) : 0L; %>
  <article class="cm-ticket">
    <span class="cm-code"><%= CategoryHtml.h(c.getCode()) %></span>
    <h3><a href="<%= cx %>/manager/awards/<%= c.getId() %>" style="color:inherit;text-decoration:none"><%= CategoryHtml.h(c.getName()) %></a></h3>
    <p><%= CategoryHtml.h(c.getArchiveReason()) %></p>
    <dl>
      <dt>Archived</dt><dd><%= CategoryHtml.brief(c.getArchivedAt()) %> by <%= CategoryHtml.h(c.getArchivedBy()) %></dd>
      <dt>Voting was</dt><dd><%= CategoryHtml.brief(c.getVotingStart()) %> &rarr; <%= CategoryHtml.brief(c.getVotingEnd()) %></dd>
      <dt>Ballots</dt><dd><%= v == 0 ? "none" : v + " sealed (kept)" %></dd>
    </dl>
    <div class="cm-ticket-actions">
      <form method="post" action="<%= cx %>/manager/awards/<%= c.getId() %>/restore"><button class="cm-btn cm-btn-sm cm-btn-primary" type="submit">Restore</button></form>
      <% if (v == 0) { %>
      <form method="post" action="<%= cx %>/manager/awards/<%= c.getId() %>/delete" data-cm-confirm="Delete <%= CategoryHtml.h(c.getName()) %> permanently? This cannot be undone."><button class="cm-btn cm-btn-sm cm-btn-danger" type="submit">Delete permanently</button></form>
      <% } else { %>
      <button class="cm-btn cm-btn-sm" type="button" disabled title="Categories with ballots stay archived">Kept for audit</button>
      <% } %>
    </div>
  </article>
  <% } %>
</div>
<% } %>

<%@ include file="_cmBottom.jspf" %>
