<%@ page import="java.util.List" %>
<%@ page import="com.pation.awardnomination.dispute.DisputeTicket" %>
<%@ page import="com.pation.awardnomination.dispute.SystemActivityLog" %>
<%@ page import="com.pation.awardnomination.dispute.TicketStatus" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Dispute Ticket Details</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f9f9f9; color: #333; }
        .nav-bar { background-color: #343a40; padding: 12px 20px; border-radius: 4px; margin-bottom: 25px; }
        .nav-bar a { color: #fff; text-decoration: none; margin-right: 20px; font-weight: bold; font-size: 14px; }
        .nav-bar a:hover { color: #ffc107; }

        .card { background: white; border: 1px solid #ddd; padding: 25px; border-radius: 5px; box-shadow: 2px 2px 10px rgba(0,0,0,0.08); margin-bottom: 25px; }
        .card-header { border-bottom: 2px solid #007bff; padding-bottom: 10px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
        .card-header h3 { margin: 0; color: #007bff; }

        .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 15px; }
        .info-group { margin-bottom: 12px; }
        .info-label { font-weight: bold; color: #555; font-size: 13px; text-transform: uppercase; margin-bottom: 4px; }
        .info-value { font-size: 15px; color: #222; }

        .status-badge { display: inline-block; padding: 4px 10px; border-radius: 3px; font-weight: bold; font-size: 13px; }
        .status-pending { background-color: #ffeeba; color: #856404; }
        .status-investigation { background-color: #bee5eb; color: #0c5460; }
        .status-resolved { background-color: #d4edda; color: #155724; }

        table { width: 100%; border-collapse: collapse; background: white; margin-top: 10px; }
        th, td { padding: 10px; border: 1px solid #ddd; text-align: left; font-size: 13px; }
        th { background-color: #007bff; color: white; }

        .form-group { margin-bottom: 15px; }
        label { font-weight: bold; display: block; margin-bottom: 6px; }
        textarea, input[type="text"] { width: 100%; padding: 8px; box-sizing: border-box; border: 1px solid #ccc; border-radius: 4px; font-family: inherit; font-size: 14px; }

        .btn { padding: 8px 16px; text-decoration: none; border-radius: 3px; border: none; cursor: pointer; font-size: 13px; font-weight: bold; display: inline-block; }
        .btn-green { background-color: #28a745; color: white; }
        .btn-blue { background-color: #007bff; color: white; }
        .btn-red { background-color: #dc3545; color: white; }
        .btn-gray { background-color: #6c757d; color: white; }

        .alert-success { padding: 12px; background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; border-radius: 4px; margin-bottom: 20px; }
        .alert-error { padding: 12px; background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; border-radius: 4px; margin-bottom: 20px; }
        .alert-warning { padding: 12px; background-color: #fff3cd; color: #856404; border: 1px solid #ffeeba; border-radius: 4px; margin-bottom: 20px; }
    </style>
</head>
<body>

<!-- Navigation Bar -->
<div class="nav-bar">
    <a href="/dashboard">🏠 Main Dashboard</a> <%-- [INTEGRATION] back to the main system --%>
    <a href="/dispute/list">🗳️ Voter Disputes Portal</a>
    <a href="/dispute/form">+ Submit Dispute Ticket</a>
    <a href="/auditor/dashboard">🛡️ Compliance Auditor Hub</a>
    <a href="/dispute/logs">📜 Transparency Ledger</a>
</div>

<%
    DisputeTicket ticket = (DisputeTicket) request.getAttribute("ticket");
    String role = (String) request.getAttribute("role");
    if (role == null) role = "VOTER";

    String statusBadgeClass = "status-pending";
    if (ticket.getStatus() == TicketStatus.UNDER_INVESTIGATION) {
        statusBadgeClass = "status-investigation";
    } else if (ticket.getStatus() == TicketStatus.RESOLVED) {
        statusBadgeClass = "status-resolved";
    }

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

<!-- Ticket Overview Card -->
<div class="card">
    <div class="card-header">
        <h3>Ticket: <%= ticket.getTicketCode() %></h3>
        <span class="status-badge <%= statusBadgeClass %>"><%= ticket.getStatus().getLabel() %></span>
    </div>

    <div class="grid-2">
        <div class="info-group">
            <div class="info-label">Voter Name</div>
            <div class="info-value"><%= ticket.getUser().getFullName() %> (<%= ticket.getUser().getEmail() %>)</div>
        </div>
        <div class="info-group">
            <div class="info-label">Category</div>
            <div class="info-value"><%= ticket.getCategory().getFriendlyName() %></div>
        </div>
        <div class="info-group">
            <div class="info-label">Date Submitted</div>
            <div class="info-value"><%= ticket.getCreatedAt().toLocalDate() %> <%= ticket.getCreatedAt().toLocalTime().toString().substring(0, 5) %></div>
        </div>
        <div class="info-group">
            <div class="info-label">Evidence File Attachment</div>
            <div class="info-value"><%= (ticket.getEvidenceFileName() != null ? ticket.getEvidenceFileName() : "None uploaded") %></div>
        </div>
    </div>

    <div class="info-group" style="margin-top: 15px;">
        <div class="info-label">Title / Subject</div>
        <div class="info-value" style="font-weight: bold;"><%= ticket.getTitle() %></div>
    </div>

    <div class="info-group" style="margin-top: 15px;">
        <div class="info-label">Voter's Concern Description</div>
        <div class="info-value" style="background: #f8f9fa; padding: 12px; border-radius: 4px; border: 1px solid #e9ecef;"><%= ticket.getDescription() %></div>
    </div>

    <% if (ticket.getEvidenceText() != null && !ticket.getEvidenceText().isBlank()) { %>
    <div class="info-group" style="margin-top: 15px;">
        <div class="info-label">Voter Evidence Notes</div>
        <div class="info-value" style="background: #f8f9fa; padding: 12px; border-radius: 4px; border: 1px solid #e9ecef;"><%= ticket.getEvidenceText() %></div>
    </div>
    <% } %>

    <!-- Supplemental Evidence Section -->
    <% if (ticket.isAdditionalEvidenceRequested()) { %>
    <div class="alert-warning" style="margin-top: 20px;">
        <strong>Auditor Investigation Request:</strong>
        <p style="margin: 5px 0 0 0;"><%= ticket.getEvidenceRequestNotes() %></p>
    </div>
    <% } %>

    <% if (ticket.getAdditionalEvidenceProvided() != null && !ticket.getAdditionalEvidenceProvided().isBlank()) { %>
    <div class="info-group" style="margin-top: 15px;">
        <div class="info-label" style="color: #007bff;">Supplemental Evidence Submitted by Voter</div>
        <div class="info-value" style="background: #e7f1ff; padding: 12px; border-radius: 4px; border: 1px solid #b8daff;"><%= ticket.getAdditionalEvidenceProvided() %></div>
    </div>
    <% } %>

    <!-- Resolved Section -->
    <% if (ticket.getStatus() == TicketStatus.RESOLVED) { %>
    <div style="background-color: #f1f9f4; border: 1px solid #c3e6cb; border-radius: 4px; padding: 18px; margin-top: 20px;">
        <h4 style="color: #155724; margin-top: 0;">Resolution &amp; Archival Report</h4>
        <div class="info-group">
            <div class="info-label">Auditor Findings</div>
            <div class="info-value"><%= ticket.getInvestigationFindings() %></div>
        </div>
        <div class="info-group">
            <div class="info-label">Actions Executed</div>
            <div class="info-value"><%= ticket.getActionsTaken() %></div>
        </div>
        <div class="info-group">
            <div class="info-label">Final Remarks</div>
            <div class="info-value"><%= ticket.getFinalRemarks() %></div>
        </div>
        <div class="info-group" style="margin-top: 10px;">
            <a href="/dispute/logs" class="btn btn-blue" style="font-size: 12px;">View in Cryptographic Transparency Ledger &rarr;</a>
        </div>
    </div>
    <% } %>

    <!-- DELETE OPTION BUTTONS (Requirement 3) -->
    <div style="margin-top: 25px; padding-top: 15px; border-top: 1px solid #eee; display: flex; justify-content: space-between; align-items: center;">
        <div>
            <% if ("AUDITOR".equals(role)) { %>
                <a href="/auditor/dashboard" class="btn btn-gray">&larr; Back to Auditor Hub</a>
            <% } else { %>
                <a href="/dispute/list" class="btn btn-gray">&larr; Back to My Disputes</a>
            <% } %>
        </div>

        <div>
            <!-- User Delete / Cancel Option -->
            <% if (!"AUDITOR".equals(role)) { %>
            <form action="/dispute/delete" method="POST" style="display: inline-block;" onsubmit="return confirm('Cancel and delete dispute ticket <%= ticket.getTicketCode() %>?');">
                <input type="hidden" name="ticketId" value="<%= ticket.getId() %>">
                <input type="hidden" name="userId" value="<%= ticket.getUser().getId() %>">
                <button type="submit" class="btn btn-red">Cancel &amp; Delete Ticket</button>
            </form>
            <% } %>

            <!-- Auditor Delete Archive Option (Only when Resolved) -->
            <% if ("AUDITOR".equals(role) && ticket.getStatus() == TicketStatus.RESOLVED) { %>
            <form action="/auditor/dispute/delete" method="POST" style="display: inline-block;" onsubmit="return confirm('Permanently delete archived record <%= ticket.getTicketCode() %>?');">
                <input type="hidden" name="ticketId" value="<%= ticket.getId() %>">
                <input type="hidden" name="auditorId" value="<%= session.getAttribute("disputeUserId") %>"> <%-- [INTEGRATION] logged-in auditor (was hard-coded 3) --%>
                <button type="submit" class="btn btn-red">Delete Archived Record</button>
            </form>
            <% } %>
        </div>
    </div>
</div>

<!-- Voter Actions: Submit supplemental evidence or acknowledge -->
<% if (!"AUDITOR".equals(role)) { %>
    <% if (ticket.isAdditionalEvidenceRequested() && ticket.getStatus() != TicketStatus.RESOLVED) { %>
    <div class="card">
        <div class="card-header">
            <h3>Provide Additional Clarification / Evidence</h3>
        </div>
        <form action="/dispute/<%= ticket.getId() %>/evidence" method="POST">
            <input type="hidden" name="userId" value="<%= ticket.getUser().getId() %>">
            <div class="form-group">
                <label for="additionalEvidence">Enter requested evidence, receipt tokens, or details:</label>
                <textarea id="additionalEvidence" name="additionalEvidence" rows="4" required placeholder="Type your response to the Compliance Auditor here..."></textarea>
            </div>
            <button type="submit" class="btn btn-green">Send Supplemental Evidence</button>
        </form>
    </div>
    <% } %>

    <% if (ticket.getStatus() == TicketStatus.RESOLVED && !ticket.isAcknowledgedByUser()) { %>
    <div class="card">
        <div class="card-header">
            <h3>Acknowledge Resolution</h3>
        </div>
        <p>Please review the auditor's findings above and confirm acknowledgment.</p>
        <form action="/dispute/<%= ticket.getId() %>/acknowledge" method="POST">
            <input type="hidden" name="userId" value="<%= ticket.getUser().getId() %>">
            <button type="submit" class="btn btn-green">Acknowledge Resolution</button>
        </form>
    </div>
    <% } %>
<% } %>

<!-- Compliance Auditor Action Forms -->
<% if ("AUDITOR".equals(role) && ticket.getStatus() != TicketStatus.RESOLVED) { %>
<div class="card">
    <div class="card-header">
        <h3>Auditor Investigation &amp; Resolution Actions</h3>
    </div>

    <!-- Branch 1: Escalate to Under Investigation -->
    <div style="margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid #eee;">
        <h4>1. Require Additional Investigation (Extension Flow 6a)</h4>
        <p style="color: #666; font-size: 13px;">Update status to 'Under Investigation' and prompt the voter for further proof or clarification.</p>
        <form action="/auditor/dispute/investigate" method="POST">
            <input type="hidden" name="ticketId" value="<%= ticket.getId() %>">
            <input type="hidden" name="auditorId" value="<%= session.getAttribute("disputeUserId") %>"> <%-- [INTEGRATION] logged-in auditor (was hard-coded 3) --%>
            <div class="form-group">
                <label for="requestNotes">Information / Evidence Needed from Voter:</label>
                <textarea id="requestNotes" name="requestNotes" rows="3" required placeholder="e.g., Please provide confirmation timestamp or screenshot of error message..."></textarea>
            </div>
            <button type="submit" class="btn btn-blue">Escalate to Investigation &amp; Notify Voter</button>
        </form>
    </div>

    <!-- Branch 2: Resolve & Archive -->
    <div>
        <h4>2. Resolve &amp; Archive Dispute (Main Flow Steps 6-9)</h4>
        <p style="color: #666; font-size: 13px;">Finalize investigation, seal resolution into the Immutable Transparency Log with SHA-256 hash, and notify the voter.</p>
        <form action="/auditor/dispute/resolve" method="POST">
            <input type="hidden" name="ticketId" value="<%= ticket.getId() %>">
            <input type="hidden" name="auditorId" value="<%= session.getAttribute("disputeUserId") %>"> <%-- [INTEGRATION] logged-in auditor (was hard-coded 3) --%>
            <div class="form-group">
                <label for="findings">Investigation Findings:</label>
                <textarea id="findings" name="findings" rows="3" required placeholder="Summarize technical findings (e.g., node latency detected; votes verified in master ledger)"></textarea>
            </div>
            <div class="form-group">
                <label for="actionsTaken">Actions Taken:</label>
                <textarea id="actionsTaken" name="actionsTaken" rows="2" required placeholder="Actions executed (e.g., cache cleared, ballot validated)"></textarea>
            </div>
            <div class="form-group">
                <label for="finalRemarks">Final Remarks for Transparency Log:</label>
                <textarea id="finalRemarks" name="finalRemarks" rows="2" required placeholder="Final remarks visible in ledger"></textarea>
            </div>
            <button type="submit" class="btn btn-green">Resolve &amp; Archive Dispute</button>
        </form>
    </div>
</div>
<% } %>

<!-- Associated System Activity Logs (Provided by System Administrator) -->
<div class="card">
    <div class="card-header">
        <h3>Associated System Logs (Provided by System Administrator)</h3>
    </div>
    <p style="color: #666; font-size: 13px;">Audit activity logs captured during voter's session for technical correlation:</p>
    <table>
        <thead>
        <tr>
            <th>Timestamp</th>
            <th>Action Type</th>
            <th>IP Address</th>
            <th>Device Signature</th>
            <th>Details</th>
            <th>Anomaly Status</th>
        </tr>
        </thead>
        <tbody>
        <%
            List<SystemActivityLog> logs = (List<SystemActivityLog>) request.getAttribute("systemLogs");
            if (logs != null && !logs.isEmpty()) {
                for (SystemActivityLog log : logs) {
        %>
        <tr>
            <td><%= log.getTimestamp().toLocalDate() %> <%= log.getTimestamp().toLocalTime().toString().substring(0, 8) %></td>
            <td><strong><%= log.getActionType() %></strong></td>
            <td><%= log.getIpAddress() %></td>
            <td><%= log.getDeviceSignature() %></td>
            <td><%= log.getDetails() %></td>
            <td>
                <% if ("FLAGGED_ANOMALY".equals(log.getAnomalyStatus())) { %>
                    <span style="color: red; font-weight: bold;"><%= log.getAnomalyStatus() %></span>
                <% } else { %>
                    <span style="color: green;"><%= log.getAnomalyStatus() %></span>
                <% } %>
            </td>
        </tr>
        <%
                }
            } else {
        %>
        <tr>
            <td colspan="6" style="text-align: center; color: #888; padding: 15px;">No activity logs recorded for this voter account.</td>
        </tr>
        <%
            }
        %>
        </tbody>
    </table>
</div>

</body>
</html>
