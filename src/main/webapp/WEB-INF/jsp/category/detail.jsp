<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, java.time.LocalDateTime" %>
<%@ page import="com.pation.awardnomination.category.*" %>
<%@ page import="com.pation.awardnomination.nominee.Nominee" %>
<%@ page import="com.pation.awardnomination.dispute.AuditTransparencyLog" %>
<%
    AwardCategory c = (AwardCategory) request.getAttribute("category");
    CategoryPhase ph = (CategoryPhase) request.getAttribute("phase");
    LocalDateTime now = (LocalDateTime) request.getAttribute("now");
    List<EligibilityRule> rules = (List<EligibilityRule>) request.getAttribute("rules");
    List<CategoryEntry> entries = (List<CategoryEntry>) request.getAttribute("entries");
    Map<Integer, Nominee> nominees = (Map<Integer, Nominee>) request.getAttribute("nominees");
    Map<Integer, EligibilityReport> reports = (Map<Integer, EligibilityReport>) request.getAttribute("reports");
    Map<Integer, Long> tally = (Map<Integer, Long>) request.getAttribute("tally");
    long votes = (Long) request.getAttribute("votes");
    boolean sealed = (Boolean) request.getAttribute("sealed");
    List<CategoryService.Candidate> candidates = (List<CategoryService.Candidate>) request.getAttribute("candidates");
    List<AuditTransparencyLog> history = (List<AuditTransparencyLog>) request.getAttribute("history");
    String cmTitle = c.getName();
    String cmTab = "detail";
    boolean cmPublic = false;
    boolean archived = c.isArchived();
    boolean hasManual = false;
    for (EligibilityRule r : rules) if (!r.isAutomatic()) hasManual = true;
    long top = 0;
    for (Long v : tally.values()) top = Math.max(top, v);
    boolean showVotes = votes > 0 || ph == CategoryPhase.VOTING_OPEN || ph == CategoryPhase.RESULTS;
    LocalDateTime next = c.nextMilestone(now);
    int nextStop = CategoryHtml.nextStop(c, now);
    String[] stopNames = {"Nominations open", "Nominations close", "Voting opens", "Voting closes"};
    LocalDateTime[] stopDates = {c.getNominationStart(), c.getNominationEnd(), c.getVotingStart(), c.getVotingEnd()};
    String[] stopNotes = {"Nominees submit profiles", "Draft profiles lock", "Ballot is sealed at first vote", "Results go to Module 5"};
%>
<%@ include file="_cmTop.jspf" %>

<div class="cm-hero">
  <div>
    <div class="cm-kicker"><a href="<%= cx %>/manager/awards">Control board</a> <span>/</span> <span><%= CategoryHtml.h(c.getCode()) %></span></div>
    <div class="cm-hero-tags">
      <span class="cm-phase cm-phase-lg <%= ph.getCss() %>"><%= CategoryHtml.h(ph.getLabel()) %></span>
      <span class="cm-code"><%= CategoryHtml.h(c.getCode()) %></span>
      <% if (c.getBallotLimit() != null) { %><span class="cm-count" style="font-family:var(--cm-mono);font-size:12px;color:var(--cm-muted)">limit <%= c.getBallotLimit() %> nominees</span><% } %>
    </div>
    <h1><%= CategoryHtml.h(c.getName()) %></h1>
    <p><%= c.getDescription() == null ? "<span style=\"color:var(--cm-muted)\">No description.</span>" : CategoryHtml.h(c.getDescription()) %></p>
    <div class="cm-hero-actions">
      <% if (!archived) { %>
      <a class="cm-btn cm-btn-dark" href="<%= cx %>/manager/awards/<%= c.getId() %>/edit"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="13" r="8"/><path d="M12 9v4l2.5 2.5"/></svg>Edit timeline &amp; details</a>
      <a class="cm-btn" href="#archive">Archive&hellip;</a>
      <% } else { %>
      <form method="post" action="<%= cx %>/manager/awards/<%= c.getId() %>/restore" style="margin:0"><button class="cm-btn cm-btn-primary" type="submit">Restore to the timeline</button></form>
      <a class="cm-btn" href="<%= cx %>/manager/awards/archive">Back to archive</a>
      <% } %>
    </div>
  </div>
  <aside class="cm-countdown">
    <span><%= next == null ? (archived ? "Archived" : "Voting closed") : CategoryHtml.h(c.nextMilestoneLabel(now)) + " in" %></span>
    <% if (next != null) { %>
      <strong data-cm-until="<%= CategoryHtml.iso(next) %>" data-cm-long="1"><%= CategoryHtml.span(now, next) %></strong>
      <small><%= CategoryHtml.full(next) %></small>
    <% } else if (archived) { %>
      <strong><%= CategoryHtml.day(c.getArchivedAt()) %></strong>
      <small>by <%= CategoryHtml.h(c.getArchivedBy()) %></small>
    <% } else { %>
      <strong><%= CategoryHtml.plural(votes, "ballot", "ballots") %></strong>
      <small>sealed &middot; closed <%= CategoryHtml.full(c.getVotingEnd()) %></small>
    <% } %>
  </aside>
