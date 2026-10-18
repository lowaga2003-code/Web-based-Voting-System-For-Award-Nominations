<%@ page import="java.util.List" %>
<%@ page import="com.pation.awardnomination.dispute.DisputeTicket" %>
<%@ page import="com.pation.awardnomination.dispute.DisputeNotification" %>
<%@ page import="com.pation.awardnomination.dispute.TicketStatus" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Dispute &amp; Audit Portal - My Tickets</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f9f9f9; color: #333; }
        .nav-bar { background-color: #343a40; padding: 12px 20px; border-radius: 4px; margin-bottom: 25px; }
        .nav-bar a { color: #fff; text-decoration: none; margin-right: 20px; font-weight: bold; font-size: 14px; }
        .nav-bar a:hover { color: #ffc107; }
        .nav-bar .active { color: #ffc107; text-decoration: underline; }
        
        table { width: 100%; border-collapse: collapse; background: white; box-shadow: 2px 2px 10px rgba(0,0,0,0.1); margin-top: 15px; }
        th, td { padding: 12px; border: 1px solid #ddd; text-align: left; vertical-align: middle; }
        th { background-color: #007bff; color: white; }
        
        .status-pending { color: #e67e22; font-weight: bold; }
        .status-investigation { color: #007bff; font-weight: bold; }
        .status-resolved { color: #28a745; font-weight: bold; }

        .btn { padding: 8px 14px; text-decoration: none; border-radius: 3px; border: none; cursor: pointer; display: inline-block; font-size: 13px; font-weight: bold; }
        .btn-green { background-color: #28a745; color: white; }
        .btn-blue { background-color: #007bff; color: white; }
        .btn-red { background-color: #dc3545; color: white; }
        .btn-gray { background-color: #6c757d; color: white; }
        
        .alert-success { padding: 12px; background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; border-radius: 4px; margin-bottom: 20px; }
        .alert-error { padding: 12px; background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; border-radius: 4px; margin-bottom: 20px; }
        .alert-warning { padding: 12px; background-color: #fff3cd; color: #856404; border: 1px solid #ffeeba; border-radius: 4px; margin-bottom: 20px; }
        
        .action-cell { white-space: nowrap; }
        .action-cell form { display: inline-block; margin: 0; }
    </style>
</head>
<body>

<!-- Navigation Bar -->
<div class="nav-bar">
    <a href="/dashboard">🏠 Main Dashboard</a> <%-- [INTEGRATION] back to the main system --%>
    <a href="/dispute/list" class="active">🗳️ Voter Disputes Portal</a>
    <a href="/dispute/form">+ Submit Dispute Ticket</a>
    <a href="/auditor/dashboard">🛡️ Compliance Auditor Hub</a>
    <a href="/dispute/logs">📜 Transparency Ledger</a>
</div>

<h2>Voting Dispute &amp; Support Management</h2>
<p style="color: #666;">Track your reported voting anomalies, submit additional evidence when requested, or cancel/delete your tickets.</p>

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

<!-- Notification alert if auditor requested evidence -->
<%
    List<DisputeNotification> notifications = (List<DisputeNotification>) request.getAttribute("notifications");
    if (notifications != null && !notifications.isEmpty()) {
        for (DisputeNotification notif : notifications) {
            if ("EVIDENCE_REQUESTED".equals(notif.getType()) && !notif.isRead()) {
%>
    <div class="alert-warning">
        <strong>⚠️ Attention Required:</strong> <%= notif.getMessage() %> 
        &nbsp;&nbsp;
        <a href="/dispute/detail/<%= notif.getTicketId() %>?role=VOTER" class="btn btn-blue" style="padding: 4px 8px; font-size: 12px;">Respond with Evidence</a>
    </div>
<%
            }
        }
    }
%>

<!-- Tickets Table -->
<table>
    <thead>
    <tr>
        <th>Ticket Code</th>
        <th>Category</th>
        <th>Title / Subject</th>
        <th>Evidence File</th>
        <th>Status</th>
        <th>Submitted Date</th>
        <th style="width: 220px;">Actions</th>
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
        <td><%= t.getCategory().getFriendlyName() %></td>
        <td><%= t.getTitle() %></td>
        <td><%= (t.getEvidenceFileName() != null ? t.getEvidenceFileName() : "None") %></td>
        <td class="<%= statusClass %>"><%= t.getStatus().getLabel() %></td>
        <td><%= t.getCreatedAt().toLocalDate() %> <%= t.getCreatedAt().toLocalTime().toString().substring(0, 5) %></td>
        <td class="action-cell">
            <a href="/dispute/detail/<%= t.getId() %>?role=VOTER" class="btn btn-blue" style="padding: 6px 10px; font-size: 12px;">View</a>
            
            <!-- DELETE / CANCEL OPTION FOR USER (Requirement 3) -->
            <form action="/dispute/delete" method="POST" onsubmit="return confirm('Are you sure you want to cancel and delete ticket <%= t.getTicketCode() %>?');">
                <input type="hidden" name="ticketId" value="<%= t.getId() %>">
                <input type="hidden" name="userId" value="<%= t.getUser().getId() %>">
                <button type="submit" class="btn btn-red" style="padding: 6px 10px; font-size: 12px;">Delete / Cancel</button>
            </form>
        </td>
    </tr>
    <%
            }
        } else {
    %>
    <tr>
        <td colspan="7" style="text-align: center; color: #888; padding: 25px;">No dispute tickets submitted yet.</td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>

<br><br>
<a href="/dispute/form" class="btn btn-green">+ Raise New Dispute Ticket</a>

</body>
</html>
