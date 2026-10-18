<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.pation.awardnomination.policy.*" %>
<%@ page import="java.util.*" %>
<%--
  MODULE 6 - the consent checkbox of the voting page (consent_type = VOTING).
  The voting pages (/voter/**) send a voter here first if they haven't given their voting consent for the
  current documents (PolicyConsentGate). After "Agree" a VOTING row per document is saved in consent_records
  and the voter goes on to the voting page ("next").
  Model: missing, currentDocs, votingConsents, next ("" when the page was opened directly)
--%>
<%
    List<PolicyDocument> missing = (List<PolicyDocument>) request.getAttribute("missing");
    Map<PolicyType, PolicyDocument> currentDocs = (Map<PolicyType, PolicyDocument>) request.getAttribute("currentDocs");
    Map<PolicyType, ConsentRecord> votingConsents = (Map<PolicyType, ConsentRecord>) request.getAttribute("votingConsents");
    String next = (String) request.getAttribute("next");
    if (missing == null) { missing = Collections.emptyList(); }
    if (currentDocs == null) { currentDocs = Collections.emptyMap(); }
    if (votingConsents == null) { votingConsents = Collections.emptyMap(); }
    if (next == null) { next = ""; }

    PolicyDocument privacy = currentDocs.get(PolicyType.PRIVACY_POLICY);
    PolicyDocument terms = currentDocs.get(PolicyType.TERMS_AND_CONDITIONS);
    List<PolicyDocument> docs = new ArrayList<PolicyDocument>(currentDocs.values());

    String pmTitle = "Consent for voting";
    String pmNav = "my";
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

<div data-pm-stamp-scope>
    <div class="pm-head">
        <div>
            <h1>Consent for voting</h1>
            <p class="pm-lead">Your votes are saved with your account so that each person votes once in each award category.
                Read how this works, then confirm before you vote.</p>
        </div>
    </div>

    <div class="pm-grid">
        <div class="pm-stack">
            <section class="pm-panel" aria-labelledby="docsTitle">
                <div class="pm-panel-head">
                    <h2 id="docsTitle">Documents that apply to voting</h2>
                </div>
                <% if (docs.isEmpty()) { %>
                <div class="pm-empty"><p>No document has been published yet.</p></div>
                <% } else { %>
                <ul class="pm-doclist">
                    <% for (PolicyDocument d : docs) {
                           ConsentRecord given = votingConsents.get(d.getPolicyType()); %>
                    <li>
                        <%= pmSeal(d, true) %>
                        <div>
                            <strong class="pm-doclist-title"><%= h(d.getTitle()) %></strong>
                            <p class="pm-small pm-muted pm-mt-xs">
                                <% if (given != null) { %>
                                You agreed to version <%= h(given.getPolicyVersion()) %> for voting on <%= h(PolicyHtml.when(given.getAgreedAt())) %>.
                                <% } else { %>
                                Not agreed for voting yet.
                                <% } %>
                            </p>
                            <a class="pm-small" href="/policy/view/<%= d.getId() %>" target="_blank" rel="noopener">View <%= h(d.getPolicyType().getDisplayName()) %></a>
                        </div>
                    </li>
                    <% } %>
                </ul>
                <% } %>
            </section>

            <% if (missing.isEmpty() && !docs.isEmpty()) { %>
            <div class="pm-notice is-info pm-notice-row">
                <p>You have given your consent for voting for the current documents. Nothing else to do.</p>
                <a class="pm-btn pm-btn-small" href="/privacy/my">Back to my privacy page</a>
            </div>
            <% } else if (!docs.isEmpty()) { %>
            <form class="pm-agree" method="post" action="/privacy/voting">
                <input type="hidden" name="next" value="<%= h(next) %>">
                <% for (PolicyDocument d : docs) { %>
                <input type="hidden" name="policyId" value="<%= d.getId() %>">
                <% } %>
                <label class="pm-check">
                    <input type="checkbox" name="agree" value="yes" required data-pm-agree>
                    <span>I agree that my votes are saved and processed as described in the
                        <% if (privacy != null) { %><a href="/policy/view/<%= privacy.getId() %>" target="_blank" rel="noopener">Privacy Policy</a> (version <%= h(privacy.getVersionLabel()) %>)<% } else { %>Privacy Policy<% } %>,
                        and I accept the voting rules in the
                        <% if (terms != null) { %><a href="/policy/view/<%= terms.getId() %>" target="_blank" rel="noopener">Terms &amp; Conditions</a> (version <%= h(terms.getVersionLabel()) %>)<% } else { %>Terms &amp; Conditions<% } %>.</span>
                </label>
                <div class="pm-actions">
                    <button type="submit" class="pm-btn pm-btn-primary" data-pm-agree-button><%= next.isEmpty() ? "Give my consent for voting" : "Agree and go to voting" %></button>
                    <a class="pm-btn" href="/policy/privacy" target="_blank" rel="noopener">View Privacy Policy</a>
                </div>
            </form>
            <% } %>
        </div>

        <aside class="pm-stack">
            <section class="pm-panel">
                <div class="pm-panel-head"><h2>What is saved</h2></div>
                <p>When you agree, one record per document is added to <code>consent_records</code> with your user id,
                    the consent type <strong>VOTING</strong>, the date and time, and the version number.</p>
                <p class="pm-m0">You can see these records, or withdraw your consent, on the <a href="/privacy/my">My privacy &amp; consent</a> page.</p>
            </section>
        </aside>
    </div>
</div>
</main>

<%@ include file="_pmFoot.jspf" %>
</body>
</html>
