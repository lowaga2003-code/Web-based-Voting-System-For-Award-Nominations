<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.pation.awardnomination.dispute.DisputeTicket" %>
<%@ page import="com.pation.awardnomination.dispute.TicketStatus" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Compliance Auditor Hub</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f9f9f9; color: #333; }
        .nav-bar { background-color: #343a40; padding: 12px 20px; border-radius: 4px; margin-bottom: 25px; }
        .nav-bar a { color: #fff; text-decoration: none; margin-right: 20px; font-weight: bold; font-size: 14px; }
        .nav-bar a:hover { color: #ffc107; }
        .nav-bar .active { color: #ffc107; text-decoration: underline; }

        .metrics-container { display: flex; gap: 15px; margin-bottom: 25px; flex-wrap: wrap; }
        .metric-card { flex: 1; min-width: 180px; background: white; padding: 18px; border-radius: 5px; box-shadow: 2px 2px 10px rgba(0,0,0,0.08); border-top: 4px solid #007bff; }
        .metric-card.pending { border-top-color: #e67e22; }
        .metric-card.investigation { border-top-color: #17a2b8; }
        .metric-card.resolved { border-top-color: #28a745; }
        .metric-title { font-size: 13px; color: #666; text-transform: uppercase; font-weight: bold; }
        .metric-value { font-size: 28px; font-weight: bold; margin: 8px 0; color: #222; }

        .filter-bar { margin-bottom: 15px; }
        .filter-btn { padding: 8px 16px; margin-right: 8px; border-radius: 3px; border: 1px solid #ccc; background: white; cursor: pointer; text-decoration: none; color: #333; font-size: 13px; font-weight: bold; display: inline-block; }
        .filter-btn.active { background-color: #007bff; color: white; border-color: #007bff; }

        table { width: 100%; border-collapse: collapse; background: white; box-shadow: 2px 2px 10px rgba(0,0,0,0.1); }
        th, td { padding: 12px; border: 1px solid #ddd; text-align: left; vertical-align: middle; }
        th { background-color: #007bff; color: white; }

        .status-pending { color: #e67e22; font-weight: bold; }
        .status-investigation { color: #007bff; font-weight: bold; }
        .status-resolved { color: #28a745; font-weight: bold; }

        .btn { padding: 6px 12px; text-decoration: none; border-radius: 3px; border: none; cursor: pointer; display: inline-block; font-size: 12px; font-weight: bold; }
        .btn-green { background-color: #28a745; color: white; }
        .btn-blue { background-color: #007bff; color: white; }
        .btn-red { background-color: #dc3545; color: white; }

        .alert-success { padding: 12px; background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; border-radius: 4px; margin-bottom: 20px; }
        .alert-error { padding: 12px; background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; border-radius: 4px; margin-bottom: 20px; }

        .action-cell { white-space: nowrap; }
        .action-cell form { display: inline-block; margin: 0; }
    </style>
</head>
<body>

<!-- Navigation Bar -->
<div class="nav-bar">
    <a href="/dashboard">🏠 Main Dashboard</a> <%-- [INTEGRATION] back to the main system --%>
    <a href="/dispute/list">🗳️ Voter Disputes Portal</a>
    <a href="/dispute/form">+ Submit Dispute Ticket</a>
    <a href="/auditor/dashboard" class="active">🛡️ Compliance Auditor Hub</a>
    <a href="/dispute/logs">📜 Transparency Ledger</a>
</div>

<h2>Compliance Auditor Dispute Management Hub</h2>
<p style="color: #666;">
    Review reported voting anomalies, correlate with system administrator logs, escalate investigations, archive verified resolutions, and manage archive records.
</p>

<%
    String successMsg = (String) request.getAttribute("successMessage");
    if (successMsg != null) {
%>
    <div class="alert-success"><%= successMsg %></div>
<%
    }
    String errorMsg = (String) request.getAttribute("errorMessage");
    if (errorMsg != null) {
%>
    <div class="alert-error"><%= errorMsg %></div>
<%
    }
%>

<!-- Metrics Grid -->
<%
    Map<String, Long> metrics = (Map<String, Long>) request.getAttribute("metrics");
    long total = (metrics != null && metrics.get("total") != null) ? metrics.get("total") : 0;
    long pending = (metrics != null && metrics.get("pending") != null) ? metrics.get("pending") : 0;
    long investigating = (metrics != null && metrics.get("underInvestigation") != null) ? metrics.get("underInvestigation") : 0;
    long resolved = (metrics != null && metrics.get("resolved") != null) ? metrics.get("resolved") : 0;
%>
<div class="metrics-container">
    <div class="metric-card">
        <div class="metric-title">Total Disputes</div>
        <div class="metric-value"><%= total %></div>
    </div>
    <div class="metric-card pending">
        <div class="metric-title">Pending Review</div>
        <div class="metric-value" style="color: #e67e22;"><%= pending %></div>
    </div>
    <div class="metric-card investigation">
        <div class="metric-title">Under Investigation</div>
        <div class="metric-value" style="color: #007bff;"><%= investigating %></div>
    </div>
    <div class="metric-card resolved">
        <div class="metric-title">Resolved &amp; Archived</div>
        <div class="metric-value" style="color: #28a745;"><%= resolved %></div>
    </div>
</div>

<!-- Status Filter Tabs -->
<%
    String currentStatus = (String) request.getAttribute("currentStatus");
    if (currentStatus == null) currentStatus = "ALL";
%>
<div class="filter-bar">
    <a href="/auditor/dashboard?status=ALL" class="filter-btn <%= "ALL".equals(currentStatus) ? "active" : "" %>">All Disputes (<%= total %>)</a>
    <a href="/auditor/dashboard?status=PENDING" class="filter-btn <%= "PENDING".equals(currentStatus) ? "active" : "" %>">Pending Review (<%= pending %>)</a>
    <a href="/auditor/dashboard?status=UNDER_INVESTIGATION" class="filter-btn <%= "UNDER_INVESTIGATION".equals(currentStatus) ? "active" : "" %>">Under Investigation (<%= investigating %>)</a>
    <a href="/auditor/dashboard?status=RESOLVED" class="filter-btn <%= "RESOLVED".equals(currentStatus) ? "active" : "" %>">Resolved &amp; Archived (<%= resolved %>)</a>
</div>

<!-- Disputes Table -->
<table>
    <thead>
    <tr>
        <th>Ticket Code</th>
        <th>Voter</th>
        <th>Category</th>
        <th>Title / Subject</th>
        <th>Submitted Date</th>
        <th>Status</th>
        <th style="width: 200px;">Actions</th>
    </tr>
    </thead>
    <tbody>
    <%
        List<DisputeTicket> tickets = (List<DisputeTicket>) request.getAttribute("tickets");
        if (tickets != null && !tickets.isEmpty()) {
            for (DisputeTicket t : tickets) {
                String statusClass = "status-pending";
                if (t.getStatus() == TicketStatus.UNDER_INVESTIGATION) {
                    statusClass = "status-investigation";
                } else if (t.getStatus() == TicketStatus.RESOLVED) {
                    statusClass = "status-resolved";
                }
    %>
    <tr>
        <td><strong><%= t.getTicketCode() %></strong></td>
        <td><%= t.getUser().getFullName() %> (<%= t.getUser().getUsername() %>)</td>
        <td><%= t.getCategory().getFriendlyName() %></td>
        <td><%= t.getTitle() %></td>
        <td><%= t.getCreatedAt().toLocalDate() %> <%= t.getCreatedAt().toLocalTime().toString().substring(0, 5) %></td>
        <td class="<%= statusClass %>"><%= t.getStatus().getLabel() %></td>
        <td class="action-cell">
            <a href="/dispute/detail/<%= t.getId() %>?role=AUDITOR" class="btn btn-blue">Inspect / Review</a>
            
            <!-- DELETE OPTION FOR AUDITOR: Only available for ARCHIVED/RESOLVED tickets (Requirement 3) -->
            <% if (t.getStatus() == TicketStatus.RESOLVED) { %>
            <form action="/auditor/dispute/delete" method="POST" onsubmit="return confirm('Are you sure you want to permanently delete archived dispute record <%= t.getTicketCode() %>?');">
                <input type="hidden" name="ticketId" value="<%= t.getId() %>">
                <input type="hidden" name="auditorId" value="<%= session.getAttribute("disputeUserId") %>"> <%-- [INTEGRATION] logged-in auditor (was hard-coded 3) --%>
                <button type="submit" class="btn btn-red">Delete Archive</button>
            </form>
            <% } %>
        </td>
    </tr>
    <%
            }
        } else {
    %>
    <tr>
        <td colspan="7" style="text-align: center; color: #888; padding: 25px;">No dispute records found for the selected filter.</td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>

</body>
</html>
