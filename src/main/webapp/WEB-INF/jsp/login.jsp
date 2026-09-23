<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login | Award Nominations</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600&family=Playfair+Display:ital,wght@0,400;0,600;1,400&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Montserrat', sans-serif; background-color: #0f172a; color: #f8fafc; }
        h1, h2, h3, .serif { font-family: 'Playfair Display', serif; }
        .glass-panel {
            background: rgba(30, 41, 59, 0.7);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.1);
        }
        .gold-accent { color: #d4af37; }
        .bg-gold-accent { background-color: #d4af37; }
        .btn-gold {
            background: linear-gradient(135deg, #d4af37 0%, #aa8c2c 100%);
            color: #0f172a;
            transition: all 0.3s ease;
        }
        .btn-gold:hover {
            box-shadow: 0 4px 15px rgba(212, 175, 55, 0.4);
            transform: translateY(-1px);
        }
        .input-glass {
            background: rgba(15, 23, 42, 0.5);
            border: 1px solid rgba(255, 255, 255, 0.15);
            color: white;
        }
        .input-glass:focus {
            outline: none;
            border-color: #d4af37;
            box-shadow: 0 0 0 1px #d4af37;
        }
    </style>
</head>
<body class="min-h-screen flex items-center justify-center relative overflow-hidden">
    
    <!-- Abstract subtle background element -->
    <div class="absolute top-[-10%] left-[-10%] w-[500px] h-[500px] rounded-full bg-[#1e293b] opacity-50 blur-[100px]"></div>
    <div class="absolute bottom-[-10%] right-[-10%] w-[600px] h-[600px] rounded-full bg-[#332a12] opacity-40 blur-[120px]"></div>

    <div class="glass-panel p-10 rounded-2xl w-full max-w-md relative z-10 shadow-2xl">
        <div class="text-center mb-10">
            <h1 class="text-4xl serif mb-2 tracking-wide">AWARDS</h1>
            <p class="text-sm text-slate-400 font-light tracking-widest uppercase">Secure Portal</p>
        </div>

        <form action="/login" method="post" class="space-y-6">
            <c:if test="${not empty param.error}">
                <div class="p-3 text-sm text-red-400 border border-red-400/30 bg-red-400/10 rounded-lg text-center">
                    Invalid username or password.
                </div>
            </c:if>
            <c:if test="${not empty param.logout}">
                <div class="p-3 text-sm text-emerald-400 border border-emerald-400/30 bg-emerald-400/10 rounded-lg text-center">
                    You have been logged out.
                </div>
            </c:if>

            <div>
                <label for="email" class="block text-xs font-medium text-slate-300 mb-2 uppercase tracking-wider">Email Address</label>
                <input type="email" id="email" name="email" class="input-glass w-full px-4 py-3 rounded-lg transition-all" required autofocus>
            </div>
            
            <div>
                <label for="password" class="block text-xs font-medium text-slate-300 mb-2 uppercase tracking-wider">Password</label>
                <input type="password" id="password" name="password" class="input-glass w-full px-4 py-3 rounded-lg transition-all" required>
            </div>

            <div class="flex items-center justify-between text-sm mt-2">
                <a href="#" class="text-slate-400 hover:text-white transition-colors">Forgot password?</a>
            </div>

            <button type="submit" class="btn-gold w-full py-3 rounded-lg font-semibold tracking-wide mt-4">
                Sign In
            </button>
        </form>



        <div class="mt-6 text-center text-sm text-slate-400">
            Don't have an account? <a href="/register" class="gold-accent hover:text-white transition-colors ml-1">Register here</a>
        </div>
    </div>


</body>
</html>
