<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>New Nomination | Award Nomination System</title>
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

        /* ===== 3. Form card ===== */
        .page { position: relative; max-width: 720px; margin: -60px auto 110px; padding: 0 20px; }
        .card { padding: 34px; background: #ffffff; border: 1px solid #e6dde0; border-radius: 18px; box-shadow: 0 18px 40px rgba(60, 20, 35, 0.10); }
        fieldset { margin: 0 0 30px; padding: 0; border: 0; }
        legend { display: flex; align-items: center; gap: 12px; margin-bottom: 18px; font-family: 'Cormorant Garamond', Georgia, serif; font-size: 1.6rem; font-weight: 700; color: #3b0f1f; }
        .num { width: 34px; height: 34px; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; background: #c08a1e; color: #ffffff; font: 800 0.95rem 'Manrope', sans-serif; box-shadow: 0 0 0 4px #f6e7c4; }

        /* ===== 4. Fields + validation messages ===== */
        .field { margin-bottom: 20px; }
        .field label { display: block; margin-bottom: 6px; font-weight: 700; }
        .field .req { color: #b42318; }
        .field input[type="text"], .field textarea { width: 100%; padding: 12px 14px; border: 1.5px solid #d8cdd1; border-radius: 10px; font: inherit; color: #241418; background: #ffffff; }
        .field textarea { min-height: 130px; resize: vertical; }
        .field input:focus, .field textarea:focus { outline: none; border-color: #7a1d3b; box-shadow: 0 0 0 4px rgba(122, 29, 59, 0.14); }
        .field .help { margin: 6px 0 0; font-size: 0.85rem; color: #6c5a60; }
        .field .error { display: none; margin: 6px 0 0; font-size: 0.85rem; font-weight: 700; color: #b42318; }
        .field.has-error input, .field.has-error textarea, .field.has-error .dropzone { border-color: #b42318; }
        .field.has-error .error { display: block; }
        .counter { float: right; font-size: 0.85rem; font-weight: 700; color: #8a5a00; }

        /* evidence upload box */
        .dropzone { display: block; padding: 26px; text-align: center; border: 2px dashed #d8cdd1; border-radius: 14px; background: #faf8f9; cursor: pointer; }
        .dropzone:hover, .dropzone.dragover { border-color: #c08a1e; background: #fdf8ec; }
        .dropzone.has-file { border-style: solid; border-color: #1f7a4d; background: #effaf3; }
        .field:focus-within .dropzone { border-color: #7a1d3b; box-shadow: 0 0 0 4px rgba(122, 29, 59, 0.14); }   /* keyboard users see where they are */
        .dropzone svg { width: 30px; height: 30px; color: #c08a1e; }
        .dropzone .hint { display: block; margin-top: 6px; font-weight: 600; color: #5f4d53; }
        .file-input { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0, 0, 0, 0); }

        /* error sent back by the server */
        .alert { margin-bottom: 20px; padding: 14px 18px; border-radius: 12px; background: #fdecea; border: 1px solid #f5c2bd; color: #8a1c12; }

        /* ===== 5. Buttons (colour codes are written directly) ===== */
        .btn-submit { width: 100%; padding: 15px; border: 0; border-radius: 12px; background: #7a1d3b; color: #ffffff; font: 700 1.02rem 'Manrope', 'Segoe UI', sans-serif; cursor: pointer; }
        .btn-submit:hover { background: #611630; }
        .btn-submit:disabled { background: #b98a99; cursor: wait; }
        .btn-submit:focus-visible { outline: 3px solid #241418; outline-offset: 3px; }

        @media (max-width: 560px) { .card { padding: 24px 20px; } }
    </style>
</head>
<body>
<%-- [INTEGRATION] system navigation bar (Dashboard / Logout) --%>
<%@ include file="/WEB-INF/jsp/common/portalBar.jspf" %>

<header class="hero">
    <div class="hero-inner">
        <a class="back" href="/nominee/home">Back to the nominee portal</a>
        <h1>New nomination</h1>
        <p>Three short steps. Fields marked * are required.</p>
    </div>
</header>

<main class="page">

    <%-- Error from the server check in NomineeController.submitNominee() (file type / size) --%>
    <% if (request.getAttribute("errorMessage") != null) { %>
    <div class="alert" role="alert"><strong>Your nomination was not saved.</strong> <%= request.getAttribute("errorMessage") %></div>
    <% } %>

    <form class="card" action="/nominee/submit" method="POST" enctype="multipart/form-data" onsubmit="return validateForm()" novalidate>

        <%-- Step 1 --%>
        <fieldset>
            <legend><span class="num">1</span>About the nominee</legend>
            <div class="field">
                <label for="fullName">Full name <span class="req">*</span></label>
                <input type="text" id="fullName" name="fullName" required
                       onkeypress="return allowOnlyLetters(event)" onblur="checkField('fullName')" oninput="clearError('fullName')">
                <p class="help">Letters and spaces only, as written on the NIC.</p>
                <p class="error" aria-live="polite"></p>
            </div>
            <div class="field">
                <label for="nic">NIC number <span class="req">*</span></label>
                <input type="text" id="nic" name="nic" placeholder="981234567V or 200112345678" required
                       onkeypress="return allowOnlyNicChars(event)" onblur="checkField('nic')" oninput="clearError('nic')">
                <p class="help">Old NIC: 9 digits and V or X. New NIC: 12 digits. This becomes your login ID.</p>
                <p class="error" aria-live="polite"></p>
            </div>
        </fieldset>

        <%-- Step 2 --%>
        <fieldset>
            <legend><span class="num">2</span>Work and achievements</legend>
            <div class="field">
                <label for="companyName">Company or organisation <span class="req">*</span></label>
                <input type="text" id="companyName" name="companyName" required onblur="checkField('companyName')" oninput="clearError('companyName')">
                <p class="error" aria-live="polite"></p>
            </div>
            <div class="field">
                <label for="biography">Biography <span class="req">*</span> <span class="counter" id="wordCount">0 words</span></label>
                <textarea id="biography" name="biography" required onblur="checkField('biography')" oninput="clearError('biography'); countWords()"></textarea>
                <p class="help">Say what the nominee did and why it matters. Some award categories ask for at least 20 words.</p>
                <p class="error" aria-live="polite"></p>
            </div>
        </fieldset>

        <%-- Step 3 --%>
        <fieldset>
            <legend><span class="num">3</span>Evidence</legend>
            <div class="field">
                <label class="dropzone" id="dropzone" for="evidenceFile">
                    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M12 15V4m0 0L7 9m5-5l5 5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/><path d="M4 16v3a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-3" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>
                    <span class="hint" id="dzHint">Drag a certificate or letter here, or click to choose a file</span>
                </label>
                <input class="file-input" type="file" id="evidenceFile" name="evidenceFile" accept=".pdf,.png,.jpg,.jpeg" required>
                <p class="help">PDF, PNG or JPG, up to 5 MB.</p>
                <p class="error" aria-live="polite"></p>
            </div>
        </fieldset>

        <button type="submit" class="btn-submit" id="submitBtn">Submit nomination</button>
    </form>
</main>

<%-- AI assistant (same file on every nominee page) --%>
<%@ include file="nomineeChatbot.jspf" %>

<script>
    // ===== 1. Show or clear the red message under a field =====
    function showError(id, message) {
        var box = document.getElementById(id).closest('.field');
        box.classList.add('has-error');
        box.querySelector('.error').textContent = message;
    }

    function clearError(id) {
        document.getElementById(id).closest('.field').classList.remove('has-error');
    }

    // ===== 2. Block wrong keys while typing, and say why =====
    function allowOnlyLetters(e) {
        if (e.key === 'Enter') { return true; }   // Enter sends the form
        var key = String.fromCharCode(e.which || e.keyCode);
        if (/[A-Za-z ]/.test(key)) { return true; }
        showError(e.target.id, 'Only letters and spaces are allowed here.');
        return false;
    }

    function allowOnlyNicChars(e) {
        if (e.key === 'Enter') { return true; }
        var key = String.fromCharCode(e.which || e.keyCode);
        if (/[0-9VvXx]/.test(key)) { return true; }
        showError('nic', 'A NIC has only numbers, and V or X for old NICs.');
        return false;
    }

    // ===== 3. Check one field (runs when you leave the field and before sending) =====  /////////////validation////////
    function checkField(id) {
        var value = document.getElementById(id).value.trim();

        if (id === 'fullName') {
            if (value === '') { showError(id, 'Enter the nominee\'s full name.'); return false; }
            if (!/^[A-Za-z ]{3,}$/.test(value)) { showError(id, 'Use at least 3 letters, and letters and spaces only.'); return false; }
        }
        if (id === 'nic') {
            if (value === '') { showError(id, 'Enter the NIC number.'); return false; }
            if (!/^([0-9]{9}[VvXx]|[0-9]{12})$/.test(value)) { showError(id, 'Use 9 digits and V or X (981234567V), or 12 digits (200112345678).'); return false; }
        }
        if (id === 'companyName' && value === '') { showError(id, 'Enter the company or organisation name.'); return false; }
        if (id === 'biography' && value === '') { showError(id, 'Write a short biography.'); return false; }

        clearError(id);
        return true;
    }

    // ===== 4. Live word counter for the biography =====
    function countWords() {
        var text = document.getElementById('biography').value.trim();
        var words = text === '' ? 0 : text.split(/\s+/).length;
        document.getElementById('wordCount').textContent = words + (words === 1 ? ' word' : ' words');
    }

    // ===== 5. Evidence file: type and size (the server checks again) =====           /////////////////validation////////////
    function checkFile() {
        var input = document.getElementById('evidenceFile');
        if (input.files.length === 0) { showError('evidenceFile', 'Choose an evidence file.'); return false; }
        var file = input.files[0];
        if (file.type !== 'application/pdf' && file.type !== 'image/png' && file.type !== 'image/jpeg') {
            showError('evidenceFile', 'This file type is not allowed. Use a PDF, PNG or JPG file.');
            return false;
        }
        if (file.size > 5 * 1024 * 1024) {
            showError('evidenceFile', 'This file is larger than 5 MB. Choose a smaller file.');
            return false;
        }
        clearError('evidenceFile');
        return true;
    }

    // ===== 6. Before sending: check every field =====
    function validateForm() {
        var ok = true;
        var ids = ['fullName', 'nic', 'companyName', 'biography'];
        for (var i = 0; i < ids.length; i++) {
            if (!checkField(ids[i])) { ok = false; }
        }
        if (!checkFile()) { ok = false; }

        if (!ok) {
            document.querySelector('.has-error').scrollIntoView({ behavior: 'smooth', block: 'center' });
            return false;
        }
        // NIC is saved the same way every time (no spaces, capital V/X), because it is the login ID
        document.getElementById('nic').value = document.getElementById('nic').value.trim().toUpperCase();
        var button = document.getElementById('submitBtn');
        button.disabled = true;
        button.textContent = 'Submitting...';
        return true;
    }

    // ===== 7. Evidence box: click or drag a file =====
    (function () {
        var dropzone = document.getElementById('dropzone');
        var input = document.getElementById('evidenceFile');
        var hint = document.getElementById('dzHint');

        function showFileName() {
            if (input.files.length > 0) {
                hint.textContent = 'Selected: ' + input.files[0].name;
                dropzone.classList.toggle('has-file', checkFile());
            }
        }

        input.addEventListener('change', showFileName);
        dropzone.addEventListener('dragover', function (e) { e.preventDefault(); dropzone.classList.add('dragover'); });
        dropzone.addEventListener('dragleave', function () { dropzone.classList.remove('dragover'); });
        dropzone.addEventListener('drop', function (e) {
            e.preventDefault();
            dropzone.classList.remove('dragover');
            if (e.dataTransfer.files.length > 0) {
                input.files = e.dataTransfer.files;
                showFileName();
            }
        });
    })();
</script>

</body>
</html>
