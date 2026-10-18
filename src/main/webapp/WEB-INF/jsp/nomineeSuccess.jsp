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
    <title>Nomination Received | Award Nomination System</title>
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
        .hero-inner { max-width: 600px; margin: 0 auto; }
        .hero h1 { margin: 0 0 10px; font-family: 'Cormorant Garamond', Georgia, serif; font-size: clamp(2.4rem, 5.5vw, 3.4rem); line-height: 1; color: #ffffff; }
        .hero p { margin: 0; color: #ead7dc; }

        /* ===== 3. Certificate card ===== */
        .page { position: relative; max-width: 640px; margin: -60px auto 110px; padding: 0 20px; }
        .certificate { padding: 10px; background: #ffffff; border: 1px solid #e6dde0; border-radius: 18px; box-shadow: 0 18px 40px rgba(60, 20, 35, 0.10); }
        .certificate-inner { padding: 34px 30px; border: 1.5px solid #e2c27a; border-radius: 12px; text-align: center; }
        .seal { width: 76px; height: 76px; margin: 0 auto 16px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #c08a1e; color: #ffffff; box-shadow: 0 0 0 6px #f6e7c4; }
        .seal svg { width: 38px; height: 38px; }
        .certificate h2 { margin: 0 0 8px; font-family: 'Cormorant Garamond', Georgia, serif; font-size: 2rem; color: #3b0f1f; }
        .certificate p { margin: 0 auto; max-width: 46ch; color: #5f4d53; }

        /* NIC = login ID, shown like a certificate number */
        .nic-box { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 12px; margin: 24px 0; padding: 14px 16px; border-radius: 12px; background: #fbf3df; text-align: left; }
        .nic-label { display: block; font-size: 0.85rem; color: #6c5a60; }
        .nic-value { display: block; font-size: 1.35rem; font-weight: 800; letter-spacing: 0.04em; color: #3b0f1f; font-variant-numeric: tabular-nums; }

        /* what happens next */
        .next { margin: 0 0 26px; padding: 0; list-style: none; text-align: left; counter-reset: step; }
        .next li { counter-increment: step; display: flex; gap: 12px; padding: 8px 0; color: #5f4d53; }
        .next li::before { content: counter(step); flex: none; width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #f3e6ea; color: #7a1d3b; font-weight: 800; font-size: 0.85rem; }

        /* ===== 4. Buttons (colour codes are written directly) ===== */
        .buttons { display: grid; gap: 10px; }
        .btn { display: inline-flex; align-items: center; justify-content: center; padding: 14px 20px; border-radius: 12px; font: 700 1rem 'Manrope', 'Segoe UI', sans-serif; text-decoration: none; cursor: pointer; }
        .btn:focus-visible { outline: 3px solid #241418; outline-offset: 3px; }
        .btn-login { background: #7a1d3b; color: #ffffff; border: 0; }
        .btn-login:hover { background: #611630; }
        .btn-again { background: #ffffff; color: #7a1d3b; border: 1.5px solid #7a1d3b; }
        .btn-again:hover { background: #f8eef1; }
        .btn-copy { flex: none; padding: 9px 14px; background: #ffffff; color: #3b0f1f; border: 1.5px solid #c08a1e; font-size: 0.9rem; }
        .btn-copy:hover { background: #fff8e8; }
    </style>
</head>
<body>
<%-- [INTEGRATION] system navigation bar (Dashboard / Logout) --%>
<%@ include file="/WEB-INF/jsp/common/portalBar.jspf" %>

<header class="hero">
    <div class="hero-inner">
        <h1>Nomination received</h1>
        <p>Keep your NIC number safe. You will use it to log in and follow your nomination.</p>
    </div>
</header>

<main class="page">
    <section class="certificate">
        <div class="certificate-inner">
            <div class="seal">
                <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M6 12.5l4 4L18 8" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </div>
            <h2>Your nomination is saved</h2>
            <p>It starts as a <strong>Draft</strong>. The award office reviews it next.</p>

            <%-- NIC comes from NomineeController.submitNominee(): model.addAttribute("nic", nic) --%>
            <div class="nic-box">
                <div>
                    <span class="nic-label">Your login ID (NIC)</span>
                    <span class="nic-value" id="nicValue"><%= esc((String) request.getAttribute("nic")) %></span>
                </div>
                <button type="button" class="btn btn-copy" id="copyBtn" onclick="copyNic()">Copy</button>
            </div>

            <ol class="next">
                <li>Log in on the nominee portal with this NIC.</li>
                <li>Check your status on My nomination.</li>
                <li>Book an interview slot when the award office opens them.</li>
            </ol>

            <div class="buttons">
                <a class="btn btn-login" href="/nominee/home">Log in now</a>
                <a class="btn btn-again" href="/nominee/form">Submit another nomination</a>
            </div>
        </div>
    </section>
</main>

<%-- AI assistant (same file on every nominee page) --%>
<%@ include file="nomineeChatbot.jspf" %>

<script>
    // Copy the NIC so the nominee can paste it on the login page
    function copyNic() {
        var nicBox = document.getElementById('nicValue');
        var button = document.getElementById('copyBtn');
        if (navigator.clipboard) {
            navigator.clipboard.writeText(nicBox.textContent.trim()).then(function () { button.textContent = 'Copied'; });
        } else {
            // older browsers / plain http: select the NIC and copy the selection
            window.getSelection().selectAllChildren(nicBox);
            document.execCommand('copy');
            button.textContent = 'Copied';
        }
    }
</script>

</body>
</html>
