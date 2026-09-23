<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register | Award Nominations</title>
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
<body class="min-h-screen flex items-center justify-center relative overflow-hidden py-10">
    
    <div class="absolute top-[-10%] right-[-10%] w-[500px] h-[500px] rounded-full bg-[#1e293b] opacity-50 blur-[100px]"></div>
    <div class="absolute bottom-[-10%] left-[-10%] w-[600px] h-[600px] rounded-full bg-[#332a12] opacity-40 blur-[120px]"></div>

    <div class="glass-panel p-10 rounded-2xl w-full max-w-lg relative z-10 shadow-2xl">
        <div class="text-center mb-8">
            <h1 class="text-3xl serif mb-2 tracking-wide">REQUEST ACCESS</h1>
            <p class="text-sm text-slate-400 font-light tracking-widest uppercase">Create an Account</p>
        </div>

        <form id="registerForm" class="space-y-5">
            <div id="alertBox" class="hidden p-3 text-sm rounded-lg text-center border"></div>

            <div class="grid grid-cols-2 gap-4">
                <div class="col-span-2">
                    <label for="username" class="block text-xs font-medium text-slate-300 mb-2 uppercase tracking-wider">Username</label>
                    <input type="text" id="username" class="input-glass w-full px-4 py-3 rounded-lg transition-all" required>
                </div>
                
                <div class="col-span-2">
                    <label for="email" class="block text-xs font-medium text-slate-300 mb-2 uppercase tracking-wider">Email Address</label>
                    <input type="email" id="email" class="input-glass w-full px-4 py-3 rounded-lg transition-all" required>
                </div>

                <div class="col-span-2 sm:col-span-1">
                    <label for="password" class="block text-xs font-medium text-slate-300 mb-2 uppercase tracking-wider">Password</label>
                    <input type="password" id="password" class="input-glass w-full px-4 py-3 rounded-lg transition-all" required>
                </div>

                <div class="col-span-2 sm:col-span-1">
                    <label for="role" class="block text-xs font-medium text-slate-300 mb-2 uppercase tracking-wider">Account Type</label>
                    <select id="role" class="input-glass w-full px-4 py-3 rounded-lg transition-all appearance-none cursor-pointer">
                        <option value="ROLE_VOTER" class="bg-slate-800">Voter</option>
                        <option value="ROLE_NOMINEE" class="bg-slate-800">Nominee</option>
                    </select>
                </div>
            </div>

            <div class="pt-4">
                <button type="submit" class="btn-gold w-full py-3 rounded-lg font-semibold tracking-wide">
                    Submit Registration
                </button>
            </div>
        </form>

        <div class="mt-8 text-center text-sm text-slate-400">
            Already have an account? <a href="/login" class="gold-accent hover:text-white transition-colors ml-1">Sign in here</a>
        </div>
    </div>

    <script>
        document.getElementById('registerForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const alertBox = document.getElementById('alertBox');
            
            const data = {
                username: document.getElementById('username').value,
                email: document.getElementById('email').value,
                password: document.getElementById('password').value,
                role: document.getElementById('role').value
            };

            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });
                
                const result = await response.json();
                
                alertBox.classList.remove('hidden', 'border-red-400/30', 'bg-red-400/10', 'text-red-400', 'border-emerald-400/30', 'bg-emerald-400/10', 'text-emerald-400');
                
                if(response.ok) {
                    if (result.requiresOtp) {
                        window.location.href = `/verify-registration?userId=${result.userId}`;
                    } else {
                        alertBox.classList.add('border-emerald-400/30', 'bg-emerald-400/10', 'text-emerald-400');
                        alertBox.innerText = 'Registration successful! You can now log in.';
                        document.getElementById('registerForm').reset();
                    }
                } else {
                    alertBox.classList.add('border-red-400/30', 'bg-red-400/10', 'text-red-400');
                    alertBox.innerText = result.message || 'Registration failed.';
                }
            } catch (err) {
                alertBox.classList.remove('hidden');
                alertBox.classList.add('border-red-400/30', 'bg-red-400/10', 'text-red-400');
                alertBox.innerText = 'Network error occurred.';
            }
        });
    </script>
</body>
</html>
