<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
  MODULE 1 (admin member) - Two-factor (OTP) step of the voter login.
  [INTEGRATION] Redesigned in the admin member's own style. Uses his REST API: POST /api/auth/verify-login
  After success the voter goes to the main dashboard.
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Two-Factor Authentication | Award Nominations</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:wght@500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/admin-module.css">
</head>
<body class="admin-ui">

<div class="a-blob a-blob-1" aria-hidden="true"></div>
<div class="a-blob a-blob-2" aria-hidden="true"></div>

<main class="a-auth">
    <section class="a-glass a-card" aria-labelledby="otpTitle">
        <div class="a-brand">
            <span class="a-brand-mark" aria-hidden="true">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.6" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
            </span>
            <h1 id="otpTitle" style="font-size:1.8rem; letter-spacing:0.04em; text-transform:none;">Verification Required</h1>
        </div>
        <p class="a-intro">We've sent a 6-digit one-time password to your email. Enter it below to securely log in. It expires in 5 minutes.</p>

        <div id="errorMsg" class="a-alert a-alert-error" role="alert" hidden></div>

        <form id="verifyForm" novalidate>
            <div class="a-field">
                <label class="a-label" for="otpCode">One-time password</label>
                <input class="a-input a-otp" type="text" id="otpCode" name="otpCode" inputmode="numeric"
                       autocomplete="one-time-code" pattern="[0-9]{6}" maxlength="6" placeholder="······" required autofocus>
            </div>
            <p class="a-hint">Demo: the mock email service prints this code in the IntelliJ run console.</p>
            <button type="submit" id="submitBtn" class="a-btn a-btn-gold a-btn-block">Verify and Log In</button>
        </form>

        <p class="a-foot">Didn't receive the code?<a href="/login">Back to login</a></p>
    </section>
</main>

<script>
    (function () {
        var form = document.getElementById('verifyForm');
        var errorMsg = document.getElementById('errorMsg');
        var submitBtn = document.getElementById('submitBtn');

        function showError(message) {
            errorMsg.textContent = String(message || 'Invalid OTP');
            errorMsg.hidden = false;
            submitBtn.disabled = false;
            submitBtn.textContent = 'Verify and Log In';
        }

        form.addEventListener('submit', function (event) {
            event.preventDefault();
            errorMsg.hidden = true;

            var otp = document.getElementById('otpCode').value.trim();
            if (!/^[0-9]{6}$/.test(otp)) {
                showError('Enter the 6-digit code.');
                return;
            }

            submitBtn.disabled = true;
            submitBtn.textContent = 'Verifying…';

            var body = new URLSearchParams();
            body.append('otpCode', otp);

            fetch('/api/auth/verify-login', { method: 'POST', body: body, headers: { 'Accept': 'application/json' } })
                .then(function (res) {
                    return res.json()
                        .catch(function () { return {}; })
                        .then(function (data) { return { ok: res.ok, data: data || {} }; });
                })
                .then(function (result) {
                    if (result.ok) {
                        window.location.href = '/dashboard';
                    } else {
                        showError(result.data.message);
                    }
                })
                .catch(function () {
                    showError('Network error. Please try again.');
                });
        });
    })();
</script>
</body>
</html>
