<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.pation.awardnomination.evaluation.*" %>
<%-- MODULE 5 - Add / edit judge (CREATE / UPDATE). --%>
<%
    String action = (String) request.getAttribute("action");
    if (action == null) { action = "/evaluation/judges"; }
    boolean adding = "/evaluation/judges".equals(action);
    String evTitle = adding ? "Add judge" : "Edit judge";
    String evNav = "judges";
    Judge judge = (Judge) request.getAttribute("judge");
    if (judge == null) { judge = new Judge(); }
    Map<String, String> fieldErrors = (Map<String, String>) request.getAttribute("fieldErrors");
    if (fieldErrors == null) { fieldErrors = new HashMap<String, String>(); }
    String[][] fields = {
        {"name", "Name", judge.getName(), "text"},
        {"email", "Email", judge.getEmail(), "email"},
        {"organization", "Organization", judge.getOrganization(), "text"},
        {"expertise", "Expertise (optional)", judge.getExpertise(), "text"}
    };
%>
<%@ include file="/WEB-INF/jsp/evaluation/_evTop.jspf" %>
<p class="ev-crumb"><a href="/evaluation/judges">Judges</a></p>
<div class="ev-head"><h1><%= adding ? "Add a judge" : "Edit " + h(judge.getName()) %></h1></div>
<section class="ev-card ev-narrow">
 <form action="<%= h(action) %>" method="post">
  <% for (String[] f : fields) { %>
  <div class="ev-field">
   <label class="ev-label" for="<%= f[0] %>"><%= f[1] %></label>
   <input class="ev-input" id="<%= f[0] %>" name="<%= f[0] %>" type="<%= f[3] %>" value="<%= h(f[2]) %>"<%= "expertise".equals(f[0]) ? "" : " required" %>>
   <% if (fieldErrors.containsKey(f[0])) { %><span class="ev-fielderr"><%= h(fieldErrors.get(f[0])) %></span><% } %>
  </div>
  <% } %>
  <div class="ev-actions"><button class="ev-btn" type="submit">Save judge</button><a class="ev-btn ghost" href="/evaluation/judges">Cancel</a></div>
 </form>
</section>
<%@ include file="/WEB-INF/jsp/evaluation/_evBottom.jspf" %>
