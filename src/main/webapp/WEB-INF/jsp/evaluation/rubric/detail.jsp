<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.pation.awardnomination.evaluation.*" %>
<%-- MODULE 5 - One rubric and its criteria (UPDATE / criteria CRUD). --%>
<%
    Rubric rubric = (Rubric) request.getAttribute("rubric");
    String evTitle = "Rubric " + rubric.getName();
    String evNav = "rubrics";
    int left = 100 - rubric.getTotalWeight();
%>
<%@ include file="/WEB-INF/jsp/evaluation/_evTop.jspf" %>
<p class="ev-crumb"><a href="/evaluation/rubrics">Rubrics</a></p>
<div class="ev-head">
 <div><h1><%= h(rubric.getName()) %></h1><p class="ev-lead">Category: <%= h(rubric.getCategoryName()) %></p></div>
 <div class="ev-average"><b class="ev-weight-total<%= rubric.isComplete() ? "" : " off" %>"><%= rubric.getTotalWeight() %>%</b><span><%= rubric.isComplete() ? "ready for marking" : left + "% still to share out" %></span></div>
</div>

<section class="ev-card">
 <div class="ev-weights" role="img" aria-label="Criteria weights add up to <%= rubric.getTotalWeight() %>%">
  <% for (RubricCriterion c : rubric.getCriteria()) { %><span style="width:<%= c.getWeight() %>%" title="<%= h(c.getName()) %> <%= c.getWeight() %>%"></span><% } %>
 </div>
 <h2 class="ev-mt">Criteria</h2>
 <% if (rubric.getCriteria().isEmpty()) { %><p class="ev-note">No criteria yet. Add the first one below.</p><% } %>
 <% for (RubricCriterion c : rubric.getCriteria()) { %>
 <div class="ev-form-row ev-mt">
  <form class="ev-inline" method="post" action="/evaluation/rubrics/<%= rubric.getId() %>/criteria/<%= c.getId() %>">
   <input class="ev-input" name="name" value="<%= h(c.getName()) %>" required aria-label="Criterion name">
   <input class="ev-input tiny" type="number" name="weight" value="<%= c.getWeight() %>" min="1" max="100" required aria-label="Weight %">
   <span class="ev-muted">%</span>
   <button class="ev-btn ghost small">Save</button>
  </form>
  <form class="ev-inline" method="post" action="/evaluation/rubrics/<%= rubric.getId() %>/criteria/<%= c.getId() %>/delete" onsubmit="return confirm('Delete this criterion? Marks given for it are removed too.');">
   <button class="ev-btn danger small">Delete</button>
  </form>
 </div>
 <% } %>
</section>

<div class="ev-grid2">
 <section class="ev-card">
  <h2>Add a criterion</h2>
  <form method="post" action="/evaluation/rubrics/<%= rubric.getId() %>/criteria" class="ev-form-row ev-mt">
   <div class="ev-field grow"><label class="ev-label" for="cname">Criterion</label><input class="ev-input" id="cname" name="name" placeholder="e.g. Innovation" required></div>
   <div class="ev-field"><label class="ev-label" for="cweight">Weight %</label><input class="ev-input tiny" id="cweight" type="number" name="weight" min="1" max="<%= Math.max(left, 1) %>" value="<%= left > 0 ? String.valueOf(left) : "" %>" required></div>
   <button class="ev-btn">Add</button>
  </form>
 </section>
 <section class="ev-card">
  <h2>Rubric details</h2>
  <form method="post" action="/evaluation/rubrics/<%= rubric.getId() %>" class="ev-mt">
   <div class="ev-field"><label class="ev-label" for="rn">Name</label><input class="ev-input" id="rn" name="name" value="<%= h(rubric.getName()) %>" required></div>
   <div class="ev-field"><label class="ev-label" for="rc">Award category</label><input class="ev-input" id="rc" name="categoryName" value="<%= h(rubric.getCategoryName()) %>" required></div>
   <button class="ev-btn ghost">Update rubric</button>
  </form>
 </section>
</div>
<%@ include file="/WEB-INF/jsp/evaluation/_evBottom.jspf" %>
