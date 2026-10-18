<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
  MODULE 1 (admin member) - Self registration for normal users (Voter / Nominee).
  [INTEGRATION] Redesigned in the admin member's own style. Uses his REST API: POST /api/auth/register (JSON).
  Voters must verify their email with an OTP -> /verify-registration.
  [INTEGRATION - Module 6] "I agree" checkbox (Privacy Policy + Terms & Conditions). The form is sent as
  /api/auth/register?consent=accepted; Module 6's RegistrationConsentFilter refuses sign-up without it and
  saves a REGISTRATION consent record once the account exists.
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register | Award Nominations</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:wght@500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/admin-module.css">
</head>
<body class="admin-ui">

<div class="a-blob a-blob-1" aria-hidden="true"></div>
<div class="a-blob a-blob-2" aria-hidden="true"></div>

<main class="a-auth">
    <section class="a-glass a-card wide" aria-labelledby="brandTitle">
        <div class="a-brand">
            <h1 id="brandTitle">Request Access</h1>
            <p class="a-tagline">Create an Account</p>
        </div>

        <div id="alertBox" class="a-alert a-alert-error" role="alert" hidden></div>

        <form id="registerForm" novalidate>
            <div class="a-field">
                <span class="a-label" id="roleLabel">Account Type</span>
                <div class="a-segment" role="radiogroup" aria-labelledby="roleLabel">
                    <label>
                        <input type="radio" name="role" value="ROLE_VOTER" checked>
                        <span>Voter<small>Vote for nominees</small></span>
                    </label>
                    <label>
                        <input type="radio" name="role" value="ROLE_NOMINEE">
                        <span>Nominee<small>Submit a nomination</small></span>
                    </label>
                </div>
            </div>
            <div class="a-field">
                <label class="a-label" for="username">Username</label>
                <input class="a-input" type="text" id="username" name="username" autocomplete="username" minlength="3" required>
            </div>
            <div class="a-field">
                <label class="a-label" for="email">Email Address</label>
                <input class="a-input" type="email" id="email" name="email" autocomplete="email" required>
            </div>
            <div class="a-grid-2">
                <div class="a-field">
                    <label class="a-label" for="password">Password</label>
                    <input class="a-input" type="password" id="password" name="password" autocomplete="new-password" minlength="6" required>
                </div>
                <div class="a-field">
                    <label class="a-label" for="confirmPassword">Confirm Password</label>
                    <input class="a-input" type="password" id="confirmPassword" name="confirmPassword" autocomplete="new-password" minlength="6" required>
                </div>
            </div>
            <p class="a-hint">Staff accounts (manager, auditor, chairperson, legal officer) are created by the System Administrator.</p>
            <%-- [INTEGRATION - Module 6] consent is required before an account can be created --%>
            <label class="a-consent" for="consent">
                <input type="checkbox" id="consent" name="consent" value="accepted" required>
                <span>I have read and agree to the <a href="/policy/privacy" target="_blank" rel="noopener">Privacy Policy</a>
                    and the <a href="/policy/terms" target="_blank" rel="noopener">Terms &amp; Conditions</a>.</span>
            </label>
            <button type="submit" id="submitBtn" class="a-btn a-btn-gold a-btn-block">Submit Registration</button>
        </form>

        <p class="a-foot">Already have an account?<a href="/login">Sign in here</a></p>
    </section>
</main>

<script>
    (function () {
        var form = document.getElementById('registerForm');
        var alertBox = document.getElementById('alertBox');
        var submitBtn = document.getElementById('submitBtn');

        function showError(message) {
            alertBox.textContent = String(message || 'Registration failed.').replace(/^Error:\s*/, '');
            alertBox.hidden = false;
            submitBtn.disabled = false;
            submitBtn.textContent = 'Submit Registration';
        }

        form.addEventListener('submit', function (event) {
            event.preventDefault();
            alertBox.hidden = true;

            var f = form.elements;
            var checkedRole = form.querySelector('input[name="role"]:checked');
            var data = {
                username: f['username'].value.trim(),
                email: f['email'].value.trim(),
                password: f['password'].value,
                role: checkedRole ? checkedRole.value : 'ROLE_VOTER'
            };

            if (!f['consent'].checked) {
                showError('Please agree to the Privacy Policy and the Terms & Conditions to create your account.');
                f['consent'].focus();
                return;
            }
            if (!form.checkValidity()) {
                form.reportValidity();
                return;
            }
            if (data.password !== f['confirmPassword'].value) {
                showError('Passwords do not match.');
                return;
            }

            submitBtn.disabled = true;
            submitBtn.textContent = 'Submitting…';

            // [INTEGRATION - Module 6] tells the server the user ticked "I agree"
            fetch('/api/auth/register?consent=accepted', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify(data)
            })
                .then(function (res) {
                    return res.json()
                        .catch(function () { return {}; })
                        .then(function (body) { return { ok: res.ok, body: body || {} }; });
                })
                .then(function (result) {
                    if (!result.ok) {
                        showError(result.body.message);
                    } else if (result.body.requiresOtp) {
                        window.location.href = '/verify-registration?userId=' + encodeURIComponent(result.body.userId);
                    } else {
                        window.location.href = '/login?registered';
                    }
                })
                .catch(function () {
                    showError('Network error occurred. Please try again.');
                });
        });
    })();
</script>
</body>
</html>
