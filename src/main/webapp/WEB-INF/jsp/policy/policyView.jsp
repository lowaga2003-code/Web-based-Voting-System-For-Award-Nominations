<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.pation.awardnomination.policy.*" %>
<%@ page import="java.util.*" %>
<%--
  MODULE 6 - Policy & Consent Management: read a document ("Read" - viewing the privacy terms).
  /policy/privacy, /policy/terms = current version (public, linked from the sign-up form)
  /policy/view/{id}              = one exact version, e.g. the version a user agreed to
  Model: doc, policyType, current, versions, myConsents (signed-in users only)
--%>
<%
    PolicyDocument doc = (PolicyDocument) request.getAttribute("doc");
    PolicyType policyType = (PolicyType) request.getAttribute("policyType");
    PolicyDocument current = (PolicyDocument) request.getAttribute("current");
    List<PolicyDocument> versions = (List<PolicyDocument>) request.getAttribute("versions");
    if (versions == null) {
        versions = Collections.emptyList();
    }
    Map<PolicyType, ConsentRecord> myConsents = (Map<PolicyType, ConsentRecord>) request.getAttribute("myConsents");
    boolean signedIn = request.getAttribute("pmAccount") != null;
    boolean staffView = Boolean.TRUE.equals(request.getAttribute("pmStaff"));
    boolean legalView = Boolean.TRUE.equals(request.getAttribute("pmLegal"));

    String pmTitle = doc != null ? doc.getTitle() : "Document not found";
    String pmNav = staffView ? "policies" : (policyType == PolicyType.TERMS_AND_CONDITIONS ? "terms" : "privacy");
    ConsentRecord mine = (myConsents != null && policyType != null) ? myConsents.get(policyType) : null;

    // the version that replaced this one (versions are listed newest first)
    PolicyDocument successor = null;
    if (doc != null) {
        for (int i = 1; i < versions.size(); i++) {
            if (versions.get(i).getId().equals(doc.getId())) {
                successor = versions.get(i - 1);
            }
        }
    }
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

