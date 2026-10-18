<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, java.time.LocalDateTime" %>
<%@ page import="com.pation.awardnomination.category.*" %>
<%@ page import="com.pation.awardnomination.dispute.AuditTransparencyLog" %>
<%
    String cmTitle = "Season control board";
    String cmTab = "board";
    boolean cmPublic = false;
    LiveStats stats = (LiveStats) request.getAttribute("stats");
    LocalDateTime now = (LocalDateTime) request.getAttribute("now");
    List<BoardNotice> notices = (List<BoardNotice>) request.getAttribute("notices");
    List<AuditTransparencyLog> recent = (List<AuditTransparencyLog>) request.getAttribute("recent");
    NominationLocks locks = (NominationLocks) request.getAttribute("locks");
    List<AwardCategory> cats = new ArrayList<AwardCategory>();
    for (LiveStats.CategoryLive cl : stats.getCategories()) cats.add(cl.getCategory());
    CategoryHtml.Scale scale = CategoryHtml.scaleFor(cats, now);
    List<LocalDateTime> ticks = scale.days() > 100 ? scale.monthTicks() : scale.weekTicks();
%>
<%@ include file="_cmTop.jspf" %>

<div class="cm-head">
  <div>
    <div class="cm-kicker">Programme Manager &middot; Module 2</div>
    <h1>Season control board</h1>
    <p>Every deadline of the award season is set here. Nominee profile locks (Module 3), voting and judging all follow these dates.</p>
  </div>
  <div class="cm-head-actions">
    <a class="cm-btn" href="<%= cx %>/manager/awards/stats"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 20V10M10 20V4M16 20v-7M22 20H2"/></svg>Live statistics</a>
    <a class="cm-btn cm-btn-primary" href="<%= cx %>/manager/awards/new"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"><path d="M12 5v14M5 12h14"/></svg>New category</a>
  </div>
</div>

<div class="cm-kpis" data-cm-live-url="<%= cx %>/manager/awards/api/live">
  <div class="cm-kpi" style="--k:#06b6d4"><span>Active categories</span><strong data-cm-live="categories"><%= stats.getActiveCategories() %></strong><small><%= stats.countInPhase(CategoryPhase.NOMINATIONS_OPEN) %> taking nominations</small></div>
  <div class="cm-kpi" style="--k:var(--ph-live)"><span>Voting live now</span><strong data-cm-live="live"><%= stats.countInPhase(CategoryPhase.VOTING_OPEN) %></strong><small>categories open to voters</small></div>
  <div class="cm-kpi" style="--k:var(--ph-nom)"><span>Nominations received</span><strong data-cm-live="nominations"><%= stats.getNominationsTotal() %></strong><small><span data-cm-live="approved"><%= stats.getApproved() %></span> approved &middot; <span data-cm-live="drafts"><%= stats.getDrafts() %></span> draft</small></div>
  <div class="cm-kpi" style="--k:var(--ph-prep)"><span>Nominees on ballots</span><strong data-cm-live="entries"><%= stats.getEntriesOnBallots() %></strong><small>across all categories</small></div>
  <div class="cm-kpi" style="--k:var(--ph-done)"><span>Votes cast</span><strong data-cm-live="votes"><%= stats.getVotesTotal() %></strong><small><span data-cm-live="turnout"><%= stats.getTurnoutPercent() %></span>% of <span data-cm-live="registered"><%= stats.getRegisteredVoters() %></span> voters</small></div>
</div>

<div class="cm-lockbox">
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></svg>
  <div>
    <strong>Module 3 nomination lock:</strong>
    <% if (!locks.isConfigured()) { %>
      no deadlines yet, so nominee profiles are not locked.
    <% } else if (locks.acceptingNew()) { %>
      new nominations are open. Profiles that are not on a ballot lock at <span class="cm-mono"><%= CategoryHtml.full(locks.getOverallDeadline()) %></span>.
    <% } else { %>
      every nomination window is closed, so new nominations and profile edits are locked.
    <% } %>
  </div>
  <a href="<%= cx %>/timeline" style="margin-left:auto;font-weight:600;text-decoration:none">Public award calendar &rarr;</a>
