<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.pation.awardnomination.policy.*" %>
<%@ page import="java.util.*" %>
<%--
  MODULE 6 - "My privacy & consent" (the privacy part of every user's profile).
   - What the user agrees to now, and the voting consent
   - My Consent History  (SELECT ... FROM consent_records WHERE user_id = ?)
   - Request Data Deletion / withdraw consent (INSERT INTO data_deletion_requests), cancel a pending request (DELETE)
  Model: history, currentDocs, currentConsents, mustAccept, optionalUpdates, votingMissing, votingConsents,
         requests, pendingTypes
--%>
<%
    List<ConsentRecord> history = (List<ConsentRecord>) request.getAttribute("history");
    Map<PolicyType, PolicyDocument> currentDocs = (Map<PolicyType, PolicyDocument>) request.getAttribute("currentDocs");
    Map<PolicyType, ConsentRecord> currentConsents = (Map<PolicyType, ConsentRecord>) request.getAttribute("currentConsents");
    List<PolicyDocument> mustAccept = (List<PolicyDocument>) request.getAttribute("mustAccept");
    List<PolicyDocument> optionalUpdates = (List<PolicyDocument>) request.getAttribute("optionalUpdates");
    List<PolicyDocument> votingMissing = (List<PolicyDocument>) request.getAttribute("votingMissing");
    Map<PolicyType, ConsentRecord> votingConsents = (Map<PolicyType, ConsentRecord>) request.getAttribute("votingConsents");
    List<DeletionRequest> requests = (List<DeletionRequest>) request.getAttribute("requests");
    Set<DeletionRequestType> pendingTypes = (Set<DeletionRequestType>) request.getAttribute("pendingTypes");
    if (history == null) { history = Collections.emptyList(); }
    if (currentDocs == null) { currentDocs = Collections.emptyMap(); }
    if (currentConsents == null) { currentConsents = Collections.emptyMap(); }
    if (mustAccept == null) { mustAccept = Collections.emptyList(); }
    if (optionalUpdates == null) { optionalUpdates = Collections.emptyList(); }
    if (votingMissing == null) { votingMissing = Collections.emptyList(); }
    if (votingConsents == null) { votingConsents = Collections.emptyMap(); }
    if (requests == null) { requests = Collections.emptyList(); }
    if (pendingTypes == null) { pendingTypes = Collections.emptySet(); }

    Set<PolicyType> mustTypes = EnumSet.noneOf(PolicyType.class);
    for (PolicyDocument d : mustAccept) {
        mustTypes.add(d.getPolicyType());
    }
    java.time.LocalDateTime votingGiven = null;
    for (ConsentRecord r : votingConsents.values()) {
        if (votingGiven == null || (r.getAgreedAt() != null && r.getAgreedAt().isAfter(votingGiven))) {
            votingGiven = r.getAgreedAt();
        }
    }
    boolean allPending = pendingTypes.size() == DeletionRequestType.values().length;

    String pmTitle = "My privacy & consent";
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

    <div class="pm-head">
        <div>
            <h1>My privacy &amp; consent</h1>
            <p class="pm-lead">What you agreed to and when, and your requests to withdraw consent or delete your data.</p>
        </div>
        <div class="pm-head-actions">
            <a class="pm-btn" href="#request-form">Request data deletion</a>
        </div>
    </div>

    <% if (!mustAccept.isEmpty()) { %>
    <div class="pm-notice is-warn pm-notice-row">
        <p>You need to agree to <%= pmDocumentNames(mustAccept) %> before you can use the rest of the system.</p>
        <a class="pm-btn pm-btn-primary pm-btn-small" href="/privacy/consent?next=/privacy/my">Review and agree</a>
    </div>
    <% } else if (!optionalUpdates.isEmpty()) { %>
    <div class="pm-notice is-info pm-notice-row">
        <p>There is a newer version of <%= pmDocumentNames(optionalUpdates) %>. You don't have to agree again.</p>
        <a class="pm-btn pm-btn-small" href="/privacy/consent?next=/privacy/my">Read what changed</a>
    </div>
    <% } %>

    <div class="pm-grid">
        <div class="pm-stack">
            <section class="pm-panel" aria-labelledby="nowTitle">
                <div class="pm-panel-head">
                    <h2 id="nowTitle">What you agree to now</h2>
                </div>
                <div class="pm-table-wrap">
                    <table class="pm-table">
                        <thead>
                        <tr><th>Document</th><th>Your version</th><th>Agreed on</th><th>Where</th><th>Status</th></tr>
                        </thead>
                        <tbody>
                        <% for (PolicyType type : PolicyType.values()) {
                               PolicyDocument live = currentDocs.get(type);
                               ConsentRecord c = currentConsents.get(type); %>
                        <tr>
                            <td><a href="/policy/<%= h(type.getSlug()) %>"><%= h(type.getDisplayName()) %></a></td>
                            <% if (c == null) { %>
                            <td class="pm-muted">None</td><td></td><td></td>
                            <% } else { %>
                            <td><a class="pm-version" href="/policy/view/<%= c.getPolicyId() %>"><%= h(c.getPolicyVersion()) %></a></td>
                            <td class="pm-nowrap"><%= h(PolicyHtml.dateTime(c.getAgreedAt())) %></td>
                            <td class="pm-nowrap"><%= h(c.getConsentType().getDisplayName()) %></td>
                            <% } %>
                            <td>
                                <% if (live == null) { %>
                                <span class="pm-muted">Not published yet</span>
                                <% } else if (mustTypes.contains(type)) { %>
                                <span class="pm-badge is-pending"><%= c == null ? "Not agreed" : "Version " + h(live.getVersionLabel()) + " to accept" %></span>
                                <% } else if (c != null && c.getPolicyId() != null && c.getPolicyId().equals(live.getId())) { %>
                                <span class="pm-badge is-current">Up to date</span>
                                <% } else { %>
                                <span class="pm-badge is-archived">Version <%= h(live.getVersionLabel()) %> available</span>
                                <% } %>
                            </td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
                <div class="pm-notice <%= votingMissing.isEmpty() ? "is-info" : "" %> pm-notice-row pm-after">
                    <% if (votingMissing.isEmpty() && votingGiven == null) { %>
                    <p><strong>Consent for voting:</strong> there is no published document to agree to yet.</p>
                    <% } else if (votingMissing.isEmpty()) { %>
                    <p><strong>Consent for voting:</strong> given on <%= h(PolicyHtml.when(votingGiven)) %>.</p>
                    <% } else { %>
                    <p><strong>Consent for voting:</strong> not given for the current documents yet. You will be asked for it before you vote.</p>
                    <a class="pm-btn pm-btn-small" href="/privacy/voting">Give it now</a>
                    <% } %>
                </div>
            </section>

            <section class="pm-panel" aria-labelledby="historyTitle">
                <div class="pm-panel-head">
                    <h2 id="historyTitle">My consent history</h2>
                    <span class="pm-source"><%= history.size() %> <%= history.size() == 1 ? "record" : "records" %> in <code>consent_records</code></span>
                </div>
                <% if (history.isEmpty()) { %>
                <div class="pm-empty"><p>You haven't agreed to any document yet.</p></div>
                <% } else { %>
                <div class="pm-table-wrap">
                    <table class="pm-table">
                        <thead>
                        <tr><th>Record</th><th>Document</th><th>Version</th><th>Where</th><th>Agreed on</th><th>Status</th></tr>
                        </thead>
                        <tbody>
                        <% for (ConsentRecord r : history) { %>
                        <tr>
                            <td class="pm-code"><%= h(r.getCode()) %></td>
                            <td><%= h(r.getPolicyType().getDisplayName()) %></td>
                            <td><a class="pm-version" href="/policy/view/<%= r.getPolicyId() %>" title="Read version <%= h(r.getPolicyVersion()) %>"><%= h(r.getPolicyVersion()) %></a></td>
                            <td class="pm-nowrap"><%= h(r.getConsentType().getDisplayName()) %></td>
                            <td class="pm-nowrap"><%= h(PolicyHtml.dateTime(r.getAgreedAt())) %></td>
                            <td>
                                <% if (r.isActive()) { %>
                                <span class="pm-badge is-active">Active</span>
                                <% } else { %>
                                <span class="pm-badge is-withdrawn">Withdrawn</span>
                                <div class="pm-small pm-muted pm-mt-xs pm-nowrap"><%= h(PolicyHtml.dateTime(r.getWithdrawnAt())) %></div>
                                <% } %>
                            </td>
                        </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
                <% } %>
            </section>

            <section class="pm-panel" id="requests" aria-labelledby="requestsTitle">
                <div class="pm-panel-head">
                    <h2 id="requestsTitle">My requests</h2>
                    <span class="pm-source"><%= requests.size() %> in <code>data_deletion_requests</code></span>
                </div>
                <% if (requests.isEmpty()) { %>
                <div class="pm-empty"><p>You haven't sent any request.</p><p>Use the form to withdraw your consent or ask for your data to be deleted.</p></div>
                <% } else { %>
                <% for (DeletionRequest r : requests) {
                       String badge = r.getStatus() == RequestStatus.PENDING ? "is-pending" : (r.getStatus() == RequestStatus.APPROVED ? "is-approved" : "is-rejected"); %>
                <article class="pm-request">
                    <div class="pm-request-head">
                        <div>
                            <h3><%= h(r.getRequestType().getDisplayName()) %></h3>
                            <p class="pm-small pm-muted pm-mt-xs"><span class="pm-code"><%= h(r.getCode()) %></span>, sent <%= h(PolicyHtml.when(r.getRequestedAt())) %></p>
                        </div>
                        <span class="pm-badge <%= badge %>"><%= h(r.getStatus().getLabel()) %></span>
                    </div>
                    <% if (r.getReason() != null) { %>
                    <p class="pm-quote"><%= h(r.getReason()) %></p>
                    <% } %>
                    <% if (r.isPending()) { %>
                    <form method="post" action="/privacy/requests/<%= r.getId() %>/cancel" class="pm-actions">
                        <button type="submit" class="pm-btn pm-btn-small pm-btn-danger" data-pm-confirm="Cancel request <%= h(r.getCode()) %>? It will be deleted.">Cancel request</button>
                        <span class="pm-small pm-muted">The Legal &amp; Governance Officer hasn't decided yet.</span>
                    </form>
                    <% } else { %>
                    <dl class="pm-facts">
                        <div><dt>Decided by</dt><dd><%= h(r.getReviewedBy()) %></dd></div>
                        <div><dt>Decided on</dt><dd><%= h(PolicyHtml.when(r.getReviewedAt())) %></dd></div>
                        <% if (r.getReviewNote() != null) { %><div><dt>Note</dt><dd><%= h(r.getReviewNote()) %></dd></div><% } %>
                        <% if (r.getOutcome() != null) { %><div><dt>What happened</dt><dd><%= h(r.getOutcome()) %></dd></div><% } %>
                    </dl>
                    <% } %>
                </article>
                <% } %>
                <% } %>
            </section>
        </div>

        <aside class="pm-stack">
            <section class="pm-panel" id="request-form" aria-labelledby="formTitle">
                <div class="pm-panel-head">
                    <h2 id="formTitle">Withdraw consent or delete my data</h2>
                </div>
                <% if (allPending) { %>
                <div class="pm-empty"><p>Both of your requests are waiting for a decision.</p></div>
                <% } else { %>
                <form method="post" action="/privacy/requests" data-pm-request-form>
                    <fieldset class="pm-fieldset">
                        <legend class="pm-label">What would you like us to do?</legend>
                        <div class="pm-choice-list">
                            <% boolean firstFree = true;
                               for (DeletionRequestType t : DeletionRequestType.values()) {
                                   boolean pending = pendingTypes.contains(t);
                                   boolean checked = !pending && firstFree;
                                   if (checked) { firstFree = false; } %>
                            <label class="pm-choice<%= pending ? " is-disabled" : "" %>">
                                <input type="radio" name="requestType" value="<%= t.name() %>"<%= pending ? " disabled" : "" %><%= checked ? " checked" : "" %>>
                                <span>
                                    <strong><%= h(t.getDisplayName()) %></strong>
                                    <span><%= h(pending ? "You already sent this request. It is waiting for a decision." : t.getDescription()) %></span>
                                </span>
                            </label>
                            <% } %>
                        </div>
                    </fieldset>
                    <div class="pm-field">
                        <label class="pm-label" for="reason">Reason <span class="pm-muted">(optional)</span></label>
                        <textarea class="pm-textarea" id="reason" name="reason" maxlength="1000" rows="3"></textarea>
                    </div>
                    <label class="pm-check" data-pm-delete-confirm>
                        <input type="checkbox" name="confirm" value="yes">
                        <span>I understand that my account will be closed if this request is approved.</span>
                    </label>
                    <div class="pm-actions pm-after">
                        <button type="submit" class="pm-btn pm-btn-primary">Send request</button>
                    </div>
                    <p class="pm-help pm-after">The Legal &amp; Governance Officer reviews every request. You can follow it under My requests.</p>
                </form>
                <% } %>
            </section>
        </aside>
    </div>
</main>

<%@ include file="_pmFoot.jspf" %>
</body>
</html>
