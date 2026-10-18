<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, com.pation.awardnomination.evaluation.*, com.pation.awardnomination.nominee.Nominee" %>
<%--
  MODULE 5 - Candidate shortlist (CREATE / READ / UPDATE / DELETE). Nominations come from Module 3;
  the candidate's name opens the score sheet (evidence + judges' marks).
--%>
<%
    String evTitle = "Shortlist";
    String evNav = "shortlist";
    List<ShortlistEntry> entries = (List<ShortlistEntry>) request.getAttribute("entries");
    List<Nominee> nominees = (List<Nominee>) request.getAttribute("nominees");
    List<String> categories = (List<String>) request.getAttribute("categories");
    String category = (String) request.getAttribute("category");
    if (entries == null) { entries = new ArrayList<ShortlistEntry>(); }
    if (nominees == null) { nominees = new ArrayList<Nominee>(); }
    if (categories == null) { categories = new ArrayList<String>(); }
%>
<%@ include file="/WEB-INF/jsp/evaluation/_evTop.jspf" %>
<div class="ev-head">
 <div>
  <h1>Shortlist</h1>
  <p class="ev-lead">The final round. Open a candidate to read the Module 3 evidence and enter the judges' marks.</p>
 </div>
</div>

<section class="ev-card">
 <h2>Add a nomination</h2>
 <% if (nominees.isEmpty()) { %>
  <p class="ev-note">No nomination can be shortlisted yet. In Module 3 a nomination must be <b>Under Review</b> or <b>Approved</b>, not Draft.</p>
 <% } else { %>
 <form method="post" action="/evaluation/shortlist" class="ev-form-row ev-mt">
  <div class="ev-field grow">
   <label class="ev-label" for="nomineeId">Nomination (Module 3)</label>
   <select class="ev-select" id="nomineeId" name="nomineeId" required>
    <option value="">Choose a nomination</option>
    <% for (Nominee n : nominees) { %>
     <option value="<%= n.getId() %>">#<%= n.getId() %> <%= h(n.getFullName()) %>, <%= h(n.getCompanyName()) %> (<%= h(n.getStatus()) %>)</option>
    <% } %>
   </select>
  </div>
  <div class="ev-field grow">
   <label class="ev-label" for="categoryName">Award category</label>
   <input class="ev-input" id="categoryName" name="categoryName" list="categoryList" required>
   <datalist id="categoryList"><% for (String c : categories) { %><option value="<%= h(c) %>"><% } %></datalist>
  </div>
  <button class="ev-btn">Add to shortlist</button>
 </form>
 <p class="ev-note">The name and organization are copied from the nominee's Module 3 profile.</p>
 <% } %>
</section>

<section class="ev-card">
 <div class="ev-card-head">
  <h2>Candidates</h2>
  <form method="get" action="/evaluation/shortlist" class="ev-inline">
   <input class="ev-input short" name="category" value="<%= h(category) %>" placeholder="Filter by category" list="categoryList" aria-label="Filter by category">
   <button class="ev-btn ghost small">Filter</button>
   <% if (category != null && !category.isBlank()) { %><a class="ev-small" href="/evaluation/shortlist">Show all</a><% } %>
  </form>
 </div>
 <% if (entries.isEmpty()) { %>
  <div class="ev-empty">No candidates<%= category != null && !category.isBlank() ? " in this category" : " yet" %>.</div>
 <% } else { %>
 <div class="ev-table-wrap">
 <table class="ev-table">
  <tr><th>Candidate</th><th>Category</th><th>Status</th><th></th></tr>
  <% for (ShortlistEntry e : entries) {
         String st = e.getStatus().name(); %>
   <tr>
    <td><span class="ev-person"><a href="/evaluation/candidates/<%= e.getId() %>"><%= h(e.getNomineeName()) %></a><small>Nominee #<%= e.getNomineeId() %>, <%= h(e.getOrganization()) %></small></span></td>
    <td><%= h(e.getCategoryName()) %></td>
    <td><span class="ev-pill <%= st.toLowerCase() %>"><%= st.charAt(0) + st.substring(1).toLowerCase() %></span>
     <% if (e.getReason() != null) { %><div class="ev-small ev-muted">Reason: <%= h(e.getReason()) %></div><% } %></td>
    <td>
     <div class="ev-actions">
      <a class="ev-btn small" href="/evaluation/candidates/<%= e.getId() %>">Score sheet</a>
      <% if ("SHORTLISTED".equals(st)) { %>
       <form class="ev-inline" method="post" action="/evaluation/shortlist/<%= e.getId() %>/disqualify">
        <input class="ev-input reason" name="reason" placeholder="Reason" required aria-label="Reason for disqualifying <%= h(e.getNomineeName()) %>">
        <button class="ev-btn danger small">Disqualify</button>
       </form>
      <% } else if ("DISQUALIFIED".equals(st)) { %>
       <form class="ev-inline" method="post" action="/evaluation/shortlist/<%= e.getId() %>/reinstate"><button class="ev-btn ghost small">Reinstate</button></form>
      <% } %>
      <form class="ev-inline" method="post" action="/evaluation/shortlist/<%= e.getId() %>/delete" onsubmit="return confirm('Remove from the shortlist? The marks are removed too.');">
       <button class="ev-btn danger small">Remove</button>
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