</div>

<section class="cm-board" aria-labelledby="board-h">
  <div class="cm-board-h">
    <h2 id="board-h">SEASON DEPARTURES</h2>
    <div class="cm-board-legend">
      <span class="nom"><b></b>Nominations</span><span class="prep"><b></b>Ballot prep</span><span class="live"><b></b>Voting live</span><span class="done"><b></b>Results</span>
    </div>
  </div>
  <% if (stats.getCategories().isEmpty()) { %>
    <div class="cm-board-empty">NO ACTIVE CATEGORIES &middot; <a href="<%= cx %>/manager/awards/new" style="color:#67e8f9">CREATE THE FIRST ONE</a></div>
  <% } else { %>
  <div style="overflow-x:auto">
  <table class="cm-board-table">
    <thead><tr><th>Code</th><th>Award category</th><th>Nominations</th><th>Voting</th><th>Status</th><th>Next</th></tr></thead>
    <tbody>
    <% for (LiveStats.CategoryLive cl : stats.getCategories()) {
         AwardCategory c = cl.getCategory();
         CategoryPhase ph = cl.getPhase();
         LocalDateTime next = c.nextMilestone(now); %>
      <tr data-href="<%= cx %>/manager/awards/<%= c.getId() %>">
        <td data-l="CODE"><span class="cm-board-code"><%= CategoryHtml.h(c.getCode()) %></span></td>
        <td>
          <a class="cm-board-name" href="<%= cx %>/manager/awards/<%= c.getId() %>"><%= CategoryHtml.h(c.getName()) %></a>
          <div class="cm-board-meta"><%= CategoryHtml.plural(cl.getEntries(), "nominee", "nominees") %> on ballot &middot; <span data-cm-cat-votes="<%= c.getId() %>"><%= cl.getVotes() %></span> votes</div>
        </td>
        <td data-l="NOMINATIONS" class="cm-board-when"><%= CategoryHtml.brief(c.getNominationStart()) %><small>to <%= CategoryHtml.brief(c.getNominationEnd()) %></small></td>
        <td data-l="VOTING" class="cm-board-when"><%= CategoryHtml.brief(c.getVotingStart()) %><small>to <%= CategoryHtml.brief(c.getVotingEnd()) %></small></td>
        <td data-l="STATUS"><span class="cm-status <%= ph.getCss() %>"><span class="cm-led"></span><span class="cm-flaps"><%= CategoryHtml.flaps(ph.getBoardText()) %></span></span></td>
        <td data-l="NEXT" class="cm-board-next">
          <% if (next != null) { %>
            <span data-cm-until="<%= CategoryHtml.iso(next) %>"><%= CategoryHtml.span(now, next) %></span><small><%= CategoryHtml.h(c.nextMilestoneLabel(now)) %></small>
          <% } else { %>
            &mdash;<small>Voting closed</small>
          <% } %>
        </td>
      </tr>
    <% } %>
    </tbody>
  </table>
  </div>
  <% } %>
</section>

