<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.pation.awardnomination.evaluation.*, com.pation.awardnomination.nominee.Nominee, com.pation.awardnomination.nominee.Interview" %>
<%--
  MODULE 5 - one shortlisted candidate: the Module 3 dossier (evidence, interview) and the score sheet.
  Each judge on the category's panel has one row; marks 0-10 per criterion; changing saved marks = re-evaluation.
--%>
<%
    ShortlistEntry entry = (ShortlistEntry) request.getAttribute("entry");
    Nominee nominee = (Nominee) request.getAttribute("nominee");
    Interview interview = (Interview) request.getAttribute("interview");
    Rubric rubric = (Rubric) request.getAttribute("rubric");
    List<ScoreSheet> sheets = (List<ScoreSheet>) request.getAttribute("sheets");
    Double average = (Double) request.getAttribute("average");
    if (sheets == null) { sheets = new ArrayList<ScoreSheet>(); }
    String evTitle = entry.getNomineeName();
    String evNav = "shortlist";
    String st = entry.getStatus().name();
    boolean canScore = rubric != null && rubric.isComplete() && entry.isActive();
    int finished = 0;
    for (ScoreSheet s : sheets) { if (s.isComplete()) finished++; }
%>
<%@ include file="/WEB-INF/jsp/evaluation/_evTop.jspf" %>
<p class="ev-crumb"><a href="/evaluation/shortlist">Shortlist</a> &nbsp;/&nbsp; <a href="/evaluation/results?category=<%= evUrl(entry.getCategoryName()) %>">Results of <%= h(entry.getCategoryName()) %></a></p>
<div class="ev-head">
 <div>
  <h1><%= h(entry.getNomineeName()) %></h1>
  <p class="ev-lead"><span class="ev-pill <%= st.toLowerCase() %>"><%= st.charAt(0) + st.substring(1).toLowerCase() %></span>
   &nbsp;<%= h(entry.getCategoryName()) %><% if (entry.getReason() != null) { %>. Disqualified: <%= h(entry.getReason()) %><% } %></p>
 </div>
 <div class="ev-average"><b><%= evMark(average) %></b><span>average of <%= finished %> of <%= sheets.size() %> judges, out of 100</span></div>
</div>

<section class="ev-card">
 <div class="ev-card-head"><h2>Nomination and evidence</h2><span class="ev-small ev-muted">From Module 3, read only</span></div>
 <% if (nominee == null) { %>
  <p class="ev-fielderr">Nominee #<%= entry.getNomineeId() %> is no longer in the nominees table of Module 3.</p>
 <% } else { %>
 <dl class="ev-facts">
  <div><dt>Nominee</dt><dd>#<%= nominee.getId() %> <%= h(nominee.getFullName()) %></dd></div>
  <div><dt>NIC</dt><dd><%= h(nominee.getNic()) %></dd></div>
  <div><dt>Organization</dt><dd><%= h(nominee.getCompanyName()) %></dd></div>
  <div><dt>Status in Module 3</dt><dd><%= h(nominee.getStatus()) %></dd></div>
  <div><dt>Evidence file</dt><dd><%= nominee.getEvidenceFileName() == null ? "<span class=\"ev-muted\">No file uploaded</span>" : h(nominee.getEvidenceFileName()) %></dd></div>
  <div><dt>Interview</dt><dd><% if (interview == null) { %><span class="ev-muted">No slot booked</span><% } else { %><%= h(interview.getInterviewDate()) %>, <%= h(interview.getInterviewTime()) %><br><span class="ev-small ev-muted"><%= h(interview.getLocation()) %></span><% } %></dd></div>
 </dl>
 <p class="ev-bio"><%= nominee.getBiography() == null || nominee.getBiography().isBlank() ? "<span class=\"ev-muted\">No biography given.</span>" : h(nominee.getBiography()) %></p>
 <% } %>
</section>

<section class="ev-card">
 <div class="ev-card-head">
  <h2>Score sheet</h2>
  <% if (rubric != null) { %><span class="ev-small ev-muted">Rubric: <a href="/evaluation/rubrics/<%= rubric.getId() %>"><%= h(rubric.getName()) %></a>. Marks 0 to 10, total out of 100.</span><% } %>
 </div>
 <% if (rubric == null) { %>
  <div class="ev-flash warn"><p>There is no rubric for "<%= h(entry.getCategoryName()) %>" yet. <a href="/evaluation/rubrics">Create one</a> with criteria that add up to 100%.</p></div>
 <% } else if (!rubric.isComplete()) { %>
  <div class="ev-flash warn"><p>The rubric adds up to <%= rubric.getTotalWeight() %>%. <a href="/evaluation/rubrics/<%= rubric.getId() %>">Finish it</a> before entering marks.</p></div>
 <% } else if (sheets.isEmpty()) { %>
  <div class="ev-flash warn"><p>No judge is on the panel of "<%= h(entry.getCategoryName()) %>". <a href="/evaluation/assignments">Assign judges</a> first.</p></div>
 <% } else if (!entry.isActive()) { %>
  <div class="ev-flash warn"><p>A disqualified candidate can't be marked. Reinstate the candidate on the Shortlist page first.</p></div>
 <% } %>
 <% if (rubric != null && !rubric.getCriteria().isEmpty() && !sheets.isEmpty()) { %>
 <div class="ev-table-wrap">
 <table class="ev-scorecard">
  <tr>
   <th>Judge</th>
   <% for (RubricCriterion c : rubric.getCriteria()) { %><th><%= h(c.getName()) %><small><%= c.getWeight() %>%</small></th><% } %>
   <th>Total</th><th></th>
  </tr>
  <% for (ScoreSheet s : sheets) {
         String formId = "sheet" + s.getJudge().getId(); %>
  <tr>
   <td><span class="ev-person"><%= h(s.getJudge().getName()) %><small><%= h(s.getJudge().getOrganization()) %></small></span>
    <% if (s.getRevisions() > 0) { %><span class="ev-revised">re-evaluated <%= s.getRevisions() %>x</span><% } %></td>
   <% for (RubricCriterion c : rubric.getCriteria()) {
          Integer mark = s.getScore(c.getId()); %>
   <td><input class="ev-mark" form="<%= formId %>" type="number" name="score_<%= c.getId() %>" min="0" max="10" step="1" required
              value="<%= mark == null ? "" : String.valueOf(mark) %>"<%= canScore ? "" : " disabled" %>
              aria-label="<%= h(s.getJudge().getName()) %>, <%= h(c.getName()) %>"></td>
   <% } %>
   <td><span class="ev-sheet-total<%= s.isComplete() ? "" : " none" %>"><%= s.isComplete() ? evMark(s.getTotal()) : "-" %></span></td>
   <td>
    <form id="<%= formId %>" class="ev-inline" method="post" action="/evaluation/candidates/<%= entry.getId() %>/scores">
     <input type="hidden" name="judgeId" value="<%= s.getJudge().getId() %>">
     <button class="ev-btn<%= s.isStarted() ? " ghost" : "" %> small"<%= canScore ? "" : " disabled" %>><%= s.isStarted() ? "Re-evaluate" : "Save marks" %></button>
    </form>
   </td>
  </tr>
  <% } %>
 </table>
 </div>
 <p class="ev-note">Total = sum of (mark / 10 &times; weight). Changing marks that were already saved is recorded as a re-evaluation in the transparency ledger.</p>
 <% } %>
</section>
<%@ include file="/WEB-INF/jsp/evaluation/_evBottom.jspf" %>
