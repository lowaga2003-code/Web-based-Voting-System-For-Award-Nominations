<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard | User Management</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600&family=Playfair+Display:ital,wght@0,400;0,600;1,400&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Montserrat', sans-serif; background-color: #0f172a; color: #f8fafc; }
        h1, h2, h3, .serif { font-family: 'Playfair Display', serif; }
        .glass-panel {
            background: rgba(30, 41, 59, 0.4);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.05);
        }
        .gold-accent { color: #d4af37; }
        .bg-gold-accent { background-color: #d4af37; }
        .btn-outline-gold {
            border: 1px solid #d4af37;
            color: #d4af37;
            transition: all 0.3s ease;
        }
        .btn-outline-gold:hover {
            background: rgba(212, 175, 55, 0.1);
        }
        .btn-gold {
            background: linear-gradient(135deg, #d4af37 0%, #aa8c2c 100%);
            color: #0f172a;
            transition: all 0.3s ease;
        }
        .btn-gold:hover {
            box-shadow: 0 4px 15px rgba(212, 175, 55, 0.4);
            transform: translateY(-1px);
        }
        table { border-collapse: separate; border-spacing: 0 8px; }
        tr.glass-row {
            background: rgba(15, 23, 42, 0.6);
            border: 1px solid rgba(255, 255, 255, 0.05);
            transition: all 0.2s;
        }
        tr.glass-row:hover { background: rgba(30, 41, 59, 0.8); }
        td:first-child { border-top-left-radius: 8px; border-bottom-left-radius: 8px; }
        td:last-child { border-top-right-radius: 8px; border-bottom-right-radius: 8px; }
        
        .input-glass {
            background: rgba(15, 23, 42, 0.5);
            border: 1px solid rgba(255, 255, 255, 0.15);
            color: white;
        }
        .input-glass:focus { outline: none; border-color: #d4af37; box-shadow: 0 0 0 1px #d4af37; }

        /* Custom scrollbar for main content */
        ::-webkit-scrollbar { width: 8px; }
        ::-webkit-scrollbar-track { background: rgba(15, 23, 42, 0.5); }
        ::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); border-radius: 4px; }
        ::-webkit-scrollbar-thumb:hover { background: rgba(212, 175, 55, 0.5); }
    </style>
</head>
<body class="min-h-screen flex overflow-hidden">
    
    <div class="fixed top-[-20%] left-[-10%] w-[800px] h-[800px] rounded-full bg-[#1e293b] opacity-40 blur-[150px] z-0 pointer-events-none"></div>
    <div class="fixed bottom-[-10%] right-[-10%] w-[600px] h-[600px] rounded-full bg-[#332a12] opacity-30 blur-[150px] z-0 pointer-events-none"></div>

    <!-- LEFT SIDEBAR NAVIGATION -->
    <aside class="glass-panel w-72 h-screen flex flex-col border-r border-slate-700/50 relative z-50 flex-shrink-0">
        <div class="p-8 border-b border-slate-700/50 text-center">
            <h1 class="text-3xl serif tracking-widest uppercase">Admin</h1>
            <h1 class="text-lg serif tracking-widest uppercase gold-accent mt-1">Portal</h1>
        </div>
        
        <nav class="flex-1 px-4 py-8 space-y-1 overflow-y-auto">
            <p class="text-[10px] font-semibold text-slate-500 uppercase tracking-widest mb-3 px-4">Core Modules</p>
            
            <a href="#" class="flex items-center gap-3 px-4 py-2.5 bg-slate-800/60 text-[#d4af37] border-l-2 border-[#d4af37] rounded-r-lg transition text-sm">
                <svg class="w-4 h-4 opacity-80" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
                <span class="font-medium">User & Role</span>
            </a>
            
            <a href="#" class="flex items-center gap-3 px-4 py-2.5 text-slate-400 hover:text-white hover:bg-slate-800/30 rounded-lg transition border-l-2 border-transparent text-sm">
                <svg class="w-4 h-4 opacity-80" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"></path></svg>
                <span class="font-medium">Award Category</span>
            </a>

            <a href="/nominee/list" class="flex items-center gap-3 px-4 py-2.5 text-slate-400 hover:text-white hover:bg-slate-800/30 rounded-lg transition border-l-2 border-transparent text-sm">
                <svg class="w-4 h-4 opacity-80" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                <span class="font-medium">Nominee Profile</span>
            </a>

            <a href="#" class="flex items-center gap-3 px-4 py-2.5 text-slate-400 hover:text-white hover:bg-slate-800/30 rounded-lg transition border-l-2 border-transparent text-sm">
                <svg class="w-4 h-4 opacity-80" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                <span class="font-medium">Voting</span>
            </a>

            <a href="#" class="flex items-center gap-3 px-4 py-2.5 text-slate-400 hover:text-white hover:bg-slate-800/30 rounded-lg transition border-l-2 border-transparent text-sm">
                <svg class="w-4 h-4 opacity-80" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path></svg>
                <span class="font-medium">Audit & Dispute</span>
            </a>

            <a href="#" class="flex items-center gap-3 px-4 py-2.5 text-slate-400 hover:text-white hover:bg-slate-800/30 rounded-lg transition border-l-2 border-transparent text-sm">
                <svg class="w-4 h-4 opacity-80" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"></path></svg>
                <span class="font-medium">Evaluation & Selection</span>
            </a>

            <a href="#" class="flex items-center gap-3 px-4 py-2.5 text-slate-400 hover:text-white hover:bg-slate-800/30 rounded-lg transition border-l-2 border-transparent text-sm">
                <svg class="w-4 h-4 opacity-80" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>
                <span class="font-medium">Policy & Consent</span>
            </a>
        </nav>
        
        <div class="p-6 border-t border-slate-700/50">
            <a href="/logout" class="btn-outline-gold flex items-center justify-center gap-3 w-full py-3 rounded-lg text-xs font-semibold tracking-wider uppercase">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path></svg>
                Logout
            </a>
        </div>
    </aside>

    <!-- MAIN CONTENT AREA -->
    <main class="flex-1 h-screen overflow-y-auto relative z-10">
        <div class="max-w-6xl mx-auto px-10 py-12">
            <div class="flex justify-between items-end mb-10">
                <div>
                    <h2 class="text-4xl serif mb-3">User Management</h2>
                    <p class="text-slate-400 font-light">View, modify, and manage system access levels.</p>
                </div>
                <div class="flex gap-4">
                    <button onclick="document.getElementById('addUserModal').classList.remove('hidden')" class="btn-gold px-5 py-2.5 rounded-lg text-sm font-semibold shadow-lg flex items-center gap-2">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
                        Add User
                    </button>
                    <button onclick="fetchUsers()" class="text-sm btn-outline-gold px-5 py-2.5 rounded-lg flex items-center gap-2 font-medium">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>
                        Refresh
                    </button>
                </div>
            </div>

            <div class="glass-panel rounded-2xl p-8 shadow-xl">
                <div class="overflow-x-auto">
                    <table class="w-full text-left border-collapse min-w-[800px]">
                        <thead>
                            <tr class="text-xs uppercase tracking-wider text-slate-400 border-b border-slate-700/50">
                                <th class="pb-4 px-4 font-medium w-16">ID</th>
                                <th class="pb-4 px-4 font-medium">Username</th>
                                <th class="pb-4 px-4 font-medium">Email</th>
                                <th class="pb-4 px-4 font-medium">Role</th>
                                <th class="pb-4 px-4 font-medium">Status</th>
                                <th class="pb-4 px-4 font-medium text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody id="userTableBody" class="text-sm font-light">
                            <!-- Populated by JS -->
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </main>

    <!-- Add User Modal -->
    <div id="addUserModal" class="hidden fixed inset-0 z-[100] bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
        <div class="glass-panel w-full max-w-md rounded-2xl p-8 border border-slate-600 relative">
            <button onclick="document.getElementById('addUserModal').classList.add('hidden')" class="absolute top-4 right-4 text-slate-400 hover:text-white transition">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
            </button>
            <h3 class="text-2xl serif mb-6">Add New User</h3>
            <form id="addUserForm" class="space-y-4">
                <div>
                    <label class="block text-xs text-slate-400 mb-1">Username</label>
                    <input type="text" id="addUsername" class="input-glass w-full px-4 py-3 rounded-lg" required>
                </div>
                <div>
                    <label class="block text-xs text-slate-400 mb-1">Email</label>
                    <input type="email" id="addEmail" class="input-glass w-full px-4 py-3 rounded-lg" required>
                </div>
                <div>
                    <label class="block text-xs text-slate-400 mb-1">Password</label>
                    <input type="password" id="addPassword" class="input-glass w-full px-4 py-3 rounded-lg" required>
                </div>
                <div>
                    <label class="block text-xs text-slate-400 mb-1">Initial Role</label>
                    <select id="addRole" class="input-glass w-full px-4 py-3 rounded-lg">
                        <option value="ROLE_VOTER" class="bg-slate-800">Voter</option>
                        <option value="ROLE_NOMINEE" class="bg-slate-800">Nominee</option>
                        <option value="ROLE_SYSTEM_ADMINISTRATOR" class="bg-slate-800">System Administrator</option>
                        <option value="ROLE_PROGRAM_MANAGER" class="bg-slate-800">Program Manager</option>
                        <option value="ROLE_COMPLIANCE_AUDITOR" class="bg-slate-800">Compliance Auditor</option>
                        <option value="ROLE_SELECTION_COMMITTEE_CHAIRPERSON" class="bg-slate-800">Selection Committee Chairperson</option>
                        <option value="ROLE_LEGAL_GOVERNANCE_OFFICER" class="bg-slate-800">Legal & Governance Officer</option>
                    </select>
                </div>
                <button type="submit" class="btn-gold w-full py-3.5 rounded-lg mt-6 font-semibold tracking-wide">Create User</button>
            </form>
        </div>
    </div>

    <!-- Edit User Modal -->
    <div id="editUserModal" class="hidden fixed inset-0 z-[100] bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
        <div class="glass-panel w-full max-w-md rounded-2xl p-8 border border-slate-600 relative">
            <button onclick="document.getElementById('editUserModal').classList.add('hidden')" class="absolute top-4 right-4 text-slate-400 hover:text-white transition">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
            </button>
            <h3 class="text-2xl serif mb-6">Edit User Details</h3>
            <form id="editUserForm" class="space-y-4">
                <input type="hidden" id="editUserId">
                <div>
                    <label class="block text-xs text-slate-400 mb-1">Username</label>
                    <input type="text" id="editUsername" class="input-glass w-full px-4 py-3 rounded-lg" required>
                </div>
                <div>
                    <label class="block text-xs text-slate-400 mb-1">Email</label>
                    <input type="email" id="editEmail" class="input-glass w-full px-4 py-3 rounded-lg" required>
                </div>
                <div>
                    <label class="block text-xs text-slate-400 mb-1">New Password (leave blank to keep current)</label>
                    <input type="password" id="editPassword" class="input-glass w-full px-4 py-3 rounded-lg">
                </div>
                <button type="submit" class="btn-gold w-full py-3.5 rounded-lg mt-6 font-semibold tracking-wide">Save Changes</button>
            </form>
        </div>
    </div>

    <!-- Confirmation Modal -->
    <div id="confirmModal" class="hidden fixed inset-0 z-[200] bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
        <div class="glass-panel w-full max-w-sm rounded-2xl p-6 border border-slate-600 relative text-center shadow-2xl">
            <h3 id="confirmTitle" class="text-xl serif mb-2 text-white">Confirm Action</h3>
            <p id="confirmMessage" class="text-sm text-slate-400 mb-8 font-light">Are you sure you want to proceed?</p>
            <div class="flex gap-3 justify-center">
                <button onclick="closeConfirmModal()" class="px-5 py-2.5 rounded-lg text-sm bg-slate-800 text-slate-300 hover:text-white hover:bg-slate-700 transition font-medium">Cancel</button>
                <button id="confirmBtn" class="px-5 py-2.5 rounded-lg text-sm font-semibold transition">Confirm</button>
            </div>
        </div>
    </div>

    <script>
        const ROLES = [
            {val: 'ROLE_VOTER', text: 'Voter'},
            {val: 'ROLE_NOMINEE', text: 'Nominee'},
            {val: 'ROLE_SYSTEM_ADMINISTRATOR', text: 'System Admin'},
            {val: 'ROLE_PROGRAM_MANAGER', text: 'Program Manager'},
            {val: 'ROLE_COMPLIANCE_AUDITOR', text: 'Compliance Auditor'},
            {val: 'ROLE_SELECTION_COMMITTEE_CHAIRPERSON', text: 'Selection Chair'},
            {val: 'ROLE_LEGAL_GOVERNANCE_OFFICER', text: 'Legal Officer'}
        ];

        let pendingAction = null;
        let cancelAction = null;
        const currentUserEmail = '${currentUserEmail}';

        function showConfirmModal(title, message, btnText, btnClass, onConfirm, onCancel) {
            document.getElementById('confirmTitle').innerText = title;
            document.getElementById('confirmMessage').innerText = message;
            
            const btn = document.getElementById('confirmBtn');
            btn.innerText = btnText;
            btn.className = `px-5 py-2.5 rounded-lg text-sm font-semibold transition \${btnClass}`;
            
            pendingAction = onConfirm;
            cancelAction = onCancel;
            document.getElementById('confirmModal').classList.remove('hidden');
        }

        function closeConfirmModal() {
            if(cancelAction) cancelAction();
            document.getElementById('confirmModal').classList.add('hidden');
            pendingAction = null;
            cancelAction = null;
        }

        document.getElementById('confirmBtn').addEventListener('click', () => {
            if(pendingAction) pendingAction();
            cancelAction = null; // Don't trigger cancel action if confirmed
            closeConfirmModal();
        });

        async function fetchUsers() {
            const tbody = document.getElementById('userTableBody');
            tbody.innerHTML = '<tr><td colspan="6" class="text-center py-10 text-slate-500">Loading...</td></tr>';
            
            try {
                const response = await fetch('/api/admin/users');
                if(response.ok) {
                    const users = await response.json();
                    tbody.innerHTML = '';
                    users.forEach(user => {
                        const rolesStr = user.roles.map(r => ROLES.find(x => x.val === r)?.text || r).join(', ');
                        const currentRole = user.roles[0]; // Assuming 1 role per user in UI for simplicity
                        
                        const statusBadge = user.active 
                            ? '<span class="px-2 py-1 bg-emerald-900/30 text-emerald-400 border border-emerald-800/50 rounded text-xs font-medium">Active</span>'
                            : '<span class="px-2 py-1 bg-red-900/30 text-red-400 border border-red-800/50 rounded text-xs font-medium">Suspended</span>';
                        
                        let roleOptions = '<option value="" class="bg-slate-800">Change Role...</option>';
                        ROLES.forEach(r => {
                            roleOptions += `<option value="\${r.val}" class="bg-slate-800" \${currentRole === r.val ? 'disabled' : ''}>\${r.text}</option>`;
                        });

                        const isCurrentUser = user.email === currentUserEmail;
                        const disabledAttr = isCurrentUser ? 'disabled' : '';
                        const opacityClass = isCurrentUser ? 'opacity-50 cursor-not-allowed' : '';

                        const toggleAction = user.active
                            ? `<button \${disabledAttr} onclick="toggleUser(\${user.id}, 'deactivate', '\${user.username}')" class="px-3 py-1.5 bg-slate-800 hover:bg-red-900/50 border border-slate-700 text-slate-300 hover:text-red-400 rounded text-xs transition font-medium shadow-sm \${opacityClass}">Suspend</button>`
                            : `<button \${disabledAttr} onclick="toggleUser(\${user.id}, 'reactivate', '\${user.username}')" class="px-3 py-1.5 bg-slate-800 hover:bg-emerald-900/50 border border-slate-700 text-slate-300 hover:text-emerald-400 rounded text-xs transition font-medium shadow-sm \${opacityClass}">Reactivate</button>`;

                        tbody.innerHTML += `
                            <tr class="glass-row">
                                <td class="py-5 px-4 text-slate-400 font-medium">#\${user.id}</td>
                                <td class="py-5 px-4 font-semibold tracking-wide text-white">\${user.username} \${isCurrentUser ? '<span class="ml-2 px-2 py-0.5 bg-[#d4af37]/20 text-[#d4af37] text-[10px] rounded uppercase tracking-wider">You</span>' : ''}</td>
                                <td class="py-5 px-4 text-slate-400">\${user.email}</td>
                                <td class="py-5 px-4 text-slate-300 text-sm font-medium">\${rolesStr}</td>
                                <td class="py-5 px-4">\${statusBadge}</td>
                                <td class="py-5 px-4 text-right">
                                    <div class="flex justify-end gap-2 items-center">
                                        <select \${disabledAttr} onchange="updateRole(\${user.id}, this.value, '\${user.username}')" class="bg-slate-800 border border-slate-700 text-xs rounded px-2 py-1.5 outline-none focus:border-[#d4af37] w-36 cursor-pointer text-slate-300 shadow-sm \${opacityClass}">
                                            \${roleOptions}
                                        </select>
                                        <button \${disabledAttr} onclick="openEditModal(\${user.id}, '\${user.username}', '\${user.email}')" class="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-300 hover:text-white rounded text-xs transition font-medium shadow-sm \${opacityClass}">Edit</button>
                                        \${toggleAction}
                                        <button \${disabledAttr} onclick="deleteUser(\${user.id}, '\${user.username}')" class="px-3 py-1.5 bg-slate-800 hover:bg-red-600 border border-slate-700 hover:border-red-500 text-slate-300 hover:text-white rounded text-xs transition font-medium shadow-sm \${opacityClass}">Delete</button>
                                    </div>
                                </td>
                            </tr>
                        `;
                    });
                } else {
                    tbody.innerHTML = '<tr><td colspan="6" class="text-center py-10 text-red-400">Unauthorized or failed to fetch.</td></tr>';
                }
            } catch (e) {
                tbody.innerHTML = '<tr><td colspan="6" class="text-center py-10 text-red-400">Network Error.</td></tr>';
            }
        }

        function openEditModal(id, username, email) {
            document.getElementById('editUserId').value = id;
            document.getElementById('editUsername').value = username;
            document.getElementById('editEmail').value = email;
            document.getElementById('editPassword').value = '';
            document.getElementById('editUserModal').classList.remove('hidden');
        }

        function updateRole(userId, newRole, username) {
            if(!newRole) return;
            const roleName = ROLES.find(r => r.val === newRole)?.text || newRole;
            
            showConfirmModal(
                'Change Role',
                `Are you sure you want to change \${username}'s role to \${roleName}?`,
                'Change Role',
                'bg-[#d4af37] text-slate-900 hover:bg-[#aa8c2c]',
                async () => {
                    try {
                        await fetch(`/api/admin/users/\${userId}/role?role=\${newRole}`, { method: 'PUT' });
                        fetchUsers();
                    } catch (e) { alert('Failed to update role'); }
                },
                () => fetchUsers() // Reset select on cancel
            );
        }

        function toggleUser(userId, action, username) {
            const isSuspend = action === 'deactivate';
            const title = isSuspend ? 'Suspend User' : 'Reactivate User';
            const msg = isSuspend ? `Are you sure you want to suspend \${username}? They will not be able to log in.` : `Are you sure you want to reactivate \${username}?`;
            const btnColor = isSuspend ? 'bg-red-600 text-white hover:bg-red-500' : 'bg-emerald-600 text-white hover:bg-emerald-500';
            
            showConfirmModal(title, msg, isSuspend ? 'Suspend' : 'Reactivate', btnColor, async () => {
                try {
                    await fetch(`/api/admin/users/\${userId}/\${action}`, { method: 'PUT' });
                    fetchUsers();
                } catch (e) { alert(`Failed to \${action} user`); }
            });
        }

        function deleteUser(userId, username) {
            showConfirmModal(
                'Delete User',
                `WARNING: Are you absolutely sure you want to permanently DELETE \${username}? This action cannot be undone.`,
                'Delete Permanently',
                'bg-red-600 text-white hover:bg-red-500',
                async () => {
                    try {
                        await fetch(`/api/admin/users/\${userId}`, { method: 'DELETE' });
                        fetchUsers();
                    } catch (e) { alert('Failed to delete user'); }
                }
            );
        }

        document.getElementById('addUserForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = {
                username: document.getElementById('addUsername').value,
                email: document.getElementById('addEmail').value,
                password: document.getElementById('addPassword').value,
                role: document.getElementById('addRole').value
            };
            try {
                const res = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });
                if(res.ok) {
                    document.getElementById('addUserModal').classList.add('hidden');
                    document.getElementById('addUserForm').reset();
                    fetchUsers();
                } else {
                    const result = await res.json();
                    alert(result.message || 'Failed to create user');
                }
            } catch(e) { alert('Network Error'); }
        });

        document.getElementById('editUserForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = document.getElementById('editUserId').value;
            const data = {
                username: document.getElementById('editUsername').value,
                email: document.getElementById('editEmail').value,
                password: document.getElementById('editPassword').value
            };
            try {
                const res = await fetch(`/api/admin/users/\${id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });
                if(res.ok) {
                    document.getElementById('editUserModal').classList.add('hidden');
                    document.getElementById('editUserForm').reset();
                    fetchUsers();
                } else {
                    const result = await res.json();
                    alert(result.message || 'Failed to update user details');
                }
            } catch(e) { alert('Network Error'); }
        });

        // Fetch on load
        document.addEventListener('DOMContentLoaded', fetchUsers);
    </script>
</body>
</html>
