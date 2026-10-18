<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.pation.awardnomination.evaluation.*" %>
<%-- MODULE 5 - Judges (READ / DELETE). --%>
<%
    String evTitle = "Judges";
    String evNav = "judges";
    List<Judge> judges = (List<Judge>) request.getAttribute("judges");
    if (judges == null) { judges = new ArrayList<Judge>(); }
%>
<%@ include file="/WEB-INF/jsp/evaluation/_evTop.jspf" %>
<div class="ev-head">
 <div>
  <h1>Judges</h1>
  <p class="ev-lead">The organization is used for the conflict-of-interest check: a judge can't mark a category that has a candidate from the same organization.</p>
 </div>
 <a class="ev-btn" href="/evaluation/judges/new">Add judge</a>
</div>
<section class="ev-card">
 <div class="ev-card-head"><h2><%= judges.size() %> <%= judges.size() == 1 ? "judge" : "judges" %></h2><span class="ev-small ev-muted">Table judges</span></div>
 <% if (judges.isEmpty()) { %>
  <div class="ev-empty">No judges yet. Add the first judge to start the panel.</div>
 <% } else { %>
 <div class="ev-table-wrap">
 <table class="ev-table">
  <tr><th>ID</th><th>Judge</th><th>Organization</th><th>Expertise</th><th></th></tr>
  <% for (Judge j : judges) { %>
   <tr>
    <td><%= j.getId() %></td>
    <td><span class="ev-person"><%= h(j.getName()) %><small><%= h(j.getEmail()) %></small></span></td>
    <td><%= h(j.getOrganization()) %></td>
    <td><%= h(j.getExpertise()) %></td>
    <td>
     <div class="ev-actions">
      <a class="ev-btn ghost small" href="/evaluation/judges/<%= j.getId() %>/edit">Edit</a>
      <form class="ev-inline" method="post" action="/evaluation/judges/<%= j.getId() %>/delete" onsubmit="return confirm('Delete this judge? Their panel places and marks are removed too.');">
       <button class="ev-btn danger small">Delete</button>
      </form>
     </div>
    </td>
   </tr>
  <% } %>
 </table>
 </div>
 <% } %>
</section>
<%@ include file="/WEB-INF/jsp/evaluation/_evBottom.jspf" %>
