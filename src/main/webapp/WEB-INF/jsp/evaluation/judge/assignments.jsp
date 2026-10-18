<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.pation.awardnomination.evaluation.*" %>
<%--
  MODULE 5 - Judge panel: assign judges to categories (CREATE) / remove them (DELETE).
  The panel is shown grouped by category; conflicts of interest that exist now are listed with "Remove judge".
--%>
<%
    String evTitle = "Judge panel";
    String evNav = "panel";
    List<Judge> judges = (List<Judge>) request.getAttribute("judges");
    List<JudgeAssignment> assignments = (List<JudgeAssignment>) request.getAttribute("assignments");
    List<PanelConflict> conflicts = (List<PanelConflict>) request.getAttribute("conflicts");
    List<String> categories = (List<String>) request.getAttribute("categories");
    if (judges == null) { judges = new ArrayList<Judge>(); }
    if (assignments == null) { assignments = new ArrayList<JudgeAssignment>(); }
    if (conflicts == null) { conflicts = new ArrayList<PanelConflict>(); }
    if (categories == null) { categories = new ArrayList<String>(); }
    Map<String, List<JudgeAssignment>> byCategory = new TreeMap<String, List<JudgeAssignment>>(String.CASE_INSENSITIVE_ORDER);
    for (JudgeAssignment a : assignments) {
        List<JudgeAssignment> list = byCategory.get(a.getCategoryName());
        if (list == null) { list = new ArrayList<JudgeAssignment>(); byCategory.put(a.getCategoryName(), list); }
        list.add(a);
    }
%>
<%@ include file="/WEB-INF/jsp/evaluation/_evTop.jspf" %>
<div class="ev-head">
 <div>
  <h1>Judge panel</h1>
  <p class="ev-lead">A judge can't join a category that has a shortlisted candidate from the same organization.</p>
 </div>
</div>

<section class="ev-card">
 <h2>Assign a judge</h2>
 <% if (judges.isEmpty()) { %>
  <p class="ev-note">Add judges on the <a href="/evaluation/judges">Judges</a> page first.</p>
 <% } else { %>
 <form method="post" action="/evaluation/assignments" class="ev-form-row ev-mt">
  <div class="ev-field grow">
   <label class="ev-label" for="judgeId">Judge</label>
   <select class="ev-select" id="judgeId" name="judgeId" required>
    <option value="">Choose a judge</option>
    <% for (Judge j : judges) { %><option value="<%= j.getId() %>"><%= h(j.getName()) %> (<%= h(j.getOrganization()) %>)</option><% } %>
   </select>
  </div>
  <div class="ev-field grow">
   <label class="ev-label" for="categoryName">Award category</label>
   <input class="ev-input" id="categoryName" name="categoryName" list="categoryList" required>
   <datalist id="categoryList"><% for (String c : categories) { %><option value="<%= h(c) %>"><% } %></datalist>
  </div>
  <button class="ev-btn">Assign judge</button>
 </form>
 <% } %>
</section>

<% if (!conflicts.isEmpty()) { %>
<section class="ev-flash warn" aria-labelledby="conflictTitle">
 <h2 id="conflictTitle" class="ev-alert-title">Conflicts of interest</h2>
 <p>These candidates were shortlisted after the judge joined the panel. Remove the judge from that category; their marks there are removed too.</p>
 <div class="ev-table-wrap">
 <table class="ev-table">
  <% for (PanelConflict pc : conflicts) { %>
   <tr>
    <td><b><%= h(pc.getAssignment().getJudge().getName()) %></b> in <%= h(pc.getAssignment().getCategoryName()) %></td>
    <td><%= h(pc.getDescription()) %></td>
    <td>
     <form class="ev-inline" method="post" action="/evaluation/assignments/<%= pc.getAssignment().getId() %>/delete" onsubmit="return confirm('Remove this judge from the category?');">
      <input type="hidden" name="conflict" value="yes">
      <button class="ev-btn danger small">Remove judge</button>
     </form>
    </td>
   </tr>
  <% } %>
 </table>
 </div>
</section>
<% } %>

<section class="ev-card">
 <div class="ev-card-head"><h2>Panels by category</h2><span class="ev-small ev-muted">Table judge_assignments</span></div>
 <% if (byCategory.isEmpty()) { %>
  <div class="ev-empty">No judge is on a panel yet.</div>
 <% } %>
 <% for (Map.Entry<String, List<JudgeAssignment>> cat : byCategory.entrySet()) { %>
 <div class="ev-category">
  <div><h3><%= h(cat.getKey()) %></h3><span class="ev-small ev-muted"><%= cat.getValue().size() %> <%= cat.getValue().size() == 1 ? "judge" : "judges" %></span></div>
  <div>
   <% for (JudgeAssignment a : cat.getValue()) { %>
   <span class="ev-chip"><%= h(a.getJudge().getName()) %> <small><%= h(a.getJudge().getOrganization()) %></small>
    <form class="ev-inline" method="post" action="/evaluation/assignments/<%= a.getId() %>/delete" onsubmit="return confirm('Remove from this panel? The judge\'s marks in this category are removed too.');">
     <button class="ev-btn danger small" aria-label="Remove <%= h(a.getJudge().getName()) %> from <%= h(cat.getKey()) %>">Remove</button>
    </form>
   </span>
   <% } %>
  </div>
 </div>
 <% } %>
</section>
<%@ include file="/WEB-INF/jsp/evaluation/_evBottom.jspf" %>
