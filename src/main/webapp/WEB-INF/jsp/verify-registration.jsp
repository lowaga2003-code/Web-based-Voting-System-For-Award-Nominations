<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
  MODULE 1 (admin member) - Email verification (OTP) after a voter registers.
  [INTEGRATION] Redesigned in the admin member's own style. Uses his REST API: POST /api/auth/verify-registration
--%>
<%
    String userIdParam = request.getParameter("userId");
    String userId = (userIdParam != null && userIdParam.matches("\\d+")) ? userIdParam : null;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verify Email | Award Nominations</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:wght@500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/admin-module.css">
</head>
<body class="admin-ui">

<div class="a-blob a-blob-1" aria-hidden="true"></div>
<div class="a-blob a-blob-2" aria-hidden="true"></div>

<main class="a-auth">
    <section class="a-glass a-card" aria-labelledby="verifyTitle">
        <div class="a-brand">
            <span class="a-brand-mark" aria-hidden="true">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.6" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/></svg>
            </span>
            <h1 id="verifyTitle" style="font-size:1.8rem; letter-spacing:0.04em; text-transform:none;">Verify Your Email</h1>
        </div>

        <% if (userId == null) { %>
        <div class="a-alert a-alert-error" role="alert">This verification link is incomplete. Please register again.</div>
        <a class="a-btn a-btn-outline a-btn-block" href="/register">Back to registration</a>
        <% } else { %>
        <p class="a-intro">We've sent a 6-digit one-time password to your email to verify your new account. It expires in 5 minutes.</p>

        <div id="errorMsg" class="a-alert a-alert-error" role="alert" hidden></div>
        <div id="successMsg" class="a-alert a-alert-ok" role="status" hidden></div>

        <form id="verifyForm" novalidate>
            <input type="hidden" id="userId" value="<%= userId %>">
            <div class="a-field">
                <label class="a-label" for="otpCode">One-time password</label>
                <input class="a-input a-otp" type="text" id="otpCode" name="otpCode" inputmode="numeric"
                       autocomplete="one-time-code" pattern="[0-9]{6}" maxlength="6" placeholder="······" required autofocus>
            </div>
            <p class="a-hint">Demo: the mock email service prints this code in the IntelliJ run console.</p>
            <button type="submit" id="submitBtn" class="a-btn a-btn-gold a-btn-block">Verify Email</button>
        </form>
        <% } %>

        <p class="a-foot">Wrong account?<a href="/login">Back to login</a></p>
    </section>
</main>

<% if (userId != null) { %>
<script>
    (function () {
        var form = document.getElementById('verifyForm');
        var errorMsg = document.getElementById('errorMsg');
        var successMsg = document.getElementById('successMsg');
        var submitBtn = document.getElementById('submitBtn');

        function showError(message) {
            errorMsg.textContent = String(message || 'Invalid OTP');
            errorMsg.hidden = false;
            submitBtn.disabled = false;
            submitBtn.textContent = 'Verify Email';
        }

        form.addEventListener('submit', function (event) {
            event.preventDefault();
            errorMsg.hidden = true;
            successMsg.hidden = true;

            var otp = document.getElementById('otpCode').value.trim();
            if (!/^[0-9]{6}$/.test(otp)) {
                showError('Enter the 6-digit code.');
                return;
            }

            submitBtn.disabled = true;
            submitBtn.textContent = 'Verifying…';

            var body = new URLSearchParams();
            body.append('userId', document.getElementById('userId').value);
            body.append('otpCode', otp);

            fetch('/api/auth/verify-registration', { method: 'POST', body: body, headers: { 'Accept': 'application/json' } })
                .then(function (res) {
                    return res.json()
                        .catch(function () { return {}; })
                        .then(function (data) { return { ok: res.ok, data: data || {} }; });
                })
                .then(function (result) {
                    if (result.ok) {
                        successMsg.textContent = 'Email verified successfully! Redirecting to login…';
                        successMsg.hidden = false;
                        setTimeout(function () { window.location.href = '/login?verified'; }, 1500);
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
<% } %>
</body>
</html>
