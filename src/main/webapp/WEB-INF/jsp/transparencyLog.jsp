<%@ page import="java.util.List" %>
<%@ page import="com.pation.awardnomination.dispute.AuditTransparencyLog" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Cryptographic Transparency Ledger</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f9f9f9; color: #333; }
        .nav-bar { background-color: #343a40; padding: 12px 20px; border-radius: 4px; margin-bottom: 25px; }
        .nav-bar a { color: #fff; text-decoration: none; margin-right: 20px; font-weight: bold; font-size: 14px; }
        .nav-bar a:hover { color: #ffc107; }
        .nav-bar .active { color: #ffc107; text-decoration: underline; }

        table { width: 100%; border-collapse: collapse; background: white; box-shadow: 2px 2px 10px rgba(0,0,0,0.1); margin-top: 15px; }
        th, td { padding: 12px; border: 1px solid #ddd; text-align: left; vertical-align: top; font-size: 13px; }
        th { background-color: #007bff; color: white; }

        .hash-code { font-family: "Courier New", Courier, monospace; font-size: 11px; background: #eef2f7; padding: 4px 6px; border-radius: 3px; color: #004085; word-break: break-all; }
        .btn { padding: 8px 14px; text-decoration: none; border-radius: 3px; border: none; cursor: pointer; display: inline-block; font-size: 13px; font-weight: bold; }
        .btn-gray { background-color: #6c757d; color: white; }
    </style>
</head>
<body>

<!-- Navigation Bar -->
<div class="nav-bar">
    <a href="/dashboard">🏠 Main Dashboard</a> <%-- [INTEGRATION] back to the main system --%>
    <a href="/dispute/list">🗳️ Voter Disputes Portal</a>
    <a href="/dispute/form">+ Submit Dispute Ticket</a>
    <a href="/auditor/dashboard">🛡️ Compliance Auditor Hub</a>
    <a href="/dispute/logs" class="active">📜 Transparency Ledger</a>
</div>

<h2>Cryptographic Audit &amp; Transparency Ledger</h2>
<p style="color: #666;">
    Every resolved dispute generates an immutable record cryptographically sealed with a SHA-256 digital signature hash to prevent tampering and guarantee electoral audit integrity.
</p>

<table>
    <thead>
    <tr>
        <th>Ticket Code</th>
        <th>Auditor</th>
        <th>Timestamp</th>
        <th>Actions Taken</th>
        <th>Findings &amp; Remarks</th>
        <th style="width: 250px;">SHA-256 Digital Hash</th>
    </tr>
    </thead>
    <tbody>
    <%
        List<AuditTransparencyLog> logs = (List<AuditTransparencyLog>) request.getAttribute("logs");
        if (logs != null && !logs.isEmpty()) {
            for (AuditTransparencyLog log : logs) {
    %>
    <tr>
        <td><strong><%= log.getTicketCode() %></strong></td>
        <td><%= log.getAuditorUsername() %></td>
        <td><%= log.getTimestamp().toLocalDate() %> <%= log.getTimestamp().toLocalTime().toString().substring(0, 8) %></td>
        <td><%= log.getActionTaken() %></td>
        <td>
            <strong>Findings:</strong> <%= log.getInvestigationFindings() %><br>
            <strong>Remarks:</strong> <%= log.getRemarks() %>
        </td>
        <td>
            <span class="hash-code"><%= log.getCryptographicHash() %></span>
        </td>
    </tr>
    <%
            }
        } else {
    %>
    <tr>
        <td colspan="6" style="text-align: center; color: #888; padding: 25px;">No sealed transparency records present yet.</td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>

<br><br>
<a href="/auditor/dashboard" class="btn btn-gray">&larr; Back to Auditor Hub</a>

</body>
</html>
