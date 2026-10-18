<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="com.pation.awardnomination.category.*" %>
<%
    CategoryForm f = (CategoryForm) request.getAttribute("form");
    boolean editing = Boolean.TRUE.equals(request.getAttribute("editing"));
    AwardCategory c = (AwardCategory) request.getAttribute("category");
    CategoryPhase ph = (CategoryPhase) request.getAttribute("phase");
    Object votesObj = request.getAttribute("votes");
    long votes = votesObj == null ? 0L : (Long) votesObj;
    String cmTitle = editing ? "Edit " + c.getName() : "New award category";
    String cmTab = editing ? "detail" : "new";
    boolean cmPublic = false;
    String action = editing ? "/manager/awards/" + c.getId() + "/edit" : "/manager/awards";
    boolean nameLocked = editing && ph != CategoryPhase.SCHEDULED && ph != CategoryPhase.NOMINATIONS_OPEN;
    boolean frozen = editing && votes > 0 && ph == CategoryPhase.RESULTS;
    boolean onlyVotingEnd = editing && votes > 0;
%>
<%@ include file="_cmTop.jspf" %>

<div class="cm-head">
  <div>
    <div class="cm-kicker">
      <a href="<%= cx %>/manager/awards">Control board</a> <span>/</span>
      <% if (editing) { %><a href="<%= cx %>/manager/awards/<%= c.getId() %>"><%= CategoryHtml.h(c.getCode()) %></a> <span>/</span> <span>Edit</span><% } else { %><span>New category</span><% } %>
    </div>
    <h1><%= editing ? "Edit timeline &amp; details" : "Create an award category" %></h1>
    <p><%= editing ? "Changes to dates are written to the transparency log with the old and new values." : "Set the category, its eligibility rules and the two windows that drive the whole season." %></p>
  </div>
  <% if (editing) { %><div class="cm-head-actions"><span class="cm-phase cm-phase-lg <%= ph.getCss() %>"><%= CategoryHtml.h(ph.getLabel()) %></span></div><% } %>
</div>

