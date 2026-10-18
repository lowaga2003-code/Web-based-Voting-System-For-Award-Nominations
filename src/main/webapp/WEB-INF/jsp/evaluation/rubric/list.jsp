<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.pation.awardnomination.evaluation.*" %>
<%-- MODULE 5 - Evaluation rubrics (CREATE / READ / DELETE), each shown with its weight bar. --%>
<%
    String evTitle = "Rubrics";
    String evNav = "rubrics";
    List<Rubric> rubrics = (List<Rubric>) request.getAttribute("rubrics");
    List<String> categories = (List<String>) request.getAttribute("categories");
    if (rubrics == null) { rubrics = new ArrayList<Rubric>(); }
    if (categories == null) { categories = new ArrayList<String>(); }
%>
<%@ include file="/WEB-INF/jsp/evaluation/_evTop.jspf" %>
<div class="ev-head">
 <div>
  <h1>Rubrics</h1>
  <p class="ev-lead">One rubric per award category. Judges can only enter marks once its criteria add up to 100%.</p>
 </div>
</div>

<section class="ev-card">
 <h2>New rubric</h2>
 <form method="post" action="/evaluation/rubrics" class="ev-form-row ev-mt">
  <div class="ev-field grow"><label class="ev-label" for="rname">Rubric name</label><input class="ev-input" id="rname" name="name" required></div>
  <div class="ev-field grow"><label class="ev-label" for="rcat">Award category</label><input class="ev-input" id="rcat" name="categoryName" list="categoryList" required>
   <datalist id="categoryList"><% for (String c : categories) { %><option value="<%= h(c) %>"><% } %></datalist></div>
  <button class="ev-btn">Create rubric</button>
 </form>
</section>

<% if (rubrics.isEmpty()) { %>
 <div class="ev-empty">No rubrics yet.</div>
<% } else { %>
<div class="ev-rubrics">
 <% for (Rubric r : rubrics) { %>
 <article class="ev-card">
  <div class="ev-card-head">
   <h3><%= h(r.getName()) %></h3>
   <span class="ev-pill <%= r.isComplete() ? "ready" : "incomplete" %>"><%= r.isComplete() ? "Ready for marking" : r.getTotalWeight() + "% of 100%" %></span>
  </div>
  <span class="ev-muted ev-small"><%= h(r.getCategoryName()) %></span>
  <div class="ev-weights" role="img" aria-label="Criteria weights add up to <%= r.getTotalWeight() %>%">
   <% for (RubricCriterion c : r.getCriteria()) { %><span style="width:<%= c.getWeight() %>%" title="<%= h(c.getName()) %> <%= c.getWeight() %>%"></span><% } %>
  </div>
  <div class="ev-weight-legend"><% for (RubricCriterion c : r.getCriteria()) { %><span><%= h(c.getName()) %> <%= c.getWeight() %>%</span><% } %>
   <% if (r.getCriteria().isEmpty()) { %><span>No criteria yet</span><% } %></div>
  <div class="ev-actions">
   <a class="ev-btn ghost small" href="/evaluation/rubrics/<%= r.getId() %>">Edit criteria</a>
   <form class="ev-inline" method="post" action="/evaluation/rubrics/<%= r.getId() %>/delete" onsubmit="return confirm('Delete this rubric? Marks given with it are removed too.');">
    <button class="ev-btn danger small">Delete</button>
   </form>
  </div>
 </article>
 <% } %>
</div>
<% } %>
<%@ include file="/WEB-INF/jsp/evaluation/_evBottom.jspf" %>
