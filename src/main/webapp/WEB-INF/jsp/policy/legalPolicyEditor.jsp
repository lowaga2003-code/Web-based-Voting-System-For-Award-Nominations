<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.pation.awardnomination.policy.*" %>
<%@ page import="java.util.*" %>
<%--
  MODULE 6 - rich-text editor for a policy draft (Legal & Governance Officer only).
  "Save draft"  -> UPDATE policy_documents (the draft row)
  "Publish"     -> the draft gets the next version number, the old version is archived.
                   Minor update: users are informed. Major update: every user must accept it again.
  The editor (policy-editor.js) writes the formatted text into the hidden "content" field; the server
  cleans it with PolicyHtml.sanitize() before saving. Without JavaScript the HTML can be edited directly.
  Model: doc (the draft), current (live version or null), firstVersion, nextMinor, nextMajor
--%>
<%
    PolicyDocument doc = (PolicyDocument) request.getAttribute("doc");
    PolicyDocument current = (PolicyDocument) request.getAttribute("current");
    boolean firstVersion = Boolean.TRUE.equals(request.getAttribute("firstVersion"));
    String nextMinor = (String) request.getAttribute("nextMinor");
    String nextMajor = (String) request.getAttribute("nextMajor");

    String pmTitle = "Edit " + doc.getPolicyType().getDisplayName();
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
            <h1>Edit the <%= h(doc.getPolicyType().getDisplayName()) %></h1>
            <% if (current != null) { %>
            <p class="pm-lead">You are editing a draft. Users keep seeing version <%= h(current.getVersionLabel()) %>
                (published <%= h(PolicyHtml.date(current.getPublishedAt())) %>) until you publish it.</p>
            <% } else { %>
            <p class="pm-lead">You are writing the first version. Users will be asked to agree to it once you publish it.</p>
            <% } %>
        </div>
        <div class="pm-head-actions">
            <a class="pm-btn" href="/legal/policies">Back to policies</a>
        </div>
    </div>

    <form method="post" action="/legal/policies/<%= doc.getId() %>" id="policyForm" data-pm-editor-form>
        <div class="pm-grid">
            <div class="pm-stack">
                <section class="pm-panel" aria-label="Document text">
                    <div class="pm-field">
                        <label class="pm-label" for="title">Title</label>
                        <input class="pm-input pm-title-input" id="title" name="title" maxlength="200" required value="<%= h(doc.getTitle()) %>">
                    </div>
                    <div class="pm-field pm-m0" data-pm-editor>
                        <label class="pm-label" for="content">Text</label>
                        <div class="pm-editor" hidden data-pm-editor-box>
                            <div class="pm-toolbar" role="toolbar" aria-label="Formatting" aria-controls="contentEditor">
                                <button type="button" data-cmd="h2" title="Section heading">Heading</button>
                                <button type="button" data-cmd="h3" title="Sub-heading">Sub-heading</button>
                                <button type="button" data-cmd="p" title="Normal paragraph">Paragraph</button>
                                <span class="pm-tool-sep" aria-hidden="true"></span>
                                <button type="button" data-cmd="bold" aria-pressed="false" title="Bold (Ctrl+B)"><b>B</b></button>
                                <button type="button" data-cmd="italic" aria-pressed="false" title="Italic (Ctrl+I)"><i>I</i></button>
                                <button type="button" data-cmd="underline" aria-pressed="false" title="Underline (Ctrl+U)"><u>U</u></button>
                                <span class="pm-tool-sep" aria-hidden="true"></span>
                                <button type="button" data-cmd="ul" title="Bulleted list">Bullets</button>
                                <button type="button" data-cmd="ol" title="Numbered list">Numbers</button>
                                <button type="button" data-cmd="quote" title="Quote">Quote</button>
                                <span class="pm-tool-sep" aria-hidden="true"></span>
                                <button type="button" data-cmd="link" title="Add a link">Link</button>
                                <button type="button" data-cmd="unlink" title="Remove a link">Unlink</button>
                                <button type="button" data-cmd="clear" title="Remove formatting">Clear formatting</button>
                            </div>
                            <div class="pm-editable pm-doc" id="contentEditor" contenteditable="true" role="textbox"
                                 aria-multiline="true" aria-label="Policy text" spellcheck="true" data-pm-editable></div>
                        </div>
                        <textarea class="pm-textarea pm-editor-fallback" id="content" name="content" required
                                  data-pm-editor-source><%= h(doc.getContent()) %></textarea>
                        <p class="pm-help">Headings, bold, italic, underline, lists, quotes and links are kept. Anything else is removed when you save.</p>
                    </div>
                </section>
            </div>

            <aside class="pm-stack">
                <section class="pm-panel" aria-labelledby="publishTitle">
                    <div class="pm-panel-head"><h2 id="publishTitle">Publish</h2></div>
                    <div class="pm-field">
                        <label class="pm-label" for="changeSummary">What changed<%= firstVersion ? " <span class=\"pm-muted\">(optional)</span>" : "" %></label>
                        <textarea class="pm-textarea" id="changeSummary" name="changeSummary" maxlength="500" rows="3"><%= h(doc.getChangeSummary()) %></textarea>
                        <p class="pm-help">Users read this when they are asked to accept the new version.</p>
                    </div>
                    <% if (firstVersion) { %>
                    <input type="hidden" name="change" value="major">
                    <p class="pm-help">This will be version 1.0. Every user must agree to it before they can continue.</p>
                    <% } else { %>
                    <fieldset class="pm-fieldset">
                        <legend class="pm-label">Type of change</legend>
                        <div class="pm-choice-list">
                            <label class="pm-choice">
                                <input type="radio" name="change" value="minor" checked data-pm-version="<%= h(nextMinor) %>">
                                <span><strong>Minor update, version <%= h(nextMinor) %></strong>
                                    <span>Wording fixes and small clarifications. Users are told about it but don't have to agree again.</span></span>
                            </label>
                            <label class="pm-choice">
                                <input type="radio" name="change" value="major" data-pm-version="<%= h(nextMajor) %>">
                                <span><strong>Major update, version <%= h(nextMajor) %></strong>
                                    <span>New uses of data or new rules. Every user must agree to this version before they can continue.</span></span>
                            </label>
                        </div>
                    </fieldset>
                    <% } %>
                    <div class="pm-actions">
                        <button type="submit" class="pm-btn" name="action" value="save">Save draft</button>
                        <button type="submit" class="pm-btn pm-btn-primary" name="action" value="publish" data-pm-publish
                                <%= firstVersion ? "" : "data-pm-needs-note=\"changeSummary\" data-pm-needs-note-message=\"Describe what changed before you publish. Users read this when they are asked to accept the new version.\"" %>
                                data-pm-confirm="Publish this version now? Published versions can't be changed afterwards."><span>Publish version <span data-pm-publish-version><%= h(firstVersion ? "1.0" : nextMinor) %></span></span></button>
                    </div>
                </section>

                <section class="pm-panel" aria-labelledby="draftTitle">
                    <div class="pm-panel-head"><h2 id="draftTitle">This draft</h2></div>
                    <dl class="pm-facts">
                        <div><dt>Started</dt><dd><%= h(PolicyHtml.when(doc.getCreatedAt())) %> by <%= h(doc.getCreatedBy()) %></dd></div>
                        <div><dt>Last saved</dt><dd><%= doc.getUpdatedAt() == null ? "Not saved yet" : h(PolicyHtml.when(doc.getUpdatedAt())) + " by " + h(doc.getUpdatedBy()) %></dd></div>
                        <div><dt>Stored as</dt><dd><code><%= h(doc.getCode()) %></code> in <code>policy_documents</code></dd></div>
                    </dl>
                    <div class="pm-actions pm-after">
                        <a class="pm-btn pm-btn-small" href="/policy/view/<%= doc.getId() %>" target="_blank" rel="noopener">Preview saved draft</a>
                        <button type="submit" form="deleteDraftForm" class="pm-btn pm-btn-small pm-btn-danger"
                                data-pm-confirm="Delete this draft? Your changes will be lost. The published version stays as it is.">Delete draft</button>
                    </div>
                </section>
            </aside>
        </div>
    </form>

    <form method="post" action="/legal/policies/<%= doc.getId() %>/delete" id="deleteDraftForm" hidden></form>
</main>

<%@ include file="_pmFoot.jspf" %>
<script src="/js/policy-editor.js"></script>
</body>
</html>
