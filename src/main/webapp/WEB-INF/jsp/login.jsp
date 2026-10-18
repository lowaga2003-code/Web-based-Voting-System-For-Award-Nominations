<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
  MODULE 1 (admin member) - Main login of the whole system (Spring Security form login).
  [INTEGRATION] Redesigned in the admin member's own style (slate + metallic gold). No JSTL / Tailwind needed.
  Admins and normal users use the same form; CustomAuthenticationSuccessHandler sends each role to the dashboard
  (voters first go to the OTP page).
--%>
<%
    String errorMsg = null;
    String okMsg = null;
    if (request.getParameter("error") != null) {
        Object lastError = session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        String errorType = (lastError != null) ? lastError.getClass().getSimpleName() : "";
        if ("DisabledException".equals(errorType)) {
            errorMsg = "This account is suspended or its email is not verified yet.";
        } else if ("LockedException".equals(errorType)) {
            errorMsg = "This account is locked. Please contact the system administrator.";
        } else {
            errorMsg = "Invalid email or password.";
        }
    } else if (request.getParameter("logout") != null) {
        okMsg = "You have been logged out.";
    } else if (request.getParameter("registered") != null) {
        okMsg = "Registration successful. You can now sign in.";
    } else if (request.getParameter("verified") != null) {
        okMsg = "Email verified. You can now sign in.";
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login | Award Nominations</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:wght@500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/admin-module.css">
</head>
<body class="admin-ui">

<div class="a-blob a-blob-1" aria-hidden="true"></div>
<div class="a-blob a-blob-2" aria-hidden="true"></div>

<main class="a-auth">
    <section class="a-glass a-card" aria-labelledby="brandTitle">
        <div class="a-brand">
            <span class="a-brand-mark" aria-hidden="true">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.6" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>
            </span>
            <h1 id="brandTitle">Awards</h1>
            <p class="a-tagline">Secure Portal</p>
        </div>

        <% if (errorMsg != null) { %>
        <div class="a-alert a-alert-error" role="alert"><%= errorMsg %></div>
        <% } else if (okMsg != null) { %>
        <div class="a-alert a-alert-ok" role="status"><%= okMsg %></div>
        <% } %>

        <form action="/login" method="post">
            <div class="a-field">
                <label class="a-label" for="email">Email Address</label>
                <input class="a-input" type="email" id="email" name="email" autocomplete="username" required autofocus>
            </div>
            <div class="a-field">
                <label class="a-label" for="password">Password</label>
                <input class="a-input" type="password" id="password" name="password" autocomplete="current-password" required>
            </div>
            <button type="submit" class="a-btn a-btn-gold a-btn-block">Sign In</button>
        </form>

        <p class="a-foot">Don't have an account?<a href="/register">Register here</a></p>
    </section>
</main>

</body>
</html>
