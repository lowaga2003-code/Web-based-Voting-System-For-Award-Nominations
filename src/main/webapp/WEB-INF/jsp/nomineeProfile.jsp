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
    <title>My Nomination | Award Nomination System</title>
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
        .hero-inner { max-width: 720px; margin: 0 auto; }
        .hero .back { color: #f3d891; font-weight: 700; font-size: 0.92rem; text-decoration: none; }
        .hero .back:hover { text-decoration: underline; }
        .hero h1 { margin: 14px 0 10px; font-family: 'Cormorant Garamond', Georgia, serif; font-size: clamp(2.4rem, 5.5vw, 3.4rem); line-height: 1; color: #ffffff; }
        .hero p { margin: 0; color: #ead7dc; }

        /* ===== 3. Cards ===== */
        .page { position: relative; max-width: 760px; margin: -60px auto 110px; padding: 0 20px; display: grid; gap: 20px; }
        .card { padding: 30px; background: #ffffff; border: 1px solid #e6dde0; border-radius: 18px; }
        .card:first-child { box-shadow: 0 18px 40px rgba(60, 20, 35, 0.10); }
        .card h2 { margin: 0 0 16px; font-family: 'Cormorant Garamond', Georgia, serif; font-size: 1.7rem; color: #3b0f1f; }

        /* ===== 4. Status: 3 seals (Draft -> Under review -> Approved) ===== */
        .seals { list-style: none; margin: 0 0 16px; padding: 0; display: grid; grid-template-columns: repeat(3, 1fr); position: relative; }
        .seals::before, .seals::after { content: ''; position: absolute; top: 21px; left: 16%; height: 3px; }
        .seals::before { right: 16%; background: #ece4e7; }
        .seals::after { width: 0; background: #c08a1e; }      /* gold line up to the current step */
        .seals.step-2::after { width: 34%; }
        .seals.step-3::after { width: 68%; }
        .seal { position: relative; display: flex; flex-direction: column; align-items: center; gap: 8px; text-align: center; font-weight: 700; color: #8b7a80; }
        .seal .mark { position: relative; z-index: 1; width: 44px; height: 44px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #ffffff; border: 3px solid #e3d9dd; font-weight: 800; }
        .seal.done .mark { background: #c08a1e; border-color: #c08a1e; color: #ffffff; }
        .seal.current { color: #241418; }
        .seal.current .mark { box-shadow: 0 0 0 6px #f6e7c4; }
        .status-text { margin: 0; padding: 12px 16px; border-radius: 12px; background: #faf6ee; color: #5f4d53; }

        /* ===== 5. Interview card ===== */
        .interview { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
        .interview h2 { margin-bottom: 4px; }
        .interview p { margin: 0; color: #5f4d53; }

        /* ===== 6. Fields + validation messages ===== */
        .field { margin-bottom: 18px; }
        .field label { display: block; margin-bottom: 6px; font-weight: 700; }
        .field input, .field textarea { width: 100%; padding: 12px 14px; border: 1.5px solid #d8cdd1; border-radius: 10px; font: inherit; color: #241418; background: #ffffff; }
        .field textarea { min-height: 130px; resize: vertical; }
        .field input:focus, .field textarea:focus { outline: none; border-color: #7a1d3b; box-shadow: 0 0 0 4px rgba(122, 29, 59, 0.14); }
        .field input:disabled { background: #f2eef0; color: #6c5a60; }
        .field .help { margin: 6px 0 0; font-size: 0.85rem; color: #6c5a60; }
        .field .error { display: none; margin: 6px 0 0; font-size: 0.85rem; font-weight: 700; color: #b42318; }
        .field.has-error input, .field.has-error textarea { border-color: #b42318; }
        .field.has-error .error { display: block; }
        .counter { float: right; font-size: 0.85rem; font-weight: 700; color: #8a5a00; }
        .note { margin: 4px 0 18px; padding: 12px 16px; border-radius: 12px; background: #fff7e6; border: 1px solid #f1d9a6; color: #6b4a07; font-size: 0.92rem; }

        /* ===== 7. Buttons (colour codes are written directly) ===== */
        .btn { display: inline-flex; align-items: center; justify-content: center; padding: 14px 22px; border-radius: 12px; border: 0; font: 700 1rem 'Manrope', 'Segoe UI', sans-serif; text-decoration: none; cursor: pointer; }
        .btn:focus-visible { outline: 3px solid #241418; outline-offset: 3px; }
        .btn-update { width: 100%; background: #7a1d3b; color: #ffffff; }
        .btn-update:hover { background: #611630; }
        .btn-update:disabled { background: #b98a99; cursor: wait; }
        .btn-interview { background: #c08a1e; color: #241418; }
        .btn-interview:hover { background: #a87712; }

        @media (max-width: 560px) { .card { padding: 24px 20px; } .seal { font-size: 0.85rem; } }
    </style>
</head>
<body>
<%-- [INTEGRATION] system navigation bar (Dashboard / Logout) --%>
<%@ include file="/WEB-INF/jsp/common/portalBar.jspf" %>

<%
    Nominee nominee = (Nominee) request.getAttribute("nominee");

    // status -> step number for the 3 seals (1 Draft, 2 Under Review, 3 Approved)
    String status = (nominee != null && nominee.getStatus() != null) ? nominee.getStatus() : "Draft";
    int step = 1;
    String statusText = "Your nomination is saved. The award office has not started the review yet.";
    if ("Under Review".equals(status)) {
        step = 2;
        statusText = "The selection committee is checking your details and evidence.";
    } else if ("Approved".equals(status)) {
        step = 3;
        statusText = "Your nomination is approved. Make sure you have booked an interview slot.";
    }
%>

<header class="hero">
    <div class="hero-inner">
        <a class="back" href="/nominee/home">Back to the nominee portal</a>
        <h1>My nomination</h1>
        <p>Welcome<%= nominee != null ? ", " + esc(nominee.getFullName()) : "" %>. Check your status and keep your details up to date.</p>
    </div>
</header>

<main class="page">
    <% if (nominee == null) { %>
    <section class="card">
        <h2>We could not find your nomination</h2>
        <p>Log in again with the NIC number from your nomination form.</p>
        <a class="btn btn-interview" href="/nominee/home">Go to the nominee portal</a>
    </section>
    <% } else { %>

    <%-- Status --%>
    <section class="card">
        <h2>Status</h2>
        <ol class="seals step-<%= step %>">
            <li class="seal<%= step >= 1 ? " done" : "" %><%= step == 1 ? " current" : "" %>"><span class="mark">1</span>Draft</li>
            <li class="seal<%= step >= 2 ? " done" : "" %><%= step == 2 ? " current" : "" %>"><span class="mark">2</span>Under review</li>
            <li class="seal<%= step >= 3 ? " done" : "" %><%= step == 3 ? " current" : "" %>"><span class="mark">3</span>Approved</li>
        </ol>
        <p class="status-text"><%= statusText %></p>
    </section>

    <%-- Interview --%>
    <section class="card interview">
        <div>
            <h2>Interview</h2>
            <p>Choose a time to meet the selection committee.</p>
        </div>
        <a class="btn btn-interview" href="/nominee/interviews">Book an interview</a>
    </section>

    <%-- Details form: POST /nominee/update/{id} -> NomineeController.updateNominee() --%>
    <form class="card" action="/nominee/update/<%= nominee.getId() %>" method="post" onsubmit="return validateProfile()" novalidate>
        <h2>Your details</h2>
        <div class="field">
            <label for="nicView">NIC number</label>
            <input type="text" id="nicView" value="<%= esc(nominee.getNic()) %>" disabled>
            <p class="help">Your NIC is your login ID, so it cannot be changed.</p>
        </div>
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
            <label for="biography">Biography <span class="counter" id="wordCount"></span></label>
            <textarea id="biography" name="biography" required oninput="clearError('biography'); countWords()"><%= esc(nominee.getBiography()) %></textarea>
            <p class="error" aria-live="polite"></p>
        </div>
        <p class="note">Saving sends your nomination back to <strong>Under review</strong>, so the committee can check the changes.</p>
        <button type="submit" class="btn btn-update" id="saveBtn">Save changes</button>
    </form>
    <% } %>
</main>

<%-- AI assistant (same file on every nominee page) --%>
<%@ include file="nomineeChatbot.jspf" %>

<script>
    // ===== Validation for the details form =====
    function showError(id, message) {
        var box = document.getElementById(id).closest('.field');
        box.classList.add('has-error');
        box.querySelector('.error').textContent = message;
    }

    function clearError(id) {
        document.getElementById(id).closest('.field').classList.remove('has-error');
    }

    function countWords() {
        var box = document.getElementById('biography');
        if (!box) { return; }
        var text = box.value.trim();
        var words = text === '' ? 0 : text.split(/\s+/).length;
        document.getElementById('wordCount').textContent = words + (words === 1 ? ' word' : ' words');
    }

    function validateProfile() {
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

    countWords();
</script>

</body>
</html>
