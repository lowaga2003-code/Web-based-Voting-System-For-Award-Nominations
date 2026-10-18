<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
  [INTEGRATION] Friendly 403 page (SecurityConfig -> exceptionHandling().accessDeniedPage("/access-denied")).
  Shown when a logged-in user opens a module page their role is not allowed to use.
--%>
<%
    String roleLabel = (String) request.getAttribute("roleLabel");
    if (roleLabel == null) roleLabel = "your role";
    roleLabel = roleLabel.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Access denied · Award Nomination System</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/dashboard.css">
</head>
<body class="hub">

<header class="h-top">
    <a class="h-brand" href="/dashboard">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.2"/>
            <path d="M12 6.5L13.2 9.9L16.8 10.2L14 12.5L14.9 16L12 14.1L9.1 16L10 12.5L7.2 10.2L10.8 9.9Z" fill="currentColor"/>
        </svg>
        <span>Award Nomination System</span>
    </a>
    <a class="h-logout" href="/logout">Logout</a>
</header>

<main class="h-center">
    <section class="h-denied">
        <span class="h-icon" aria-hidden="true">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/></svg>
        </span>
        <h1>Access denied</h1>
        <p>This page belongs to another role's part of the system. You're signed in as <strong><%= roleLabel %></strong>.</p>
        <a class="h-btn" href="/dashboard">Back to dashboard</a>
    </section>
</main>

</body>
</html>