</div>

<div class="cm-line" aria-label="Timeline">
  <div class="cm-line-track"><div class="cm-line-fill" style="width:<%= CategoryHtml.num(CategoryHtml.progress(c, now)) %>%"></div></div>
  <% for (int i = 0; i < 4; i++) { %>
  <div class="cm-stop <%= i < nextStop ? "past" : (i == nextStop && !archived ? "next" : "") %>">
    <i></i>
    <b><%= stopNames[i] %></b>
    <span><%= CategoryHtml.brief(stopDates[i]) %></span>
    <small><%= stopNotes[i] %></small>
  </div>
  <% } %>
</div>

<div class="cm-grid cm-grid-2">
  <div>
    <section class="cm-card" id="ballot">
      <div class="cm-card-h">
        <h2>Ballot</h2>
        <span class="cm-count"><%= CategoryHtml.plural(entries.size(), "nominee", "nominees") %></span>
        <% if (showVotes) { %><span class="cm-live-dot <%= ph == CategoryPhase.VOTING_OPEN ? "" : "final" %>" style="margin-left:auto"><%= ph == CategoryPhase.VOTING_OPEN ? "LIVE" : "FINAL" %> &middot; <%= CategoryHtml.plural(votes, "VOTE", "VOTES") %></span><% } %>
      </div>
      <p class="cm-sub">
        <% if (archived) { %>Archived: the ballot is read-only.
        <% } else if (sealed) { %>The ballot is sealed<%= votes > 0 ? " because votes have been cast" : " because voting has closed" %>. Nominees can no longer be added or removed.
        <% } else { %>Place Module 3 nominations on this ballot. Each nominee is checked against the eligibility rules first.<% } %>
      </p>
      <% if (entries.isEmpty()) { %>
        <p class="cm-empty">No nominees on this ballot yet.</p>
      <% } else { %>
      <div class="cm-table-wrap">
      <table class="cm-table cm-table-stack">
        <thead><tr><th>Nominee</th><th>Eligibility now</th><% if (showVotes) { %><th>Votes</th><% } %><% if (!sealed && !archived) { %><th></th><% } %></tr></thead>
        <tbody>
        <% for (CategoryEntry e : entries) {
             Nominee n = nominees.get(e.getNomineeId());
             EligibilityReport rep = reports.get(e.getNomineeId());
             long v = tally.containsKey(e.getNomineeId()) ? tally.get(e.getNomineeId()) : 0L; %>
          <tr>
            <td class="cm-person">
              <b><%= CategoryHtml.h(e.getNomineeName()) %></b>
              <span><%= CategoryHtml.h(e.getOrganisation() == null ? "No organisation" : e.getOrganisation()) %><%= n == null ? " &middot; <strong style=\"color:#be123c\">removed from Module 3</strong>" : " &middot; " + CategoryHtml.h(n.getStatus()) %></span>
            </td>
            <td>
              <% if (rep == null) { %>
                <span class="cm-check no">Nominee record missing</span>
              <% } else if (rep.getLines().isEmpty()) { %>
                <span class="cm-check manual">No rules</span>
              <% } else { %>
                <div class="cm-checks">
                <% for (EligibilityReport.Line line : rep.getLines()) { %>
                  <span class="cm-check <%= !line.isAutomatic() ? "manual" : (line.isPassed() ? "" : "no") %>" title="<%= CategoryHtml.h(line.getRule().describe()) %>: <%= CategoryHtml.h(line.getNote()) %>"><%= !line.isAutomatic() ? "&#9998;" : (line.isPassed() ? "&#10003;" : "&#10007;") %> <%= CategoryHtml.h(CategoryHtml.cut(line.getRule().describe(), 34)) %></span>
                <% } %>
                </div>
              <% } %>
            </td>
            <% if (showVotes) { %>
            <td style="min-width:110px">
              <span class="cm-votes" data-cm-vote="<%= c.getId() %>:<%= e.getNomineeId() %>"><%= v %></span>
              <div class="cm-bar <%= v > 0 && v == top ? "lead" : "" %>"><i data-cm-bar="<%= c.getId() %>:<%= e.getNomineeId() %>" style="width:<%= CategoryHtml.percent(v, votes) %>%"></i></div>
            </td>
            <% } %>
            <% if (!sealed && !archived) { %>
            <td style="text-align:right">
              <form method="post" action="<%= cx %>/manager/awards/<%= c.getId() %>/entries/<%= e.getId() %>/delete" data-cm-confirm="Remove <%= CategoryHtml.h(e.getNomineeName()) %> from this ballot?" style="margin:0">
                <button class="cm-link-btn danger" type="submit">Remove</button>
              </form>
            </td>
            <% } %>
          </tr>
        <% } %>
        </tbody>
      </table>
      </div>
      <% } %>

      <% if (!sealed && !archived) { %>
      <form method="post" action="<%= cx %>/manager/awards/<%= c.getId() %>/entries" style="margin-top:16px;border-top:1px solid var(--cm-line-2);padding-top:16px">
        <div class="cm-inline">
          <div class="cm-field">
            <label for="nomineeId">Add a nominee from Module 3</label>
            <select class="cm-select" id="nomineeId" name="nomineeId" required>
              <option value="">Choose a nomination&hellip;</option>
              <% for (CategoryService.Candidate cand : candidates) {
                   Nominee n = cand.getNominee();
                   boolean ok = cand.getReport().isAutoPassed(); %>
                <option value="<%= n.getId() %>" <%= ok ? "" : "disabled" %>><%= CategoryHtml.h(n.getFullName()) %> &mdash; <%= CategoryHtml.h(n.getCompanyName() == null || n.getCompanyName().isBlank() ? "no organisation" : n.getCompanyName()) %> (<%= CategoryHtml.h(n.getStatus()) %>)<%= ok ? "" : " &middot; not eligible: " + CategoryHtml.h(CategoryHtml.cut(cand.getReport().failedRules(), 60)) %></option>
              <% } %>
            </select>
          </div>
          <button class="cm-btn cm-btn-primary" type="submit">Place on ballot</button>
        </div>
        <% if (hasManual) { %>
        <label class="cm-tick" style="margin-top:12px"><input type="checkbox" name="manualConfirmed" value="yes"> I have checked the manual eligibility rules for this nominee.</label>
        <% } %>
        <% if (candidates.isEmpty()) { %><p class="cm-empty" style="margin:8px 0 0">Every Module 3 nomination is already on this ballot, or none have been submitted yet.</p><% } %>
      </form>
      <% } %>
    </section>

    <section class="cm-card">
      <div class="cm-card-h"><h2>History</h2><span class="cm-count">CAT-<%= CategoryHtml.h(c.getCode()) %></span><a class="cm-card-link" href="<%= cx %>/dispute/logs">Transparency log &rarr;</a></div>
      <% if (history.isEmpty()) { %>
        <p class="cm-empty">No ledger entries for this category yet.</p>
      <% } else { %>
      <ul class="cm-ledger">
        <% for (AuditTransparencyLog l : history) { %>
        <li>
          <b><%= CategoryHtml.h(l.getActionTaken()) %></b>
          <span><%= CategoryHtml.h(l.getRemarks()) %></span>
          <span><%= CategoryHtml.h(l.getInvestigationFindings()) %></span>
          <time><%= CategoryHtml.full(l.getTimestamp()) %></time> &middot; <code title="SHA-256"><%= CategoryHtml.h(CategoryHtml.cut(l.getCryptographicHash(), 19)) %></code>
        </li>
        <% } %>
      </ul>
      <% } %>
    </section>
  </div>

  <div>
    <section class="cm-card" id="rules">
      <div class="cm-card-h"><h2>Eligibility rules</h2><span class="cm-count"><%= rules.size() %></span></div>
      <% if (rules.isEmpty()) { %>
        <p class="cm-empty">No rules: any nomination can be placed on the ballot.</p>
      <% } else { %>
      <ul class="cm-rules">
        <% for (EligibilityRule r : rules) { %>
        <li>
          <span class="cm-rule-kind <%= r.isAutomatic() ? "auto" : "manual" %>"><%= r.isAutomatic() ? "AUTO" : "MANUAL" %></span>
          <span><%= CategoryHtml.h(r.describe()) %></span>
          <% if (!sealed && !archived) { %>
          <form method="post" action="<%= cx %>/manager/awards/<%= c.getId() %>/rules/<%= r.getId() %>/delete" data-cm-confirm="Remove this eligibility rule?">
            <button class="cm-link-btn danger" type="submit" aria-label="Remove rule">Remove</button>
          </form>
          <% } %>
        </li>
        <% } %>
      </ul>
      <% } %>
      <% if (!sealed && !archived) { %>
      <form method="post" action="<%= cx %>/manager/awards/<%= c.getId() %>/rules" data-cm-rule-form>
        <div class="cm-field">
          <label for="ruleType">Add a rule</label>
          <select class="cm-select" id="ruleType" name="ruleType" data-cm-rule-type>
            <% for (RuleType t : RuleType.values()) { %><option value="<%= t.name() %>"><%= CategoryHtml.h(t.getLabel()) %></option><% } %>
          </select>
        </div>
        <div class="cm-field" data-cm-show-for="MIN_BIO_WORDS" style="margin-top:10px">
          <label for="minimum">Minimum words</label>
          <input class="cm-input" type="number" min="1" max="2000" id="minimum" name="minimum" placeholder="e.g. 50">
        </div>
        <div class="cm-field" data-cm-show-for="CUSTOM" style="margin-top:10px">
          <label for="ruleText">Rule wording</label>
          <input class="cm-input" type="text" maxlength="300" id="ruleText" name="text" placeholder="e.g. Has worked in Sri Lanka for 2+ years">
        </div>
        <button class="cm-btn" type="submit" style="margin-top:12px;width:100%">Add rule</button>
      </form>
      <div class="cm-help"><b>AUTO</b> rules are checked against the nominee's Module 3 record. <b>MANUAL</b> rules are confirmed by you when placing a nominee.</div>
      <% } %>
    </section>

    <section class="cm-card">
      <div class="cm-card-h"><h2>Timeline</h2></div>
      <dl style="display:grid;grid-template-columns:auto 1fr;gap:6px 14px;margin:0;font-size:13.5px">
        <dt style="color:var(--cm-muted)">Nominations</dt><dd style="margin:0" class="cm-mono"><%= CategoryHtml.brief(c.getNominationStart()) %> &rarr; <%= CategoryHtml.brief(c.getNominationEnd()) %></dd>
        <dt style="color:var(--cm-muted)">Voting</dt><dd style="margin:0" class="cm-mono"><%= CategoryHtml.brief(c.getVotingStart()) %> &rarr; <%= CategoryHtml.brief(c.getVotingEnd()) %></dd>
        <dt style="color:var(--cm-muted)">Created</dt><dd style="margin:0"><%= CategoryHtml.brief(c.getCreatedAt()) %> by <%= CategoryHtml.h(c.getCreatedBy()) %></dd>
        <dt style="color:var(--cm-muted)">Updated</dt><dd style="margin:0"><%= CategoryHtml.brief(c.getUpdatedAt()) %></dd>
      </dl>
      <div class="cm-help"><%= CategoryHtml.h(ph.getHelp()) %></div>
    </section>

    <% if (!archived) { %>
    <section class="cm-card cm-danger" id="archive">
      <div class="cm-card-h"><h2>Archive this category</h2></div>
      <p class="cm-sub" style="margin-top:-6px">Use this when an award is cancelled. It leaves ballots, the calendar and Module 3's deadlines; <%= votes > 0 ? "its " + votes + " sealed ballot(s) are kept for the audit trail." : "you can restore it later." %></p>
      <form method="post" action="<%= cx %>/manager/awards/<%= c.getId() %>/archive" data-cm-confirm="Archive <%= CategoryHtml.h(c.getName()) %>? It will disappear from ballots and the award calendar.">
        <div class="cm-field">
          <label for="reason">Reason (written to the transparency log)</label>
          <textarea class="cm-textarea" id="reason" name="reason" minlength="5" maxlength="500" required placeholder="e.g. Sponsor withdrew the award"></textarea>
        </div>
        <button class="cm-btn cm-btn-danger" type="submit" style="margin-top:12px">Archive category</button>
      </form>
    </section>
    <% } else { %>
    <section class="cm-card cm-danger">
      <div class="cm-card-h"><h2>Archived</h2></div>
      <p style="margin:0;font-size:14px"><%= CategoryHtml.h(c.getArchiveReason()) %></p>
      <p class="cm-sub" style="margin:8px 0 0">By <%= CategoryHtml.h(c.getArchivedBy()) %> on <%= CategoryHtml.full(c.getArchivedAt()) %></p>
    </section>
    <% } %>
  </div>
</div>

<% if (showVotes) { %><div hidden data-cm-live-url="<%= cx %>/manager/awards/api/live"></div><% } %>

<%@ include file="_cmBottom.jspf" %>