<% if (doc == null) { %>
    <div class="pm-head">
        <div>
            <h1>Document not found</h1>
            <p class="pm-lead">This version doesn't exist, or it is a draft that hasn't been published yet.</p>
        </div>
    </div>
    <div class="pm-actions">
        <a class="pm-btn pm-btn-primary" href="/policy/privacy">Read the Privacy Policy</a>
        <a class="pm-btn" href="/policy/terms">Read the Terms &amp; Conditions</a>
    </div>
<% } else { %>

    <% if (doc.isDraft()) { %>
    <div class="pm-notice is-warn pm-notice-row">
        <p><strong>Draft preview.</strong> Users can't see this text until the Legal &amp; Governance Officer publishes it.</p>
        <% if (legalView) { %><a class="pm-btn pm-btn-small" href="/legal/policies/<%= doc.getId() %>/edit">Back to the editor</a><% } %>
    </div>
    <% } else if (!doc.isCurrent() && current != null) { %>
    <div class="pm-notice is-info pm-banner-archived">
        <p>You are reading version <%= h(doc.getVersionLabel()) %><%= successor != null ? ", which was replaced on " + h(PolicyHtml.date(successor.getPublishedAt())) : "" %>.
            The current version is <%= h(current.getVersionLabel()) %>.</p>
        <a class="pm-btn pm-btn-small" href="/policy/<%= h(policyType.getSlug()) %>">Read the current version</a>
    </div>
    <% } %>

    <div class="pm-grid">
        <article class="pm-sheet" aria-labelledby="docTitle">
            <div class="pm-sheet-head">
                <div>
                    <h1 class="pm-sheet-title" id="docTitle"><%= h(doc.getTitle()) %></h1>
                    <div class="pm-sheet-meta">
                        <% if (doc.isDraft()) { %>
                        <span>Draft, last saved <%= h(PolicyHtml.when(doc.getUpdatedAt() != null ? doc.getUpdatedAt() : doc.getCreatedAt())) %></span>
                        <% } else if (doc.isCurrent()) { %>
                        <span>In effect since <%= h(PolicyHtml.date(doc.getPublishedAt())) %></span>
                        <% } else { %>
                        <span>In effect from <%= h(PolicyHtml.date(doc.getPublishedAt())) %><%= successor != null ? " until " + h(PolicyHtml.date(successor.getPublishedAt())) : "" %></span>
                        <% } %>
                        <% if (!doc.getTitle().equalsIgnoreCase(policyType.getDisplayName())) { %><span><%= h(policyType.getDisplayName()) %></span><% } %>
                    </div>
                </div>
                <%= pmSeal(doc, false) %>
            </div>
            <div class="pm-doc">
                <%= PolicyHtml.sanitize(doc.getContent()) %>
            </div>
        </article>

        <aside class="pm-stack pm-margin-col" aria-label="About this document">
            <section class="pm-panel">
                <% if (signedIn) { %>
                <div class="pm-panel-head"><h2>Your consent</h2></div>
                <div>
                    <% if (mine == null) { %>
                    <p>You haven't agreed to the <%= h(policyType.getDisplayName()) %> yet.</p>
                    <a class="pm-btn pm-btn-primary pm-btn-small" href="/privacy/consent?next=/privacy/my">Review and agree</a>
                    <% } else if (mine.getPolicyId() != null && mine.getPolicyId().equals(doc.getId())) { %>
                    <p>You agreed to this version on <%= h(PolicyHtml.when(mine.getAgreedAt())) %>,
                        <%= h(mine.getConsentType().getPhrase()) %>.</p>
                    <span class="pm-badge is-current">Agreed</span>
                    <% } else { %>
                    <p>You agreed to version <%= h(mine.getPolicyVersion()) %> on <%= h(PolicyHtml.when(mine.getAgreedAt())) %>.</p>
                    <% if (doc.isCurrent()) { %>
                    <a class="pm-btn pm-btn-small" href="/privacy/consent?next=/privacy/my">Review this version</a>
                    <% } else { %>
                    <a class="pm-btn pm-btn-small" href="/policy/view/<%= mine.getPolicyId() %>">Read the version you agreed to</a>
                    <% } %>
                    <% } %>
                </div>
                <p class="pm-after pm-small"><a href="/privacy/my">See all your consent records</a></p>
                <% } else { %>
                <div class="pm-panel-head"><h2>Agreeing to this document</h2></div>
                <p>When you create an account you tick a box to agree to this document. The system saves the date, the time and this version number.</p>
                <a class="pm-btn pm-btn-small" href="/register">Create an account</a>
                <% } %>
            </section>

            <section class="pm-panel">
                <div class="pm-panel-head">
                    <h2>Versions</h2>
                </div>
                <% if (versions.isEmpty()) { %>
                <p class="pm-muted">No version has been published yet.</p>
                <% } else { %>
                <ul class="pm-versions">
                    <% for (PolicyDocument v : versions) { %>
                    <li>
                        <a href="/policy/view/<%= v.getId() %>"<%= v.getId().equals(doc.getId()) ? " aria-current=\"true\"" : "" %>>
                            <span class="pm-version"><%= h(v.getVersionLabel()) %></span>
                            <span>Published <%= h(PolicyHtml.date(v.getPublishedAt())) %></span>
                            <small><%= h(v.getStatus().getLabel()) %><%= (v.isRequiresReacceptance() && v.getVersionMajor() != null && v.getVersionMajor() > 1) ? ", major update" : "" %></small>
                        </a>
                    </li>
                    <% } %>
                </ul>
                <% } %>
                <p class="pm-source">From the <code>policy_documents</code> table</p>
            </section>
        </aside>
    </div>
<% } %>
</main>

<%@ include file="_pmFoot.jspf" %>
</body>
</html>
