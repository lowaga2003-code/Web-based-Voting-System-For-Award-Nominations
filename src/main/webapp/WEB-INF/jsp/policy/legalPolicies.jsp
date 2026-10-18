<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.pation.awardnomination.policy.*" %>
<%@ page import="java.util.*" %>
<%--
  MODULE 6 - every version of the Privacy Policy and of the Terms & Conditions ("Update" part of the CRUD).
  Published versions never change (consent records point to them); changes go into a draft that becomes
  the next version when it is published. Only the Legal & Governance Officer sees the edit buttons.
  Model: versions (Map<PolicyType, List<PolicyDocument>>), agreed (policy id -> users who agree to it now)
--%>
<%
    Map<PolicyType, List<PolicyDocument>> versions = (Map<PolicyType, List<PolicyDocument>>) request.getAttribute("versions");
    Map<Long, Long> agreed = (Map<Long, Long>) request.getAttribute("agreed");
    if (versions == null) { versions = Collections.emptyMap(); }
    if (agreed == null) { agreed = Collections.emptyMap(); }
    boolean legal = Boolean.TRUE.equals(request.getAttribute("pmLegal"));

    String pmTitle = "Policies";
    String pmNav = "policies";
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
            <h1>Policies</h1>
            <p class="pm-lead">A published version is never changed, because users' consent records point to it.
                Edits go into a draft, which becomes the next version when you publish it.</p>
        </div>
    </div>

    <% if (!legal) { %>
    <div class="pm-notice is-info">
        <p>Only the Legal &amp; Governance Officer can edit or publish policies. You can read every version.</p>
    </div>
    <% } %>

    <div class="pm-stack">
    <% for (PolicyType type : PolicyType.values()) {
           List<PolicyDocument> list = versions.get(type);
           if (list == null) { list = Collections.emptyList(); }
           PolicyDocument openDraft = null;
           for (PolicyDocument d : list) {
               if (d.isDraft()) { openDraft = d; }
           } %>
        <section class="pm-panel" aria-labelledby="t-<%= type.getSlug() %>">
            <div class="pm-panel-head">
                <div>
                    <h2 id="t-<%= type.getSlug() %>"><%= h(type.getDisplayName()) %></h2>
                    <span class="pm-source"><%= list.size() %> <%= list.size() == 1 ? "row" : "rows" %> in <code>policy_documents</code></span>
                </div>
                <% if (legal) { %>
                <% if (openDraft != null) { %>
                <a class="pm-btn pm-btn-primary" href="/legal/policies/<%= openDraft.getId() %>/edit">Continue the draft</a>
                <% } else { %>
                <form method="post" action="/legal/policies/create" class="pm-m0">
                    <input type="hidden" name="type" value="<%= type.name() %>">
                    <button type="submit" class="pm-btn pm-btn-primary">Edit in a new draft</button>
                </form>
                <% } %>
                <% } %>
            </div>
            <% if (list.isEmpty()) { %>
            <div class="pm-empty"><p>No version yet.</p></div>
            <% } else { %>
            <div class="pm-table-wrap">
                <table class="pm-table">
                    <thead>
                    <tr><th>Version</th><th>Status</th><th>Published</th><th>Users must accept it</th><th>Users agree to it now</th><th><span class="pm-sr">Actions</span></th></tr>
                    </thead>
                    <tbody>
                    <% for (PolicyDocument d : list) {
                           String badge = d.isDraft() ? "is-draft" : (d.isCurrent() ? "is-current" : "is-archived"); %>
                    <tr>
                        <td><span class="pm-version"><%= h(d.getVersionLabel()) %></span></td>
                        <td><span class="pm-badge <%= badge %>"><%= h(d.getStatus().getLabel()) %></span></td>
                        <td>
                            <% if (d.isDraft()) { %>
                            <span class="pm-person">Not published<small>Last saved <%= h(PolicyHtml.dateTime(d.getUpdatedAt() != null ? d.getUpdatedAt() : d.getCreatedAt())) %> by <%= h(d.getUpdatedBy() != null ? d.getUpdatedBy() : d.getCreatedBy()) %></small></span>
                            <% } else { %>
                            <span class="pm-person"><%= h(PolicyHtml.dateTime(d.getPublishedAt())) %><small>by <%= h(d.getPublishedBy()) %></small></span>
                            <% } %>
                        </td>
                        <td>
                            <% if (d.isDraft()) { %>
                            <span class="pm-muted">Chosen when published</span>
                            <% } else if (d.isRequiresReacceptance()) { %>
                            Yes<span class="pm-small pm-muted"><%= d.getVersionMajor() != null && d.getVersionMajor() == 1 && d.getVersionMinor() != null && d.getVersionMinor() == 0 ? ", first version" : ", major update" %></span>
                            <% } else { %>
                            No<span class="pm-small pm-muted">, minor update</span>
                            <% } %>
                        </td>
                        <td class="pm-num"><%= d.isDraft() ? "" : String.valueOf(agreed.containsKey(d.getId()) ? agreed.get(d.getId()) : 0L) %></td>
                        <td class="pm-nowrap">
                            <div class="pm-actions">
                                <% if (d.isDraft() && legal) { %>
                                <a class="pm-btn pm-btn-small pm-btn-primary" href="/legal/policies/<%= d.getId() %>/edit">Edit</a>
                                <% } %>
                                <a class="pm-btn pm-btn-small" href="/policy/view/<%= d.getId() %>"><%= d.isDraft() ? "Preview" : "Read" %></a>
                                <% if (d.isDraft() && legal) { %>
                                <form method="post" action="/legal/policies/<%= d.getId() %>/delete" class="pm-m0">
                                    <button type="submit" class="pm-btn pm-btn-small pm-btn-danger" data-pm-confirm="Delete this draft? The published version stays as it is.">Delete draft</button>
                                </form>
                                <% } %>
                            </div>
                        </td>
                    </tr>
                    <% if (!d.isDraft() && d.getChangeSummary() != null && !d.getChangeSummary().isBlank()) { %>
                    <tr class="pm-subrow"><td></td><td colspan="5" class="pm-small pm-muted">What changed: <%= h(d.getChangeSummary()) %></td></tr>
                    <% } %>
                    <% } %>
                    </tbody>
                </table>
            </div>
            <% } %>
        </section>
    <% } %>
    </div>
</main>

<%@ include file="_pmFoot.jspf" %>
</body>
</html>
