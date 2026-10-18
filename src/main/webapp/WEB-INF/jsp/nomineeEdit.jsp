<%@ page import="com.pation.awardnomination.nominee.Nominee" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    // Makes database text safe inside HTML: & < > " become HTML codes,
    // so a name like Silva "Jr" or <b> shows exactly as typed and cannot break the page.
    String esc(String text) {
        if (text == null) { return ""; }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Nomination | Award Nomination System</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@600;700&family=Manrope:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        /* ===== 1. Page ===== */
        * { box-sizing: border-box; }
        body { margin: 0; background: #f4f2f3; color: #241418; font-family: 'Manrope', 'Segoe UI', sans-serif; line-height: 1.55; }

        /* ===== 2. Top band ===== */
        .hero {
            margin-top: -28px; padding: 96px 20px 104px; color: #f3e6ea;
            background-color: #5b1630;
            background-image:
                repeating-radial-gradient(circle at 12% 135%, rgba(243, 216, 145, 0.12) 0 1px, transparent 1px 11px),
                repeating-radial-gradient(circle at 92% -45%, rgba(243, 216, 145, 0.09) 0 1px, transparent 1px 13px);
        }
        .hero-inner { max-width: 680px; margin: 0 auto; }
        .hero .back { color: #f3d891; font-weight: 700; font-size: 0.92rem; text-decoration: none; }
        .hero .back:hover { text-decoration: underline; }
        .hero h1 { margin: 14px 0 10px; font-family: 'Cormorant Garamond', Georgia, serif; font-size: clamp(2.4rem, 5.5vw, 3.4rem); line-height: 1; color: #ffffff; }
        .hero p { margin: 0; color: #ead7dc; }

        /* ===== 3. Card ===== */
        .page { position: relative; max-width: 720px; margin: -60px auto 110px; padding: 0 20px; }
        .card { padding: 32px; background: #ffffff; border: 1px solid #e6dde0; border-radius: 18px; box-shadow: 0 18px 40px rgba(60, 20, 35, 0.10); }

        /* ===== 4. Fields + validation messages ===== */
        .field { margin-bottom: 20px; }
        .field label { display: block; margin-bottom: 6px; font-weight: 700; }
        .field input, .field textarea, .field select { width: 100%; padding: 12px 14px; border: 1.5px solid #d8cdd1; border-radius: 10px; font: inherit; color: #241418; background: #ffffff; }
        .field textarea { min-height: 130px; resize: vertical; }
        .field input:focus, .field textarea:focus, .field select:focus { outline: none; border-color: #7a1d3b; box-shadow: 0 0 0 4px rgba(122, 29, 59, 0.14); }
        .field .help { margin: 6px 0 0; font-size: 0.85rem; color: #6c5a60; }
        .field .error { display: none; margin: 6px 0 0; font-size: 0.85rem; font-weight: 700; color: #b42318; }
        .field.has-error input, .field.has-error textarea { border-color: #b42318; }
        .field.has-error .error { display: block; }

        /* status: coloured dot + dropdown (admin) or info box (nominee) */
        .field .status-label { display: flex; align-items: center; gap: 8px; margin: 0 0 6px; font-weight: 700; }
        .status-dot { flex: none; width: 12px; height: 12px; border-radius: 50%; }
        .status-draft { background: #64748b; }
        .status-review { background: #d97706; }
        .status-approved { background: #16a34a; }
        .status-info { padding: 12px 16px; border-radius: 12px; background: #fff7e6; border: 1px solid #f1d9a6; color: #6b4a07; }

        /* ===== 5. Buttons (colour codes are written directly) ===== */
        .buttons { display: flex; gap: 12px; flex-wrap: wrap; margin-top: 8px; }
        .btn { display: inline-flex; align-items: center; justify-content: center; padding: 14px 22px; border-radius: 12px; font: 700 1rem 'Manrope', 'Segoe UI', sans-serif; text-decoration: none; cursor: pointer; }
        .btn:focus-visible { outline: 3px solid #241418; outline-offset: 3px; }
        .btn-save { flex: 1; border: 0; background: #7a1d3b; color: #ffffff; }
        .btn-save:hover { background: #611630; }
        .btn-save:disabled { background: #b98a99; cursor: wait; }
        .btn-cancel { border: 1.5px solid #7a1d3b; background: #ffffff; color: #7a1d3b; }
        .btn-cancel:hover { background: #f8eef1; }
        .btn-interview { margin-top: 20px; width: 100%; border: 0; background: #c08a1e; color: #241418; }
        .btn-interview:hover { background: #a87712; }

        @media (max-width: 560px) { .card { padding: 24px 20px; } .btn-save, .btn-cancel { flex: 1 1 100%; } }   /* phones: one button per row */
    </style>
</head>
<body>
<%-- [INTEGRATION] system navigation bar (Dashboard / Logout) --%>
<%@ include file="/WEB-INF/jsp/common/portalBar.jspf" %>

<%
    Nominee nominee = (Nominee) request.getAttribute("nominee");
    String role = (String) session.getAttribute("role");
    boolean isAdmin = "ADMIN".equals(role);

    // back link: admin -> nominee list, nominee -> own profile
    String backUrl = isAdmin ? "/nominee/list" : "/nominee/profile";
    String backText = isAdmin ? "Back to the nominee list" : "Back to my nomination";

    String status = (nominee != null && nominee.getStatus() != null) ? nominee.getStatus() : "Draft";
    String dotClass = "status-review";
    if ("Draft".equals(status)) { dotClass = "status-draft"; }
    else if ("Approved".equals(status)) { dotClass = "status-approved"; }
%>

<header class="hero">
    <div class="hero-inner">
        <a class="back" href="<%= backUrl %>"><%= backText %></a>
        <h1><%= isAdmin ? "Edit nominee" : "Edit my nomination" %></h1>
        <p>Update the details below and save your changes.</p>
    </div>
</header>

<main class="page">
    <% if (nominee == null) { %>
    <section class="card">
        <p>This nominee could not be found. It may have been deleted.</p>
        <a class="btn btn-cancel" href="<%= backUrl %>"><%= backText %></a>
    </section>
    <% } else { %>

    <%-- POST /nominee/update/{id} -> NomineeController.updateNominee() --%>
    <form class="card" action="/nominee/update/<%= nominee.getId() %>" method="post" onsubmit="return validateEdit()" novalidate>
        <div class="field">
            <label for="fullName">Full name</label>
            <input type="text" id="fullName" name="fullName" value="<%= esc(nominee.getFullName()) %>" required oninput="clearError('fullName')">
            <p class="error" aria-live="polite"></p>
        </div>
        <div class="field">
            <label for="companyName">Company or organisation</label>
            <input type="text" id="companyName" name="companyName" value="<%= esc(nominee.getCompanyName()) %>" required oninput="clearError('companyName')">
            <p class="error" aria-live="polite"></p>
        </div>
        <div class="field">
            <label for="biography">Biography</label>
            <textarea id="biography" name="biography" required oninput="clearError('biography')"><%= esc(nominee.getBiography()) %></textarea>
            <p class="error" aria-live="polite"></p>
        </div>

        <div class="field">
            <% if (isAdmin) { %>
            <%-- Admin ට විතරක් status එක වෙනස් කරන්න dropdown එක --%>
            <label class="status-label" for="status"><span class="status-dot <%= dotClass %>" id="statusDot"></span>Status</label>
            <select name="status" id="status" onchange="syncDot(this.value)">
                <option value="Draft" <%= "Draft".equals(status) ? "selected" : "" %>>Draft</option>
                <option value="Under Review" <%= "Under Review".equals(status) ? "selected" : "" %>>Under Review</option>
                <option value="Approved" <%= "Approved".equals(status) ? "selected" : "" %>>Approved</option>
            </select>
            <p class="help">Choose Approved when the details and evidence are checked.</p>
            <% } else { %>
            <%-- Nominee ට status එක පේනවා විතරයි. Save කළාම Under Review වෙනවා --%>
            <p class="status-label"><span class="status-dot <%= dotClass %>"></span>Status</p>
            <div class="status-info">Your status is <strong><%= status %></strong>.
                <%= "Under Review".equals(status) ? "The committee will check your changes." : "Saving changes sets it to <strong>Under Review</strong> so the committee can check them." %></div>
            <% } %>
        </div>

        <div class="buttons">
            <button type="submit" class="btn btn-save" id="saveBtn">Save changes</button>
            <a class="btn btn-cancel" href="<%= backUrl %>">Cancel</a>
        </div>
    </form>

    <a class="btn btn-interview" href="/nominee/interviews"><%= isAdmin ? "Manage interview slots" : "Book an interview" %></a>
    <% } %>
</main>

<%-- AI assistant for nominees (admins do not need it on this page) --%>
<% if (!isAdmin) { %>
<%@ include file="nomineeChatbot.jspf" %>
<% } %>

<script>
    // ===== Validation =====
    function showError(id, message) {
        var box = document.getElementById(id).closest('.field');
        box.classList.add('has-error');
        box.querySelector('.error').textContent = message;
    }

    function clearError(id) {
        document.getElementById(id).closest('.field').classList.remove('has-error');
    }

    function validateEdit() {
        var ok = true;
        var name = document.getElementById('fullName').value.trim();
        if (!/^[A-Za-z ]{3,}$/.test(name)) { showError('fullName', 'Use at least 3 letters, and letters and spaces only.'); ok = false; }
        if (document.getElementById('companyName').value.trim() === '') { showError('companyName', 'Enter the company or organisation name.'); ok = false; }
        if (document.getElementById('biography').value.trim() === '') { showError('biography', 'Write a short biography.'); ok = false; }
        if (!ok) { return false; }
        document.getElementById('saveBtn').disabled = true;
        document.getElementById('saveBtn').textContent = 'Saving...';
        return true;
    }

    // ===== Status dot follows the dropdown (admin) =====
    function syncDot(value) {
        var dot = document.getElementById('statusDot');
        dot.classList.remove('status-draft', 'status-review', 'status-approved');
        if (value === 'Draft') { dot.classList.add('status-draft'); }
        else if (value === 'Approved') { dot.classList.add('status-approved'); }
        else { dot.classList.add('status-review'); }
    }
</script>

</body>
</html>
