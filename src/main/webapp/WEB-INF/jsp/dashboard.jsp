<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard | Award Nominations</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600&family=Playfair+Display:ital,wght@0,400;0,600;1,400&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Montserrat', sans-serif; background-color: #0f172a; color: #f8fafc; }
        .glass-panel {
            background: rgba(30, 41, 59, 0.4);
            backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.05);
        }
    </style>
</head>
<body class="min-h-screen flex items-center justify-center relative overflow-hidden">
    <div class="fixed top-[-20%] left-[-10%] w-[800px] h-[800px] rounded-full bg-[#1e293b] opacity-40 blur-[150px] z-0 pointer-events-none"></div>
    <div class="fixed bottom-[-10%] right-[-10%] w-[600px] h-[600px] rounded-full bg-[#332a12] opacity-30 blur-[150px] z-0 pointer-events-none"></div>

    <div class="glass-panel p-10 rounded-2xl relative z-10 text-center shadow-xl border border-slate-700">
        <h1 class="text-3xl mb-4 font-[Playfair_Display]">Welcome to your Dashboard</h1>
        <p class="text-slate-400 mb-6 font-light">This module is under construction.</p>
        <a href="/logout" class="text-[#d4af37] border border-[#d4af37] px-6 py-2 rounded hover:bg-[#d4af37]/10 transition">Logout</a>
    </div>
</body>
</html>
