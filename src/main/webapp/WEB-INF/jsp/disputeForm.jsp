<%@ page import="com.pation.awardnomination.dispute.DisputeCategory" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Raise Dispute Ticket</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f9f9f9; color: #333; }
        .nav-bar { background-color: #343a40; padding: 12px 20px; border-radius: 4px; margin-bottom: 25px; }
        .nav-bar a { color: #fff; text-decoration: none; margin-right: 20px; font-weight: bold; font-size: 14px; }
        .nav-bar a:hover { color: #ffc107; }
        .nav-bar .active { color: #ffc107; text-decoration: underline; }

        .form-container { max-width: 600px; border: 1px solid #ccc; padding: 25px; border-radius: 5px; background: white; box-shadow: 2px 2px 10px rgba(0,0,0,0.08); }
        .form-group { margin-bottom: 15px; }
        label { font-weight: bold; display: block; margin-bottom: 6px; }
        input[type="text"], select, textarea { width: 100%; padding: 8px; box-sizing: border-box; border: 1px solid #ccc; border-radius: 4px; font-family: inherit; font-size: 14px; }
        input[type="file"] { width: 100%; padding: 4px 0; }
        
        .btn { padding: 10px 16px; text-decoration: none; border-radius: 3px; border: none; cursor: pointer; font-size: 14px; font-weight: bold; display: inline-block; }
        .btn-green { background-color: #28a745; color: white; }
        .btn-gray { background-color: #6c757d; color: white; }
        
        .alert-error { padding: 12px; background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; border-radius: 4px; margin-bottom: 20px; max-width: 600px; }
    </style>
</head>
<body>

<!-- Navigation Bar -->
<div class="nav-bar">
    <a href="/dashboard">🏠 Main Dashboard</a> <%-- [INTEGRATION] back to the main system --%>
    <a href="/dispute/list">🗳️ Voter Disputes Portal</a>
    <a href="/dispute/form" class="active">+ Submit Dispute Ticket</a>
    <a href="/auditor/dashboard">🛡️ Compliance Auditor Hub</a>
    <a href="/dispute/logs">📜 Transparency Ledger</a>
</div>

<h2>Submit Voting Dispute / Incident Ticket</h2>
<p style="color: #666; max-width: 600px;">
    Use this form to file an official dispute regarding an award voting anomaly, duplicate vote notification, or tally concern.
</p>

<%
    String errorMsg = (String) request.getAttribute("errorMessage");
    if (errorMsg != null) {
%>
    <div class="alert-error"><%= errorMsg %></div>
<%
    }
%>

<div class="form-container">
    <form action="/dispute/submit" method="POST" enctype="multipart/form-data">
        <input type="hidden" name="userId" value="<%= request.getAttribute("userId") != null ? request.getAttribute("userId") : 1 %>">

        <div class="form-group">
            <label for="category">Dispute Category:</label>
            <select name="category" id="category" required>
                <option value="">-- Select Category --</option>
                <%
                    DisputeCategory[] categories = (DisputeCategory[]) request.getAttribute("categories");
                    if (categories != null) {
                        for (DisputeCategory cat : categories) {
                %>
                    <option value="<%= cat.name() %>"><%= cat.getFriendlyName() %> - <%= cat.getDescription() %></option>
                <%
                        }
                    }
                %>
            </select>
        </div>

        <div class="form-group">
            <label for="title">Title / Subject:</label>
            <input type="text" id="title" name="title" required placeholder="e.g., Error prompt while casting Best Actor vote">
        </div>

        <div class="form-group">
            <label for="description">Detailed Concern / Description:</label>
            <textarea id="description" name="description" rows="4" required placeholder="Please describe what occurred during your voting session..."></textarea>
        </div>

        <div class="form-group">
            <label for="evidenceText">Optional Evidence Notes / Codes:</label>
            <textarea id="evidenceText" name="evidenceText" rows="2" placeholder="e.g., Error code ERR-409, receipt number, or timestamp..."></textarea>
        </div>

        <div class="form-group">
            <label for="evidenceFile">Upload Evidence (Receipt, Screenshot, or Letter):</label>
            <input type="file" id="evidenceFile" name="evidenceFile" accept=".pdf, .png, .jpg, .jpeg">
            <small style="color: #666;">Allowed formats: PDF, PNG, JPG (Max 10MB)</small>
        </div>

        <div style="margin-top: 20px;">
            <button type="submit" class="btn btn-green">Submit Dispute Ticket</button>
            <a href="/dispute/list" class="btn btn-gray" style="margin-left: 10px;">Cancel</a>
        </div>
    </form>
</div>

</body>
</html>
