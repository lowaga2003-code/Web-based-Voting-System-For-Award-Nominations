<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.pation.awardnomination.policy.*" %>
<%@ page import="com.pation.awardnomination.admin.User" %>
<%@ page import="java.util.*" %>
<%--
  MODULE 6 - withdrawal / data deletion requests with Approve and Reject (Legal & Governance Officer or
  System Administrator). Status: Pending review / Approved / Rejected (data_deletion_requests.status).
  Approve WITHDRAW_CONSENT -> the user's consent records are marked withdrawn.
  Approve DELETE_MY_DATA   -> consents withdrawn + IP address / browser removed + sign-in account disabled.
  Model: filter, requests, counts, people, myId
--%>
<%!
    private static String person(Map<Long, User> people, Long id) {
        User u = people == null ? null : people.get(id);
        if (u == null) {
            return "User #" + id + " <span class=\"pm-muted\">(account removed)</span>";
        }
        return "<a href=\"/legal/consents?q=" + id + "\">" + h(u.getUsername()) + "</a> <span class=\"pm-muted\">"
                + h(u.getEmail()) + ", #" + id + "</span>";
    }
%>
<%
    String filter = (String) request.getAttribute("filter");
    if (filter == null) { filter = "PENDING"; }
    List<DeletionRequest> requests = (List<DeletionRequest>) request.getAttribute("requests");
    Map<String, Long> counts = (Map<String, Long>) request.getAttribute("counts");
    Map<Long, User> people = (Map<Long, User>) request.getAttribute("people");
    Long myId = (Long) request.getAttribute("myId");
    if (requests == null) { requests = Collections.emptyList(); }
    if (counts == null) { counts = Collections.emptyMap(); }

    String[][] tabs = {
            {"PENDING", "Pending review"},
            {"APPROVED", "Approved"},
            {"REJECTED", "Rejected"},
            {"ALL", "All"}
    };
    String emptyText = "PENDING".equals(filter) ? "No request is waiting for a decision."
            : ("ALL".equals(filter) ? "No request has been sent yet." : "No request has this status.");

    String pmTitle = "Data requests";
    String pmNav = "requests";
%>
<!DOCTYPE html>
<html lang="en">
<head>
<%@ include file="_pmHead.jspf" %>
</head>
<body class="pm-ui">
<%@ include file="_pmHeader.jspf" %>

<main class="pm-main" id="pm-content">
<%@ include file="_pmFlash.jspf" %>

    <div class="pm-head">
        <div>
            <h1>Data requests</h1>
            <p class="pm-lead">Requests from users to withdraw their consent or delete their personal data.
                Nothing changes until a request is approved here.</p>
        </div>
    </div>

    <nav class="pm-tabs" aria-label="Filter by status">
        <% for (String[] tab : tabs) {
               Long n = counts.get(tab[0]); %>
        <a href="/legal/requests?status=<%= tab[0] %>"<%= tab[0].equals(filter) ? " aria-current=\"page\"" : "" %>><%= h(tab[1]) %> <span><%= n == null ? 0 : n %></span></a>
        <% } %>
    </nav>

    <% if (requests.isEmpty()) { %>
    <div class="pm-empty">
        <p><%= h(emptyText) %></p>
        <p>Users send requests from their My privacy &amp; consent page.</p>
    </div>
    <% } %>

    <% for (DeletionRequest r : requests) {
           String badge = r.getStatus() == RequestStatus.PENDING ? "is-pending" : (r.getStatus() == RequestStatus.APPROVED ? "is-approved" : "is-rejected");
           boolean own = myId != null && myId.equals(r.getUserId());
           boolean deletion = r.getRequestType() == DeletionRequestType.DELETE_MY_DATA; %>
    <article class="pm-panel pm-request" aria-labelledby="req-<%= r.getId() %>">
        <div class="pm-request-head">
            <div>
                <h2 id="req-<%= r.getId() %>"><%= h(r.getRequestType().getDisplayName()) %></h2>
                <p class="pm-small pm-muted pm-mt-xs"><span class="pm-code"><%= h(r.getCode()) %></span> in <code>data_deletion_requests</code></p>
            </div>
            <span class="pm-badge <%= badge %>"><%= h(r.getStatus().getLabel()) %></span>
        </div>

        <div class="pm-request-body">
            <div class="pm-request-info">
                <dl class="pm-facts">
                    <div><dt>From</dt><dd><%= person(people, r.getUserId()) %></dd></div>
                    <div><dt>Sent</dt><dd><%= h(PolicyHtml.when(r.getRequestedAt())) %></dd></div>
                    <% if (!r.isPending()) { %>
                    <div><dt>Decided by</dt><dd><%= h(r.getReviewedBy()) %></dd></div>
                    <div><dt>Decided on</dt><dd><%= h(PolicyHtml.when(r.getReviewedAt())) %></dd></div>
                    <% } %>
                </dl>
                <% if (r.getReason() != null) { %>
                <p class="pm-quote"><%= h(r.getReason()) %></p>
                <% } else { %>
                <p class="pm-muted pm-m0">No reason given.</p>
                <% } %>
            </div>

            <% if (r.isPending() && own) { %>
            <div class="pm-decision">
                <div class="pm-notice is-warn pm-m0">
                    <p>This is your own request. Another Legal &amp; Governance Officer or the System Administrator must decide.</p>
                </div>
            </div>
            <% } else if (r.isPending()) { %>
            <form class="pm-decision" method="post" action="/legal/requests/<%= r.getId() %>/approve">
                <p class="pm-small">
                    <% if (deletion) { %>
                    Approving withdraws all of this user's consents, removes the IP address and browser details from their
                    consent records and disables their sign-in account.
                    <% } else { %>
                    Approving marks all of this user's consent records as withdrawn. They must agree again before they can use the system.
                    <% } %>
                </p>
                <div class="pm-field">
                    <label class="pm-label" for="note-<%= r.getId() %>">Note to the user</label>
                    <textarea class="pm-textarea" id="note-<%= r.getId() %>" name="note" maxlength="1000" rows="2"></textarea>
                    <p class="pm-help">Required when you reject. The user sees it on their My privacy &amp; consent page.</p>
                </div>
                <div class="pm-actions">
                    <button type="submit" class="pm-btn pm-btn-primary"
                            data-pm-confirm="<%= deletion ? "Approve " + h(r.getCode()) + "? The user's sign-in account will be disabled." : "Approve " + h(r.getCode()) + "? The user's consents will be withdrawn." %>">Approve</button>
                    <button type="submit" class="pm-btn pm-btn-danger" formaction="/legal/requests/<%= r.getId() %>/reject"
                            data-pm-needs-note="note-<%= r.getId() %>"
                            data-pm-needs-note-message="Write a note to the user before you reject the request.">Reject</button>
                </div>
            </form>
            <% } else { %>
            <div class="pm-decision">
                <dl class="pm-facts is-stacked">
                    <% if (r.getReviewNote() != null) { %><div><dt>Note to the user</dt><dd><%= h(r.getReviewNote()) %></dd></div><% } %>
                    <% if (r.getOutcome() != null) { %><div><dt>What happened</dt><dd><%= h(r.getOutcome()) %></dd></div><% } %>
                </dl>
            </div>
            <% } %>
        </div>
    </article>
    <% } %>
</main>

<%@ include file="_pmFoot.jspf" %>
</body>
</html>