<form method="post" action="<%= cx %><%= action %>" data-cm-timeline-form>
<div class="cm-grid cm-grid-2">
  <div>
    <section class="cm-card">
      <div class="cm-card-h"><h2>Category</h2></div>
      <div class="cm-form-grid">
        <div class="cm-field full">
          <label for="name">Name</label>
          <input class="cm-input" id="name" name="name" maxlength="120" required value="<%= CategoryHtml.h(f.getName()) %>" <%= nameLocked ? "readonly" : "" %> placeholder="e.g. Innovator of the Year">
          <% if (nameLocked) { %><small>The name is fixed after nominations close, because other modules refer to the category by name.</small><% } %>
        </div>
        <div class="cm-field">
          <label for="code">Code</label>
          <% if (editing) { %>
            <input class="cm-input cm-mono" id="code" value="<%= CategoryHtml.h(f.getCode()) %>" readonly>
            <small>Permanent. Used as the ledger ticket code CAT-<%= CategoryHtml.h(f.getCode()) %>.</small>
          <% } else { %>
            <input class="cm-input cm-mono" id="code" name="code" maxlength="16" value="<%= CategoryHtml.h(f.getCode()) %>" placeholder="auto from name" style="text-transform:uppercase">
            <small>Optional. 2-16 letters, digits or dashes. Leave empty to generate it.</small>
          <% } %>
        </div>
        <div class="cm-field">
          <label for="ballotLimit">Ballot limit</label>
          <input class="cm-input" type="number" min="2" max="50" id="ballotLimit" name="ballotLimit" value="<%= CategoryHtml.h(f.getBallotLimit()) %>" placeholder="no limit">
          <small>Maximum nominees on the ballot (2-50).</small>
        </div>
        <div class="cm-field full">
          <label for="description">Description</label>
          <textarea class="cm-textarea" id="description" name="description" maxlength="1000" placeholder="What this award recognises. Voters see this on their ballot."><%= CategoryHtml.h(f.getDescription()) %></textarea>
        </div>
      </div>
    </section>

    <% if (!editing) { %>
    <section class="cm-card">
      <div class="cm-card-h"><h2>Eligibility rules</h2></div>
      <p class="cm-sub">Nominees are checked against these before they can be placed on the ballot. More rules can be added later.</p>
      <div class="cm-checks-form">
        <label class="cm-tick"><input type="checkbox" name="ruleSubmitted" value="true" <%= f.isRuleSubmitted() ? "checked" : "" %>> Nomination submitted (not Draft)</label>
        <label class="cm-tick"><input type="checkbox" name="ruleApproved" value="true" <%= f.isRuleApproved() ? "checked" : "" %>> Approved by the administrator</label>
        <label class="cm-tick"><input type="checkbox" name="ruleEvidence" value="true" <%= f.isRuleEvidence() ? "checked" : "" %>> Evidence document uploaded</label>
        <label class="cm-tick"><input type="checkbox" name="ruleOrganisation" value="true" <%= f.isRuleOrganisation() ? "checked" : "" %>> Organisation / company named</label>
      </div>
      <div class="cm-form-grid" style="margin-top:14px">
        <div class="cm-field">
          <label for="minBioWords">Minimum biography words</label>
          <input class="cm-input" type="number" min="1" max="2000" id="minBioWords" name="minBioWords" value="<%= CategoryHtml.h(f.getMinBioWords()) %>" placeholder="optional">
        </div>
        <div class="cm-field full">
          <label for="customRules">Custom rules (one per line, checked by you)</label>
          <textarea class="cm-textarea" id="customRules" name="customRules" placeholder="e.g. Nominee is 35 or younger on the closing date"><%= CategoryHtml.h(f.getCustomRules()) %></textarea>
        </div>
      </div>
    </section>
    <% } %>
  </div>

  <div>
    <section class="cm-card">
      <div class="cm-card-h"><h2>Timeline</h2></div>
      <% if (frozen) { %>
        <p class="cm-sub" style="color:#b45309">Voting has closed with <%= votes %> sealed ballot(s), so the timeline is frozen. Details can still be edited.</p>
      <% } else if (onlyVotingEnd) { %>
        <p class="cm-sub" style="color:#b45309"><%= votes %> ballot(s) are sealed, so only the voting end can still change.</p>
      <% } %>
      <fieldset class="cm-window n">
        <legend>Nomination window</legend>
        <div class="cm-form-grid">
          <div class="cm-field"><label for="ns">Opens</label><input class="cm-input cm-mono" type="datetime-local" id="ns" name="nominationStart" required value="<%= CategoryHtml.h(f.getNominationStart()) %>" data-cm-t="ns" <%= onlyVotingEnd ? "readonly" : "" %>></div>
          <div class="cm-field"><label for="ne">Closes</label><input class="cm-input cm-mono" type="datetime-local" id="ne" name="nominationEnd" required value="<%= CategoryHtml.h(f.getNominationEnd()) %>" data-cm-t="ne" <%= onlyVotingEnd ? "readonly" : "" %>></div>
        </div>
      </fieldset>
      <fieldset class="cm-window v" style="margin-top:14px">
        <legend>Voting window</legend>
        <div class="cm-form-grid">
          <div class="cm-field"><label for="vs">Opens</label><input class="cm-input cm-mono" type="datetime-local" id="vs" name="votingStart" required value="<%= CategoryHtml.h(f.getVotingStart()) %>" data-cm-t="vs" <%= onlyVotingEnd ? "readonly" : "" %>></div>
          <div class="cm-field"><label for="ve">Closes</label><input class="cm-input cm-mono" type="datetime-local" id="ve" name="votingEnd" required value="<%= CategoryHtml.h(f.getVotingEnd()) %>" data-cm-t="ve" <%= frozen ? "readonly" : "" %>></div>
        </div>
      </fieldset>
      <div class="cm-preview" aria-live="polite">
        <div class="cm-preview-track" data-cm-preview><i class="n"></i><i class="v"></i></div>
        <div class="cm-preview-msg" data-cm-preview-msg>Nominations, then voting. Voting must start after nominations close.</div>
      </div>
      <div class="cm-help"><b>What the dates do:</b> while nominations are open, nominees can submit and edit (Module 3). When they close, Draft profiles are locked and moved to Under Review. Voters can vote only inside the voting window, once per category.</div>
    </section>
    <div class="cm-form-actions">
      <a class="cm-btn" href="<%= cx %><%= editing ? "/manager/awards/" + c.getId() : "/manager/awards" %>">Cancel</a>
      <button class="cm-btn cm-btn-primary" type="submit"><%= editing ? "Save changes" : "Create category" %></button>
    </div>
  </div>
</div>
</form>

<%@ include file="_cmBottom.jspf" %>
