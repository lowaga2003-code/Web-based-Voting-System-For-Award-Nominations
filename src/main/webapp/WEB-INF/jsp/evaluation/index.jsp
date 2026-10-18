<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%--
  MODULE 5 - Selection Committee Chairperson home: the five steps of the evaluation (the system flow in the
  project PDF) with live counts from SQL Server; the first unfinished step is marked.
--%>
<%!
    private static long n(Map<String, Long> m, String key) {
        Long v = m.get(key);
        return v == null ? 0L : v;
    }
%>
<%
    String evTitle = "Overview";
    String evNav = "home";
    Map<String, Long> overview = (Map<String, Long>) request.getAttribute("overview");
    if (overview == null) { overview = new HashMap<String, Long>(); }
    boolean notFound = request.getParameter("notFound") != null;
    boolean[] done = {
        n(overview, "panelPlaces") > 0,
        n(overview, "completeRubrics") > 0,
        n(overview, "shortlisted") + n(overview, "winners") > 0,
        n(overview, "marks") > 0,
        n(overview, "winners") > 0
    };
    int now = -1;
    for (int i = 0; i < done.length; i++) {
        if (!done[i]) { now = i; break; }
    }
    String[] titles = {"Judges and panel", "Rubrics", "Shortlist", "Judges' marks", "Winners"};
    String[] counts = {
        n(overview, "judges") + " judges, " + n(overview, "panelPlaces") + " panel places",
        n(overview, "completeRubrics") + " of " + n(overview, "rubrics") + " at 100%",
        (n(overview, "shortlisted") + n(overview, "winners")) + " candidates, " + n(overview, "disqualified") + " disqualified",
        n(overview, "marks") + " marks given",
        n(overview, "winners") + " winners selected"
    };
    String[] texts = {
        "Add the judges and put them on the panel of each award category.",
        "One rubric per category. The criteria weights must add up to 100%.",
        "Choose nominations from Module 3 that are Under Review or Approved.",
        "Read each candidate's evidence and enter every judge's marks.",
        "Compare the averages and select one winner per category."
    };
    String[] links = {"/evaluation/assignments", "/evaluation/rubrics", "/evaluation/shortlist", "/evaluation/shortlist", "/evaluation/results"};
    String[] linkText = {"Open the judge panel", "Open rubrics", "Open the shortlist", "Choose a candidate", "Open results"};
%>
<%@ include file="/WEB-INF/jsp/evaluation/_evTop.jspf" %>
<% if (notFound) { %><div class="ev-flash err" role="alert"><p>That judge, rubric or candidate no longer exists.</p></div><% } %>
<div class="ev-head">
 <div>
  <h1>Evaluation and selection</h1>
  <p class="ev-lead">Judges score the shortlisted nominations against each category's rubric, and the Chairperson selects the winners.
   Every decision is also written to the <a href="/dispute/logs">transparency ledger</a>.</p>
 </div>
</div>

<% if (n(overview, "conflicts") > 0) { %>
<div class="ev-flash warn" role="alert">
 <p><b><%= n(overview, "conflicts") %> conflict(s) of interest:</b> a judge on the panel works for the same organization as a candidate.
  <a href="/evaluation/assignments">Review the judge panel</a>.</p>
</div>
<% } %>

<ol class="ev-steps" aria-label="Steps of the evaluation">
 <% for (int i = 0; i < titles.length; i++) { %>
 <li class="<%= done[i] ? "done" : (i == now ? "now" : "") %>">
  <b><%= titles[i] %></b>
  <span class="ev-count"><%= h(counts[i]) %><%= done[i] ? "" : (i == now ? ", next step" : "") %></span>
  <span class="ev-small ev-muted"><%= texts[i] %></span>
  <a href="<%= links[i] %>"><%= linkText[i] %></a>
 </li>
 <% } %>
</ol>
<%@ include file="/WEB-INF/jsp/evaluation/_evBottom.jspf" %>
