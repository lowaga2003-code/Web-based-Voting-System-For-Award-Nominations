<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Nominee portal of the Award Nomination System.">
    <title>Nominee Portal | Award Nomination System</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@600;700&family=Manrope:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        /* ===== 1. Page ===== */
        * { box-sizing: border-box; }
        body { margin: 0; background: #f4f2f3; color: #241418; font-family: 'Manrope', 'Segoe UI', sans-serif; line-height: 1.55; }

        /* ===== 2. Top band (award certificate pattern) ===== */
        .hero {
            margin-top: -28px; padding: 104px 20px 112px; color: #f3e6ea;
            background-color: #5b1630;
            background-image:
                repeating-radial-gradient(circle at 12% 135%, rgba(243, 216, 145, 0.12) 0 1px, transparent 1px 11px),
                repeating-radial-gradient(circle at 92% -45%, rgba(243, 216, 145, 0.09) 0 1px, transparent 1px 13px);
        }
        .hero-inner { max-width: 940px; margin: 0 auto; }
        .hero h1 { margin: 0 0 12px; font-family: 'Cormorant Garamond', Georgia, serif; font-size: clamp(2.6rem, 6vw, 3.8rem); line-height: 1; color: #ffffff; }
        .hero p { margin: 0; max-width: 58ch; font-size: 1.08rem; color: #ead7dc; }

        /* ===== 3. Content ===== */
        .page { position: relative; max-width: 980px; margin: -64px auto 96px; padding: 0 20px; }
        .actions { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        .action {
            display: flex; flex-direction: column; gap: 10px; padding: 30px;
            background: #ffffff; border: 1px solid #e6dde0; border-radius: 18px;
            box-shadow: 0 18px 40px rgba(60, 20, 35, 0.10);
        }
        .action-icon { width: 48px; height: 48px; border-radius: 14px; display: flex; align-items: center; justify-content: center; background: #fbf3df; color: #8a5a00; }
        .action-icon svg { width: 26px; height: 26px; }
        .action h2 { margin: 6px 0 0; font-family: 'Cormorant Garamond', Georgia, serif; font-size: 1.85rem; line-height: 1.1; color: #3b0f1f; }
        .action p { margin: 0; color: #5f4d53; }
        .action .push { margin-top: auto; padding-top: 8px; }

        /* ===== 4. Buttons (colour codes are written directly) ===== */
        .btn { display: inline-flex; align-items: center; justify-content: center; width: 100%; padding: 14px 22px; border: 0; border-radius: 12px; font: 700 1rem 'Manrope', 'Segoe UI', sans-serif; text-decoration: none; cursor: pointer; }
        .btn:focus-visible { outline: 3px solid #241418; outline-offset: 3px; }
        .btn-start { background: #7a1d3b; color: #ffffff; }
        .btn-start:hover { background: #611630; }
        .btn-login { background: #c08a1e; color: #241418; }
        .btn-login:hover { background: #a87712; }

        /* ===== 5. Form field + validation message ===== */
        .field { margin: 6px 0 14px; }
        .field label { display: block; margin-bottom: 6px; font-weight: 700; }
        .field input { width: 100%; padding: 13px 14px; border: 1.5px solid #d8cdd1; border-radius: 10px; font: inherit; color: #241418; background: #ffffff; }
        .field input:focus { outline: none; border-color: #7a1d3b; box-shadow: 0 0 0 4px rgba(122, 29, 59, 0.14); }
        .field .help { margin: 6px 0 0; font-size: 0.85rem; color: #6c5a60; }
        .field .error { display: none; margin: 6px 0 0; font-size: 0.85rem; font-weight: 700; color: #b42318; }
        .field.has-error input { border-color: #b42318; }
        .field.has-error .error { display: block; }
        .field.has-error .help { display: none; }

        /* login error from the server */
        .alert { margin-bottom: 20px; padding: 14px 18px; border-radius: 12px; background: #fdecea; border: 1px solid #f5c2bd; color: #8a1c12; }

        /* ===== 6. How it works (3 steps) ===== */
        .steps { margin-top: 44px; }
        .steps h2 { margin: 0 0 16px; font-family: 'Cormorant Garamond', Georgia, serif; font-size: 1.9rem; color: #3b0f1f; }
        .steps ol { list-style: none; margin: 0; padding: 0; display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; counter-reset: step; }
        .steps li { counter-increment: step; display: flex; gap: 14px; align-items: flex-start; color: #5f4d53; }
        .steps li::before {
            content: counter(step); flex: none; width: 40px; height: 40px; border-radius: 50%;
            display: flex; align-items: center; justify-content: center;
            background: #c08a1e; color: #ffffff; font-weight: 800; box-shadow: 0 0 0 4px #f6e7c4;
        }
        .steps strong { display: block; color: #241418; }

        /* ===== 7. Phones ===== */
        @media (max-width: 760px) {
            .actions, .steps ol { grid-template-columns: 1fr; }
            .action { padding: 24px; }
        }
    </style>
</head>
<body>
<%-- [INTEGRATION] system navigation bar (Dashboard / Logout) --%>
<%@ include file="/WEB-INF/jsp/common/portalBar.jspf" %>

<%-- Session එකේ role එක බලලා දෙවෙනි card එක මාරු වෙනවා (ADMIN / USER / login form) --%>
<%
    String homeRole = (String) session.getAttribute("role");
    String homeNic = (String) session.getAttribute("userNic");
    if (homeNic != null) {
        homeNic = homeNic.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
%>

<header class="hero">
    <div class="hero-inner">
        <h1>Nominee portal</h1>
        <p>Submit a nomination, follow its review and book your interview. Start with the card that fits you.</p>
    </div>
</header>

<main class="page">

    <%-- Login error from NomineeController.login() --%>
    <% if (request.getAttribute("error") != null) { %>
    <div class="alert" role="alert"><strong>We could not log you in.</strong> <%= request.getAttribute("error") %></div>
    <% } %>

    <div class="actions">

        <%-- Card 1: new nomination --%>
        <section class="action">
            <span class="action-icon">
                <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M4 20l5-1.5L18.5 9a1.9 1.9 0 0 0-2.7-2.7L6.2 15.7 4 20z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/><path d="M14.2 7.7L17 10.5" stroke="currentColor" stroke-width="1.6"/></svg>
            </span>
            <h2>Submit a nomination</h2>
            <p>Fill in one short form and upload a certificate or letter as evidence. It takes about five minutes.</p>
            <div class="push"><a class="btn btn-start" href="/nominee/form">Start a nomination</a></div>
        </section>

        <%-- Card 2: depends on who is signed in --%>
        <section class="action">
            <span class="action-icon">
                <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><rect x="3" y="5" width="18" height="14" rx="2.5" stroke="currentColor" stroke-width="1.6"/><circle cx="9" cy="11" r="2.2" stroke="currentColor" stroke-width="1.6"/><path d="M5.8 16c.6-1.6 1.8-2.4 3.2-2.4s2.6.8 3.2 2.4M14.5 10h4M14.5 13h3" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
            </span>
            <%-- [INTEGRATION] Admins are already signed in through the main login (NomineeSessionBridge sets role = ADMIN),
                 so they get a direct link instead of the old admin form. Nominees still confirm their NIC here. --%>
            <% if ("ADMIN".equals(homeRole)) { %>
            <h2>Admin panel</h2>
            <p>You are signed in as an administrator through the main login, so no second login is needed.</p>
            <div class="push"><a class="btn btn-login" href="/nominee/list">Open the admin panel</a></div>
            <% } else if ("USER".equals(homeRole) && homeNic != null) { %>
            <h2>Welcome back</h2>
            <p>You are signed in with NIC <strong><%= homeNic %></strong>.</p>
            <div class="push"><a class="btn btn-login" href="/nominee/profile">Open my nomination</a></div>
            <% } else { %>
            <h2>Already nominated?</h2>
            <p>Log in with the NIC number you entered in your nomination form.</p>
            <form action="/nominee/login" method="post" onsubmit="return checkNicLogin()" novalidate>
                <input type="hidden" name="loginType" value="user">
                <div class="field">
                    <label for="nic">NIC number</label>
                    <input type="text" id="nic" name="nic" placeholder="981234567V or 200112345678" autocomplete="off" oninput="clearError('nic')" required>
                    <p class="help">Your NIC is your login ID.</p>
                    <p class="error" aria-live="polite"></p>
                </div>
                <button type="submit" class="btn btn-login">Log in with NIC</button>
            </form>
            <% } %>
        </section>
    </div>

    <%-- How it works: the 3 steps a nominee follows --%>
    <section class="steps">
        <h2>How it works</h2>
        <ol>
            <li><div><strong>Submit your nomination</strong>Add your details and one evidence file.</div></li>
            <li><div><strong>Log in with your NIC</strong>Your NIC number is your login ID.</div></li>
            <li><div><strong>Track and book</strong>Check your status and book an interview slot.</div></li>
        </ol>
    </section>
</main>

<%-- AI assistant (same file on every nominee page) --%>
<%@ include file="nomineeChatbot.jspf" %>

<script>
    // ===== NIC login check (runs before the form is sent) =====
    function showError(id, message) {
        var box = document.getElementById(id).closest('.field');
        box.classList.add('has-error');
        box.querySelector('.error').textContent = message;
    }

    function clearError(id) {
        document.getElementById(id).closest('.field').classList.remove('has-error');
    }

    function checkNicLogin() {
        var nic = document.getElementById('nic').value.trim();
        if (nic === '') {
            showError('nic', 'Enter your NIC number.');
            return false;
        }
        if (!/^[0-9]+[VvXx]?$/.test(nic)) {
            showError('nic', 'A NIC has only numbers, with V or X at the end for old NICs.');
            return false;
        }
        document.getElementById('nic').value = nic.toUpperCase();   // same form as saved: 981234567V
        return true;
    }
</script>

</body>
</html>