<div class="cm-grid cm-grid-2">
  <div>
    <section class="cm-card">
      <div class="cm-card-h"><h2>Season timeline</h2><span class="cm-count"><%= CategoryHtml.day(scale.getStart()) %> &ndash; <%= CategoryHtml.day(scale.getEnd()) %></span></div>
      <% if (cats.isEmpty()) { %>
        <p class="cm-empty">The timeline appears once a category exists.</p>
      <% } else { %>
      <div class="cm-gantt">
        <div class="cm-gantt-axis">
          <% for (LocalDateTime t : ticks) { if (Math.abs(scale.pos(t) - scale.pos(now)) < 4) continue; %><span style="left:<%= CategoryHtml.num(scale.pos(t)) %>%"><%= scale.days() > 100 ? scale.monthLabel(t) : scale.dayLabel(t) %></span><% } %>
        </div>
        <div class="cm-gantt-body">
          <% for (AwardCategory c : cats) {
               CategoryPhase ph = c.phaseAt(now);
               String vClass = ph == CategoryPhase.RESULTS ? "done" : (ph == CategoryPhase.VOTING_OPEN ? "" : "wait"); %>
          <div class="cm-gantt-row">
            <div class="cm-gantt-label"><a href="<%= cx %>/manager/awards/<%= c.getId() %>"><%= CategoryHtml.h(c.getName()) %></a></div>
            <div class="cm-gantt-track">
              <span class="cm-gantt-bar n" style="left:<%= CategoryHtml.num(scale.pos(c.getNominationStart())) %>%;width:<%= CategoryHtml.num(scale.width(c.getNominationStart(), c.getNominationEnd())) %>%" title="Nominations <%= CategoryHtml.brief(c.getNominationStart()) %> to <%= CategoryHtml.brief(c.getNominationEnd()) %>">NOM</span>
              <span class="cm-gantt-bar v <%= vClass %>" style="left:<%= CategoryHtml.num(scale.pos(c.getVotingStart())) %>%;width:<%= CategoryHtml.num(scale.width(c.getVotingStart(), c.getVotingEnd())) %>%" title="Voting <%= CategoryHtml.brief(c.getVotingStart()) %> to <%= CategoryHtml.brief(c.getVotingEnd()) %>">VOTE</span>
            </div>
          </div>
          <% } %>
          <div class="cm-gantt-nowline"><span class="cm-gantt-now" style="left:<%= CategoryHtml.num(scale.pos(now)) %>%"></span></div>
        </div>
      </div>
      <div class="cm-gantt-legend">
        <span><i style="background:var(--ph-nom)"></i>Nomination window</span>
        <span><i style="background:repeating-linear-gradient(135deg,#16a34a 0 4px,#22c55e 4px 8px)"></i>Voting (upcoming)</span>
        <span><i style="background:var(--ph-live)"></i>Voting live</span>
        <span><i style="background:var(--ph-done)"></i>Voting closed</span>
        <span><i style="background:var(--cm-amber);width:3px"></i>Now</span>
      </div>
      <% } %>
    </section>
  </div>

  <div>
    <section class="cm-card">
      <div class="cm-card-h"><h2>Needs attention</h2><span class="cm-count"><%= notices.size() %></span></div>
      <% if (notices.isEmpty()) { %>
        <p class="cm-empty">All clear. Nothing needs the Programme Manager right now.</p>
      <% } %>
      <% for (BoardNotice n : notices) { %>
        <div class="cm-notice <%= n.getLevel() %>">
          <span class="cm-notice-dot"><%= "warn".equals(n.getLevel()) ? "!" : ("done".equals(n.getLevel()) ? "&#10003;" : "i") %></span>
          <div><%= CategoryHtml.h(n.getMessage()) %>
            <% if (n.getLink() != null) { %> <a href="<%= cx %><%= n.getLink() %>">Open &rarr;</a><% } %>
          </div>
        </div>
      <% } %>
    </section>

    <section class="cm-card">
      <div class="cm-card-h"><h2>Ledger</h2><span class="cm-count">Module 4</span><a class="cm-card-link" href="<%= cx %>/dispute/logs">Full log &rarr;</a></div>
      <% if (recent == null || recent.isEmpty()) { %>
        <p class="cm-empty">No Module 2 entries in the transparency log yet.</p>
      <% } else { %>
      <ul class="cm-ledger">
        <% for (AuditTransparencyLog l : recent) { %>
        <li>
          <b><%= CategoryHtml.h(l.getActionTaken()) %></b>
          <span><%= CategoryHtml.h(CategoryHtml.cut(l.getRemarks(), 150)) %></span>
          <time><%= CategoryHtml.brief(l.getTimestamp()) %></time> &middot; <code><%= CategoryHtml.h(l.getTicketCode()) %></code>
        </li>
        <% } %>
      </ul>
      <% } %>
    </section>
  </div>
</div>

<%@ include file="_cmBottom.jspf" %>
