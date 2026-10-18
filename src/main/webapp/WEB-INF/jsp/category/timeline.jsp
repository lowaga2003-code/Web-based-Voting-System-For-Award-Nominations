<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, java.time.LocalDateTime" %>
<%@ page import="com.pation.awardnomination.category.*" %>
<%
    String cmTitle = "Award calendar";
    String cmTab = "calendar";
    boolean cmPublic = true;
    List<AwardCategory> cats = (List<AwardCategory>) request.getAttribute("categories");
    LocalDateTime now = (LocalDateTime) request.getAttribute("now");
    CategoryHtml.Scale scale = CategoryHtml.scaleFor(cats, now);
    List<LocalDateTime> ticks = scale.days() > 100 ? scale.monthTicks() : scale.weekTicks();
    CategoryPhase[] shown = {CategoryPhase.SCHEDULED, CategoryPhase.NOMINATIONS_OPEN, CategoryPhase.BALLOT_PREP, CategoryPhase.VOTING_OPEN, CategoryPhase.RESULTS};
%>
<%@ include file="_cmTop.jspf" %>

<div class="cm-head">
  <div>
    <div class="cm-kicker">Award season &middot; set by the Programme Manager</div>
    <h1>Award calendar</h1>
    <p>When each award takes nominations and when voting opens. Nominee profiles lock when nominations close; votes are accepted only inside the voting window.</p>
  </div>
  <% if (Boolean.TRUE.equals(request.getAttribute("isVoter"))) { %>
  <div class="cm-head-actions">
    <a class="cm-btn cm-btn-primary" href="<%= cx %>/voter/ballot">Go to my ballot</a>
  </div>
  <% } %>
</div>

<% if (cats.isEmpty()) { %>
  <section class="cm-card"><p class="cm-empty" style="margin:0">No award categories are scheduled yet.</p></section>
<% } else { %>
<div class="cm-cal" style="margin-bottom:22px">
  <% for (AwardCategory c : cats) {
       CategoryPhase ph = c.phaseAt(now);
       LocalDateTime next = c.nextMilestone(now); %>
  <article class="cm-cal-card">
    <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap"><span class="cm-phase <%= ph.getCss() %>"><%= CategoryHtml.h(ph.getLabel()) %></span><span class="cm-code"><%= CategoryHtml.h(c.getCode()) %></span></div>
    <h3><%= CategoryHtml.h(c.getName()) %></h3>
    <p><%= CategoryHtml.h(c.getDescription() == null ? "" : CategoryHtml.cut(c.getDescription(), 160)) %></p>
    <div class="cm-cal-win">
      <div><b>Nominations</b><span><%= CategoryHtml.brief(c.getNominationStart()) %></span><span>&rarr; <%= CategoryHtml.brief(c.getNominationEnd()) %></span></div>
      <div><b>Voting</b><span><%= CategoryHtml.brief(c.getVotingStart()) %></span><span>&rarr; <%= CategoryHtml.brief(c.getVotingEnd()) %></span></div>
    </div>
    <div class="cm-cal-next">
      <% if (next != null) { %><%= CategoryHtml.h(c.nextMilestoneLabel(now)) %> in <span data-cm-until="<%= CategoryHtml.iso(next) %>"><%= CategoryHtml.span(now, next) %></span><% } else { %>Voting has closed<% } %>
    </div>
  </article>
  <% } %>
</div>

<section class="cm-card" style="margin-bottom:22px">
  <div class="cm-card-h"><h2>Season timeline</h2></div>
  <div class="cm-gantt">
    <div class="cm-gantt-axis">
      <% for (LocalDateTime t : ticks) { if (Math.abs(scale.pos(t) - scale.pos(now)) < 4) continue; %><span style="left:<%= CategoryHtml.num(scale.pos(t)) %>%"><%= scale.days() > 100 ? scale.monthLabel(t) : scale.dayLabel(t) %></span><% } %>
    </div>
    <div class="cm-gantt-body">
      <% for (AwardCategory c : cats) {
           CategoryPhase ph = c.phaseAt(now);
           String vClass = ph == CategoryPhase.RESULTS ? "done" : (ph == CategoryPhase.VOTING_OPEN ? "" : "wait"); %>
      <div class="cm-gantt-row">
        <div class="cm-gantt-label"><%= CategoryHtml.h(c.getName()) %></div>
        <div class="cm-gantt-track">
          <span class="cm-gantt-bar n" style="left:<%= CategoryHtml.num(scale.pos(c.getNominationStart())) %>%;width:<%= CategoryHtml.num(scale.width(c.getNominationStart(), c.getNominationEnd())) %>%">NOM</span>
          <span class="cm-gantt-bar v <%= vClass %>" style="left:<%= CategoryHtml.num(scale.pos(c.getVotingStart())) %>%;width:<%= CategoryHtml.num(scale.width(c.getVotingStart(), c.getVotingEnd())) %>%">VOTE</span>
        </div>
      </div>
      <% } %>
      <div class="cm-gantt-nowline"><span class="cm-gantt-now" style="left:<%= CategoryHtml.num(scale.pos(now)) %>%"></span></div>
    </div>
  </div>
</section>
<% } %>

<h2 style="font-size:17px;margin:0 0 12px">What each phase means</h2>
<div class="cm-phases">
  <% for (CategoryPhase p : shown) { %>
  <div><span class="cm-phase <%= p.getCss() %>"><%= CategoryHtml.h(p.getLabel()) %></span><br><%= CategoryHtml.h(p.getHelp()) %></div>
  <% } %>
</div>

<%@ include file="_cmBottom.jspf" %>
