<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.pation.awardnomination.policy.*" %>
<%@ page import="java.util.*" %>
<%--
  MODULE 6 - "consent is required before the user can continue".
  PolicyConsentGate sends users here when they have not agreed to the current Privacy Policy / Terms
  (new account, withdrawn consent, or a major update). Ticking "I agree" saves one row per document
  in consent_records (consent_type = SIGN_IN) and goes back to the page the user wanted ("next").
  Minor updates are shown too, but the user may skip them ("Not now").
  Model: toAccept, mandatory, accepted, next
--%>
<%
    List<PolicyDocument> toAccept = (List<PolicyDocument>) request.getAttribute("toAccept");
    if (toAccept == null) {
        toAccept = Collections.emptyList();
    }
    boolean mandatory = Boolean.TRUE.equals(request.getAttribute("mandatory"));
    Map<PolicyType, ConsentRecord> accepted = (Map<PolicyType, ConsentRecord>) request.getAttribute("accepted");
    if (accepted == null) {
        accepted = Collections.emptyMap();
    }
    String next = (String) request.getAttribute("next");
    if (next == null) {
        next = "/dashboard";
    }

    boolean anyUpdate = false;       // true if the user agreed to an older version of one of the documents
    for (PolicyDocument d : toAccept) {
        if (accepted.get(d.getPolicyType()) != null) {
            anyUpdate = true;
        }
    }
    String names = pmDocumentNames(toAccept);
    String pmTitle = mandatory ? "Before you continue" : "Updated documents";
    String pmNav = "";
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

<div class="pm-narrow" data-pm-stamp-scope>
    <div class="pm-head">
        <div>
            <h1><%= h(pmTitle) %></h1>
            <% if (mandatory && !anyUpdate) { %>
            <p class="pm-lead">To use the Award Nomination System, please read and agree to <%= names %>.</p>
            <% } else if (mandatory) { %>
            <p class="pm-lead">We changed <%= names %>. Read what changed, then agree to the new version to keep using the system.</p>
            <% } else { %>
            <p class="pm-lead">We updated <%= names %>. You don't have to agree again, but you can confirm that you have read the new version.</p>
            <% } %>
        </div>
    </div>

    <div class="pm-stack">
        <% for (PolicyDocument d : toAccept) {
               ConsentRecord before = accepted.get(d.getPolicyType()); %>
        <section class="pm-panel pm-review" aria-labelledby="doc-<%= d.getId() %>">
            <div class="pm-review-head">
                <%= pmSeal(d, true) %>
                <div>
                    <h2 id="doc-<%= d.getId() %>"><%= h(d.getTitle()) %></h2>
                    <p class="pm-muted pm-small pm-mt-xs">
                        Version <%= h(d.getVersionLabel()) %>, published <%= h(PolicyHtml.date(d.getPublishedAt())) %>.
                        <% if (before != null) { %>
                        You agreed to version <%= h(before.getPolicyVersion()) %> on <%= h(PolicyHtml.date(before.getAgreedAt())) %>.
                        <% } %>
                    </p>
                </div>
            </div>
            <% if (before != null && d.getChangeSummary() != null && !d.getChangeSummary().isBlank()) { %>
            <p class="pm-changed"><strong>What changed</strong><%= h(d.getChangeSummary()) %></p>
            <% } %>
            <div class="pm-reader" tabindex="0" aria-label="<%= h(d.getTitle()) %> text">
                <div class="pm-doc"><%= PolicyHtml.sanitize(d.getContent()) %></div>
            </div>
            <p class="pm-small pm-m0"><a href="/policy/view/<%= d.getId() %>" target="_blank" rel="noopener">Open the <%= h(d.getPolicyType().getDisplayName()) %> in a new tab</a></p>
        </section>
        <% } %>
    </div>

    <form class="pm-agree is-sticky" method="post" action="/privacy/consent">
        <input type="hidden" name="next" value="<%= h(next) %>">
        <% for (PolicyDocument d : toAccept) { %>
        <input type="hidden" name="policyId" value="<%= d.getId() %>">
        <% } %>
        <label class="pm-check">
            <input type="checkbox" name="agree" value="yes" required data-pm-agree>
            <span>I have read and agree to <%= names %>.</span>
        </label>
        <div class="pm-actions">
            <button type="submit" class="pm-btn pm-btn-primary" data-pm-agree-button>Agree and continue</button>
            <% if (!mandatory) { %>
            <a class="pm-btn" href="<%= h(next) %>">Not now</a>
            <% } %>
        </div>
        <% if (mandatory) { %>
        <p class="pm-small pm-muted pm-m0">If you don't agree, you can't use the rest of the system. You can still read the documents
            and <a href="/privacy/my#requests">ask for your data to be deleted</a>.</p>
        <% } %>
    </form>
</div>
</main>

<%@ include file="_pmFoot.jspf" %>
</body>
</html>
