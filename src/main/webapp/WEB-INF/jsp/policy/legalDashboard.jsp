<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.pation.awardnomination.policy.*" %>
<%@ page import="com.pation.awardnomination.admin.User" %>
<%@ page import="java.util.*" %>
<%--
  MODULE 6 - Legal console overview (Legal & Governance Officer; the System Administrator can look too).
  Every number on this page is read from SQL Server when the page opens (policy_documents,
  consent_records, data_deletion_requests).
  Model: current, drafts, usersOnCurrent, pendingRequests, pendingCount, recentConsents, people,
         activeConsents, withdrawnConsents, publishedVersions, draftCount, tables
--%>
<%!
    private static String person(Map<Long, User> people, Long id) {
        User u = people == null ? null : people.get(id);
        if (u == null) {
            return "<span class=\"pm-person\">User #" + id + "<small>Account removed</small></span>";
        }
        return "<span class=\"pm-person\"><span title=\"" + h(u.getEmail()) + "\">" + h(u.getUsername()) + "</span><small>User #" + id + "</small></span>";
    }

    private static long num(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
%>
<%
    Map<PolicyType, PolicyDocument> current = (Map<PolicyType, PolicyDocument>) request.getAttribute("current");
    Map<PolicyType, PolicyDocument> drafts = (Map<PolicyType, PolicyDocument>) request.getAttribute("drafts");
    Map<PolicyType, Long> usersOnCurrent = (Map<PolicyType, Long>) request.getAttribute("usersOnCurrent");
    List<DeletionRequest> pendingRequests = (List<DeletionRequest>) request.getAttribute("pendingRequests");
    List<ConsentRecord> recentConsents = (List<ConsentRecord>) request.getAttribute("recentConsents");
    Map<Long, User> people = (Map<Long, User>) request.getAttribute("people");
    Map<String, Long> tables = (Map<String, Long>) request.getAttribute("tables");
    if (current == null) { current = Collections.emptyMap(); }
    if (drafts == null) { drafts = Collections.emptyMap(); }
    if (usersOnCurrent == null) { usersOnCurrent = Collections.emptyMap(); }
    if (pendingRequests == null) { pendingRequests = Collections.emptyList(); }
    if (recentConsents == null) { recentConsents = Collections.emptyList(); }
    if (tables == null) { tables = Collections.emptyMap(); }
    long pendingCount = num(request.getAttribute("pendingCount"));
    long activeConsents = num(request.getAttribute("activeConsents"));
    long withdrawnConsents = num(request.getAttribute("withdrawnConsents"));
    long publishedVersions = num(request.getAttribute("publishedVersions"));
    long draftCount = num(request.getAttribute("draftCount"));
    boolean legal = Boolean.TRUE.equals(request.getAttribute("pmLegal"));

    String pmTitle = "Legal console";
    String pmNav = "overview";
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
            <h1>Legal console</h1>
            <p class="pm-lead">Privacy Policy and Terms &amp; Conditions versions, users' consent records and their data requests,
                read from SQL Server each time this page opens.</p>
        </div>
        <div class="pm-head-actions">
            <a class="pm-btn" href="/legal/requests">Review requests (<%= pendingCount %>)</a>
            <a class="pm-btn pm-btn-primary" href="/legal/policies"><%= legal ? "Edit policies" : "View policies" %></a>
        </div>
    </div>

    <% if (!legal) { %>
    <div class="pm-notice is-info">
        <p>You are signed in as System Administrator. You can review consent records and approve or reject data requests.
            Only the Legal &amp; Governance Officer can change the Privacy Policy and the Terms &amp; Conditions.</p>
    </div>
    <% } %>

    <div class="pm-grid">
        <div class="pm-stack">
            <section class="pm-panel" aria-labelledby="docsTitle">
                <div class="pm-panel-head">
                    <h2 id="docsTitle">Documents</h2>
                    <span class="pm-source">From <code>policy_documents</code></span>
                </div>
                <div class="pm-table-wrap">
                    <table class="pm-table">
                        <thead>
                        <tr><th>Document</th><th>Live version</th><th>Published</th><th>Users agreed</th><th>Draft</th><th><span class="pm-sr">Actions</span></th></tr>
                        </thead>
                        <tbody>
                        <% for (PolicyType type : PolicyType.values()) {
                               PolicyDocument live = current.get(type);
                               PolicyDocument draft = drafts.get(type);
                               Long agreed = usersOnCurrent.get(type); %>
                        <tr>
                            <td><strong><%= h(type.getDisplayName()) %></strong></td>
                            <% if (live == null) { %>
                            <td class="pm-muted">None</td><td></td><td></td>
                            <% } else { %>
                            <td><a class="pm-version" href="/policy/view/<%= live.getId() %>"><%= h(live.getVersionLabel()) %></a></td>
                            <td><span class="pm-person"><%= h(PolicyHtml.date(live.getPublishedAt())) %><small>by <%= h(live.getPublishedBy()) %></small></span></td>
                            <td class="pm-num"><%= agreed == null ? 0 : agreed %></td>
                            <% } %>
                            <td>
                                <% if (draft != null) { %>
                                <span class="pm-badge is-draft">Draft open</span>
                                <% } else { %>
                                <span class="pm-muted">None</span>
                                <% } %>
                            </td>
                            <td class="pm-nowrap">
                                <% if (legal && draft != null) { %>
                                <a class="pm-btn pm-btn-small" href="/legal/policies/<%= draft.getId() %>/edit">Continue draft</a>
                                <% } else if (legal) { %>
                                <form method="post" action="/legal/policies/create" class="pm-m0">
                                    <input type="hidden" name="type" value="<%= type.name() %>">
                                    <button type="submit" class="pm-btn pm-btn-small">Edit</button>
                                </form>
                                <% } else if (live != null) { %>
                                <a class="pm-btn pm-btn-small" href="/policy/view/<%= live.getId() %>">View</a>
                                <% } %>
                            </td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </section>

            <section class="pm-panel" aria-labelledby="pendingTitle">
                <div class="pm-panel-head">
                    <h2 id="pendingTitle">Waiting for a decision</h2>
                    <span class="pm-source"><%= pendingCount %> pending in <code>data_deletion_requests</code></span>
                </div>
                <% if (pendingRequests.isEmpty()) { %>
                <div class="pm-empty"><p>No request is waiting. New withdrawal and deletion requests appear here.</p></div>
                <% } else { %>
                <ul class="pm-requests-mini">
                    <% for (DeletionRequest r : pendingRequests) { %>
                    <li>
                        <div>
                            <strong><%= h(r.getRequestType().getDisplayName()) %></strong>
                            <div class="pm-small pm-muted"><span class="pm-code"><%= h(r.getCode()) %></span>, sent <%= h(PolicyHtml.when(r.getRequestedAt())) %></div>
                        </div>
                        <%= person(people, r.getUserId()) %>
                    </li>
                    <% } %>
                </ul>
                <p class="pm-after"><a href="/legal/requests">Open all requests</a></p>
                <% } %>
            </section>

            <section class="pm-panel" aria-labelledby="recentTitle">
                <div class="pm-panel-head">
                    <h2 id="recentTitle">Latest consents</h2>
                    <span class="pm-source">From <code>consent_records</code></span>
                </div>
                <% if (recentConsents.isEmpty()) { %>
                <div class="pm-empty"><p>No consent has been recorded yet.</p><p>Records appear when users register, sign in or vote.</p></div>
                <% } else { %>
                <div class="pm-table-wrap">
                    <table class="pm-table">
                        <thead>
                        <tr><th>Record</th><th>User</th><th>Document</th><th>Version</th><th>Where</th><th>Agreed on</th><th>Status</th></tr>
                        </thead>
                        <tbody>
                        <% for (ConsentRecord r : recentConsents) { %>
                        <tr>
                            <td class="pm-code"><%= h(r.getCode()) %></td>
                            <td><%= person(people, r.getUserId()) %></td>
                            <td><%= h(r.getPolicyType().getDisplayName()) %></td>
                            <td><a class="pm-version" href="/policy/view/<%= r.getPolicyId() %>"><%= h(r.getPolicyVersion()) %></a></td>
                            <td class="pm-nowrap"><%= h(r.getConsentType().getDisplayName()) %></td>
                            <td class="pm-nowrap"><%= h(PolicyHtml.dateTime(r.getAgreedAt())) %></td>
                            <td><span class="pm-badge <%= r.isActive() ? "is-active" : "is-withdrawn" %>"><%= h(r.getStatus().getLabel()) %></span></td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
                <p class="pm-after"><a href="/legal/consents">Search consent records</a></p>
                <% } %>
            </section>
        </div>

        <aside class="pm-stack">
            <section class="pm-panel pm-db" aria-labelledby="dbTitle">
                <h2 id="dbTitle">In SQL Server</h2>
                <p class="pm-help pm-mt-xs">Row counts of the Module 6 tables, read live. Open them in SSMS to see the same rows.</p>
                <ul class="pm-db-list">
                    <% for (Map.Entry<String, Long> t : tables.entrySet()) {
                           String detail = "";
                           if ("policy_documents".equals(t.getKey())) {
                               detail = publishedVersions + " published " + (publishedVersions == 1 ? "version" : "versions") + ", " + draftCount + " " + (draftCount == 1 ? "draft" : "drafts");
                           } else if ("consent_records".equals(t.getKey())) {
                               detail = activeConsents + " active, " + withdrawnConsents + " withdrawn";
                           } else if ("data_deletion_requests".equals(t.getKey())) {
                               detail = pendingCount + " pending review";
                           } %>
                    <li>
                        <code><%= h(t.getKey()) %></code>
                        <span class="pm-db-rows"><%= t.getValue() %><small><%= t.getValue() != null && t.getValue() == 1L ? "row" : "rows" %></small></span>
                        <span class="pm-db-detail"><%= h(detail) %></span>
                    </li>
                    <% } %>
                </ul>
                <p class="pm-help pm-after">Each consent, publication and request decision is also sealed in Module 4's
                    <a href="/dispute/logs">transparency ledger</a> (<code>audit_transparency_logs</code>).</p>
            </section>

            <section class="pm-panel" aria-labelledby="roleTitle">
                <div class="pm-panel-head"><h2 id="roleTitle">Who can do what</h2></div>
                <ul class="pm-list">
                    <li>Legal &amp; Governance Officer: edit and publish policies, review requests.</li>
                    <li>System Administrator: read consent records, review requests.</li>
                    <li>Every user: agree to documents, see their own records, send requests.</li>
                </ul>
            </section>
        </aside>
    </div>
</main>

<%@ include file="_pmFoot.jspf" %>
</body>
</html>
