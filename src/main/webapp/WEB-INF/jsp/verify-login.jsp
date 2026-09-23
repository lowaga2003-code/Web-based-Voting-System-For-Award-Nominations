<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Two-Factor Authentication | Award Nominations</title>
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
            text-align: center;
            letter-spacing: 0.5em;
            font-size: 1.5rem;
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
        <div class="text-center mb-8">
            <h1 class="text-3xl serif mb-3 tracking-wide">Verification Required</h1>
            <p class="text-sm text-slate-400 font-light">We've sent a 6-digit one-time password to your email. Please enter it below to securely log in.</p>
        </div>

        <form id="verifyForm" class="space-y-6">
            <div id="errorMsg" class="hidden p-3 text-sm text-red-400 border border-red-400/30 bg-red-400/10 rounded-lg text-center">
            </div>

            <div>
                <input type="text" id="otpCode" name="otpCode" class="input-glass w-full px-4 py-4 rounded-lg transition-all" maxlength="6" required autofocus placeholder="------">
            </div>

            <button type="submit" class="btn-gold w-full py-3 rounded-lg font-semibold tracking-wide mt-4">
                Verify and Log In
            </button>
        </form>

        <div class="mt-6 text-center text-sm text-slate-400">
            Didn't receive the code? <a href="/login" class="gold-accent hover:text-white transition-colors ml-1">Back to login</a>
        </div>
    </div>

    <script>
        document.getElementById('verifyForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const otpCode = document.getElementById('otpCode').value;
            const errorMsg = document.getElementById('errorMsg');
            
            try {
                const res = await fetch(`/api/auth/verify-login?otpCode=${otpCode}`, {
                    method: 'POST'
                });
                
                if (res.ok) {
                    window.location.href = '/voter/vote'; // Redirect to voter dashboard
                } else {
                    const data = await res.json();
                    errorMsg.innerText = data.message || 'Invalid OTP';
                    errorMsg.classList.remove('hidden');
                }
            } catch (err) {
                errorMsg.innerText = 'Network error. Please try again.';
                errorMsg.classList.remove('hidden');
            }
        });
    </script>
</body>
</html>
