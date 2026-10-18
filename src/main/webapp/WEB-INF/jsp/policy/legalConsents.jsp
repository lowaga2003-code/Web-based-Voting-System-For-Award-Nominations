<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.pation.awardnomination.policy.*" %>
<%@ page import="com.pation.awardnomination.admin.User" %>
<%@ page import="java.util.*" %>
<%--
  MODULE 6 - consent records for the Legal & Governance Officer / System Administrator
  ("Read": which policy version did a user agree to, and their consent history).
  Without a search: the latest 50 consents of all users. With ?q= (user id, e-mail or username): one user.
  Model: q, activeConsents, withdrawnConsents, records,
         searched + found + currentConsents + mustAccept + requests (search) / people (latest list)
--%>
<%!
    private static String person(Map<Long, User> people, Long id) {
        User u = people == null ? null : people.get(id);
        if (u == null) {
            return "<span class=\"pm-person\">User #" + id + "<small>Account removed</small></span>";
        }
        return "<span class=\"pm-person\"><a href=\"/legal/consents?q=" + id + "\">" + h(u.getUsername()) + "</a><small>"
                + h(u.getEmail()) + ", #" + id + "</small></span>";
    }

    private static long num(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
%>
<%
    String q = (String) request.getAttribute("q");
    if (q == null) { q = ""; }
    boolean searched = Boolean.TRUE.equals(request.getAttribute("searched"));
    User found = (User) request.getAttribute("found");
    List<ConsentRecord> records = (List<ConsentRecord>) request.getAttribute("records");
    Map<PolicyType, ConsentRecord> currentConsents = (Map<PolicyType, ConsentRecord>) request.getAttribute("currentConsents");
    List<PolicyDocument> mustAccept = (List<PolicyDocument>) request.getAttribute("mustAccept");
    List<DeletionRequest> requests = (List<DeletionRequest>) request.getAttribute("requests");
    Map<Long, User> people = (Map<Long, User>) request.getAttribute("people");
    if (records == null) { records = Collections.emptyList(); }
    if (currentConsents == null) { currentConsents = Collections.emptyMap(); }
    if (mustAccept == null) { mustAccept = Collections.emptyList(); }
    if (requests == null) { requests = Collections.emptyList(); }
    long activeConsents = num(request.getAttribute("activeConsents"));
    long withdrawnConsents = num(request.getAttribute("withdrawnConsents"));

    String pmTitle = "Consent records";
    String pmNav = "consents";
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
            <h1>Consent records</h1>
            <p class="pm-lead">Who agreed to which version, when and where. Records are never deleted: an approved
                withdrawal marks them as withdrawn, so they stay as proof.</p>
        </div>
    </div>

    <form class="pm-panel pm-search" method="get" action="/legal/consents" role="search">
        <label class="pm-label" for="q">Find a user</label>
        <div class="pm-search-row">
            <input class="pm-input" id="q" name="q" value="<%= h(q) %>" placeholder="User id, e-mail address or username" autocomplete="off">
            <button type="submit" class="pm-btn pm-btn-primary">Find user</button>
            <% if (!q.isEmpty()) { %><a class="pm-btn" href="/legal/consents">Show latest</a><% } %>
        </div>
    </form>

    <% if (searched && found == null) { %>
    <div class="pm-empty">
        <p>No account matches &ldquo;<%= h(q) %>&rdquo;.</p>
        <p>Try the user id number, the full e-mail address or the exact username.</p>
    </div>
    <% } else if (searched) { %>
    <div class="pm-stack">
    <div class="pm-grid">
        <div class="pm-stack">
            <section class="pm-panel" aria-labelledby="userTitle">
                <div class="pm-panel-head">
                    <div>
                        <h2 id="userTitle"><%= h(found.getUsername()) %></h2>
                        <p class="pm-small pm-muted pm-mt-xs"><%= h(found.getEmail()) %>, user #<%= found.getId() %>, <%= h(PolicyAccounts.roleLabel(found)) %><%= found.isActive() ? "" : ", account disabled" %></p>
                    </div>
                    <% if (mustAccept.isEmpty()) { %>
                    <span class="pm-badge is-current">Agrees to the current documents</span>
                    <% } else { %>
                    <span class="pm-badge is-pending">Has to agree to <%= mustAccept.size() == 1 ? h(mustAccept.get(0).getPolicyType().getDisplayName()) + " " + h(mustAccept.get(0).getVersionLabel()) : "the current documents" %></span>
                    <% } %>
                </div>
                <dl class="pm-facts">
                    <% for (PolicyType type : PolicyType.values()) {
                           ConsentRecord c = currentConsents.get(type); %>
                    <div>
                        <dt><%= h(type.getDisplayName()) %></dt>
                        <dd><% if (c == null) { %>No active consent<% } else { %>Version <a href="/policy/view/<%= c.getPolicyId() %>"><%= h(c.getPolicyVersion()) %></a>, <%= h(PolicyHtml.dateTime(c.getAgreedAt())) %><% } %></dd>
                    </div>
                    <% } %>
                </dl>
            </section>
        </div>

        <aside class="pm-stack">
            <section class="pm-panel" aria-labelledby="reqTitle">
                <div class="pm-panel-head">
                    <h2 id="reqTitle">Requests</h2>
                    <span class="pm-source">From <code>data_deletion_requests</code></span>
                </div>
                <% if (requests.isEmpty()) { %>
                <p class="pm-muted pm-m0">This user hasn't sent any request.</p>
                <% } else { %>
                <ul class="pm-requests-mini">
                    <% for (DeletionRequest r : requests) {
                           String badge = r.getStatus() == RequestStatus.PENDING ? "is-pending" : (r.getStatus() == RequestStatus.APPROVED ? "is-approved" : "is-rejected"); %>
                    <li>
                        <div>
                            <strong><%= h(r.getRequestType().getDisplayName()) %></strong>
                            <div class="pm-small pm-muted"><span class="pm-code"><%= h(r.getCode()) %></span>, <%= h(PolicyHtml.date(r.getRequestedAt())) %></div>
                        </div>
                        <span class="pm-badge <%= badge %>"><%= h(r.getStatus().getLabel()) %></span>
                    </li>
                    <% } %>
                </ul>
                <p class="pm-after"><a href="/legal/requests?status=ALL">Open data requests</a></p>
                <% } %>
            </section>
        </aside>
    </div>
    <section class="pm-panel" aria-labelledby="historyTitle">
        <div class="pm-panel-head">
            <h2 id="historyTitle">Consent history</h2>
            <span class="pm-source"><%= records.size() %> <%= records.size() == 1 ? "row" : "rows" %> in <code>consent_records</code> for user_id <%= found.getId() %></span>
        </div>
        <% if (records.isEmpty()) { %>
        <div class="pm-empty"><p>This user hasn't agreed to any document yet.</p></div>
        <% } else { %>
        <div class="pm-table-wrap">
            <table class="pm-table">
                <thead>
                <tr><th>Record</th><th>Document</th><th>Version</th><th>Where</th><th>Agreed on</th><th>IP address</th><th>Status</th></tr>
                </thead>
                <tbody>
                <% for (ConsentRecord r : records) { %>
                <tr>
                    <td class="pm-code"><%= h(r.getCode()) %></td>
                    <td><%= h(r.getPolicyType().getDisplayName()) %></td>
                    <td><a class="pm-version" href="/policy/view/<%= r.getPolicyId() %>"><%= h(r.getPolicyVersion()) %></a></td>
                    <td class="pm-nowrap"><%= h(r.getConsentType().getDisplayName()) %></td>
                    <td class="pm-nowrap"><%= h(PolicyHtml.dateTime(r.getAgreedAt())) %></td>
                    <td class="pm-small"><%= r.getIpAddress() == null ? "<span class=\"pm-muted\">Removed</span>" : h(r.getIpAddress()) %></td>
                    <td>
                        <span class="pm-badge <%= r.isActive() ? "is-active" : "is-withdrawn" %>"><%= h(r.getStatus().getLabel()) %></span>
                        <% if (!r.isActive() && r.getWithdrawnAt() != null) { %><div class="pm-small pm-muted pm-mt-xs pm-nowrap"><%= h(PolicyHtml.dateTime(r.getWithdrawnAt())) %></div><% } %>
                    </td>
                </tr>
                <% } %>
                </tbody>
            </table>
        </div>
        <% } %>
    </section>
    </div>
    <% } else { %>
    <section class="pm-panel" aria-labelledby="latestTitle">
        <div class="pm-panel-head">
            <h2 id="latestTitle">Latest consents</h2>
            <span class="pm-source"><%= activeConsents %> active and <%= withdrawnConsents %> withdrawn in <code>consent_records</code></span>
        </div>
        <% if (records.isEmpty()) { %>
        <div class="pm-empty"><p>No consent has been recorded yet.</p><p>Records appear when users register, sign in or vote.</p></div>
        <% } else { %>
        <div class="pm-table-wrap">
            <table class="pm-table">
                <thead>
                <tr><th>Record</th><th>User</th><th>Document</th><th>Version</th><th>Where</th><th>Agreed on</th><th>Status</th></tr>
                </thead>
                <tbody>
                <% for (ConsentRecord r : records) { %>
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
        <% } %>
    </section>
    <% } %>
</main>

<%@ include file="_pmFoot.jspf" %>
</body>
</html>
