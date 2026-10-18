<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
  MODULE 1 (admin member) - User & Role Management (System Administrator only).
  [INTEGRATION] Redesigned in the admin member's own style; same features as his original page:
  list users, add user, edit details, change role, suspend / reactivate, delete, confirmation dialogs.
  Data comes from his REST API: /api/admin/users (AdminUserController) and /api/auth/register.
--%>
<%!
    private static String escAttr(Object value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : String.valueOf(value).toCharArray()) {
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
%>
<%
    String currentUserEmail = (String) request.getAttribute("currentUserEmail");
    if (currentUserEmail == null) currentUserEmail = "";
    String avatarLetter = currentUserEmail.isEmpty() ? "A" : currentUserEmail.substring(0, 1).toUpperCase();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard | User Management</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:wght@500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/css/admin-module.css">
</head>
<body class="admin-ui" data-current-email="<%= escAttr(currentUserEmail) %>">

<div class="a-blob a-blob-1" aria-hidden="true"></div>
<div class="a-blob a-blob-2" aria-hidden="true"></div>

<div class="a-shell">

    <!-- LEFT SIDEBAR -->
    <aside class="a-sidebar">
        <div class="a-side-brand">
            <div class="t1">Admin</div>
            <div class="t2">Portal</div>
        </div>

        <nav class="a-nav" aria-label="Admin navigation">
            <p class="a-nav-label">System</p>
            <a class="a-nav-link" href="/dashboard">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"/></svg>
                Main Dashboard
            </a>
            <p class="a-nav-label">Module 1</p>
            <a class="a-nav-link is-active" href="/admin/users" aria-current="page">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>
                User &amp; Role
            </a>
        </nav>

        <div class="a-side-foot">
            <div class="a-whoami">
                <span class="a-avatar" aria-hidden="true"><%= escAttr(avatarLetter) %></span>
                <div class="a-whoami-text">
                    <strong>System Administrator</strong>
                    <span title="<%= escAttr(currentUserEmail) %>"><%= escAttr(currentUserEmail) %></span>
                </div>
            </div>
            <a href="/logout" class="a-btn a-btn-outline a-btn-block">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/></svg>
                Logout
            </a>
        </div>
    </aside>

    <!-- MAIN CONTENT -->
    <main class="a-main">
        <div class="a-container">
            <header class="a-page-head">
                <div>
                    <p class="a-crumb">Module 1 · User Account &amp; Role Management</p>
                    <h1>User Management</h1>
                    <p class="sub">View, modify, and manage system access levels.</p>
                </div>
                <div class="a-head-actions">
                    <button type="button" class="a-btn a-btn-gold" id="openAddBtn">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
                        Add User
                    </button>
                    <button type="button" class="a-btn a-btn-outline" id="refreshBtn">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
                        Refresh
                    </button>
                </div>
            </header>

            <section class="a-stats" aria-label="User statistics">
                <div class="a-glass a-stat"><span class="label">Total users</span><span class="value gold" id="statTotal">–</span></div>
                <div class="a-glass a-stat"><span class="label">Active</span><span class="value ok" id="statActive">–</span></div>
                <div class="a-glass a-stat"><span class="label">Suspended</span><span class="value bad" id="statSuspended">–</span></div>
                <div class="a-glass a-stat"><span class="label">Voters who voted</span><span class="value" id="statVoted">–</span></div>
            </section>

            <section class="a-glass a-panel" aria-label="Users">
                <div class="a-toolbar">
                    <div class="a-search">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
                        <input class="a-input" type="search" id="searchInput" placeholder="Search by username or email" aria-label="Search users">
                    </div>
                    <select class="a-select" id="roleFilter" aria-label="Filter by role">
                        <option value="">All roles</option>
                    </select>
                    <span class="a-count" id="countLabel"></span>
                </div>

                <div class="a-table-wrap">
                    <table class="a-table">
                        <thead>
                        <tr>
                            <th scope="col">ID</th>
                            <th scope="col">Username</th>
                            <th scope="col">Email</th>
                            <th scope="col">Role</th>
                            <th scope="col">Status</th>
                            <th scope="col" class="right">Actions</th>
                        </tr>
                        </thead>
                        <tbody id="userTableBody">
                        <tr class="empty"><td colspan="6">Loading…</td></tr>
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    </main>
</div>

<!-- Add User Modal -->
<div class="a-modal" id="addUserModal" role="dialog" aria-modal="true" aria-labelledby="addTitle" hidden>
    <div class="a-glass a-dialog">
        <button type="button" class="a-dialog-close" data-close="addUserModal" aria-label="Close">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
        </button>
        <h2 id="addTitle">Add New User</h2>
        <div class="a-alert a-alert-error" id="addError" role="alert" hidden></div>
        <form id="addUserForm" novalidate>
            <div class="a-field">
                <label class="a-label" for="addUsername">Username</label>
                <input class="a-input" type="text" id="addUsername" minlength="3" required>
            </div>
            <div class="a-field">
                <label class="a-label" for="addEmail">Email</label>
                <input class="a-input" type="email" id="addEmail" required>
            </div>
            <div class="a-field">
                <label class="a-label" for="addPassword">Password</label>
                <input class="a-input" type="password" id="addPassword" autocomplete="new-password" minlength="6" required>
            </div>
            <div class="a-field">
                <label class="a-label" for="addRole">Initial Role</label>
                <select class="a-select" id="addRole"></select>
            </div>
            <button type="submit" class="a-btn a-btn-gold a-btn-block" id="addSubmit">Create User</button>
        </form>
    </div>
</div>

<!-- Edit User Modal -->
<div class="a-modal" id="editUserModal" role="dialog" aria-modal="true" aria-labelledby="editTitle" hidden>
    <div class="a-glass a-dialog">
        <button type="button" class="a-dialog-close" data-close="editUserModal" aria-label="Close">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
        </button>
        <h2 id="editTitle">Edit User Details</h2>
        <div class="a-alert a-alert-error" id="editError" role="alert" hidden></div>
        <form id="editUserForm" novalidate>
            <input type="hidden" id="editUserId">
            <div class="a-field">
                <label class="a-label" for="editUsername">Username</label>
                <input class="a-input" type="text" id="editUsername" minlength="3" required>
            </div>
            <div class="a-field">
                <label class="a-label" for="editEmail">Email</label>
                <input class="a-input" type="email" id="editEmail" required>
            </div>
            <div class="a-field">
                <label class="a-label" for="editPassword">New Password</label>
                <input class="a-input" type="password" id="editPassword" autocomplete="new-password" placeholder="Leave blank to keep current">
            </div>
            <button type="submit" class="a-btn a-btn-gold a-btn-block" id="editSubmit">Save Changes</button>
        </form>
    </div>
</div>

<!-- Confirmation Modal -->
<div class="a-modal" id="confirmModal" role="alertdialog" aria-modal="true" aria-labelledby="confirmTitle" aria-describedby="confirmMessage" hidden>
    <div class="a-glass a-dialog small">
        <h2 id="confirmTitle">Confirm Action</h2>
        <p class="msg" id="confirmMessage">Are you sure you want to proceed?</p>
        <div class="a-dialog-actions">
            <button type="button" class="a-btn a-btn-quiet" id="confirmCancel">Cancel</button>
            <button type="button" class="a-btn a-btn-gold" id="confirmOk">Confirm</button>
        </div>
    </div>
</div>

<div class="a-toast" id="toast" role="status" aria-live="polite"></div>

<script>
    (function () {
        var ROLES = [
            { val: 'ROLE_VOTER', text: 'Voter' },
            { val: 'ROLE_NOMINEE', text: 'Nominee' },
            { val: 'ROLE_SYSTEM_ADMINISTRATOR', text: 'System Admin' },
            { val: 'ROLE_PROGRAM_MANAGER', text: 'Program Manager' },
            { val: 'ROLE_COMPLIANCE_AUDITOR', text: 'Compliance Auditor' },
            { val: 'ROLE_SELECTION_COMMITTEE_CHAIRPERSON', text: 'Selection Chair' },
            { val: 'ROLE_LEGAL_GOVERNANCE_OFFICER', text: 'Legal Officer' }
        ];
        var currentUserEmail = document.body.getAttribute('data-current-email') || '';
        var users = [];

        var tbody = document.getElementById('userTableBody');
        var searchInput = document.getElementById('searchInput');
        var roleFilter = document.getElementById('roleFilter');
        var countLabel = document.getElementById('countLabel');
        var toastEl = document.getElementById('toast');
        var toastTimer = null;
        var lastFocus = null;

        function roleText(val) {
            for (var i = 0; i < ROLES.length; i++) {
                if (ROLES[i].val === val) return ROLES[i].text;
            }
            return val;
        }

        function make(tag, className, text) {
            var node = document.createElement(tag);
            if (className) node.className = className;
            if (text !== undefined && text !== null) node.textContent = text;
            return node;
        }

        // fill the role <select>s once
        ROLES.forEach(function (r) {
            var filterOpt = make('option', null, r.text);
            filterOpt.value = r.val;
            roleFilter.appendChild(filterOpt);
            var addOpt = make('option', null, r.text);
            addOpt.value = r.val;
            document.getElementById('addRole').appendChild(addOpt);
        });

        // ---------- REST helper (returns {ok, data}) ----------
        function api(method, url, body) {
            var options = { method: method, headers: { 'Accept': 'application/json' } };
            if (body !== undefined) {
                options.headers['Content-Type'] = 'application/json';
                options.body = JSON.stringify(body);
            }
            return fetch(url, options).then(function (res) {
                if (res.redirected && /\/login(\?|$)/.test(res.url)) {
                    window.location.href = '/login'; // session expired -> Spring Security sent the login page
                    return { ok: false, data: { message: 'Session expired. Please log in again.' } };
                }
                return res.json()
                    .catch(function () { return {}; })
                    .then(function (data) { return { ok: res.ok, data: data || {} }; });
            });
        }

        // ---------- toast ----------
        function toast(message, kind, link) {
            clearTimeout(toastTimer);
            toastEl.textContent = '';
            toastEl.className = 'a-toast ' + (kind === 'bad' ? 'is-bad' : 'is-ok');
            var text = make('span', null, String(message || '').replace(/^Error:\s*/, ''));
            if (link) {
                text.appendChild(document.createTextNode(' '));
                var a = make('a', null, link.text);
                a.href = link.href;
                text.appendChild(a);
            }
            toastEl.appendChild(text);
            void toastEl.offsetWidth;
            toastEl.classList.add('show');
            toastTimer = setTimeout(function () { toastEl.classList.remove('show'); }, link ? 9000 : 4500);
        }

        // ---------- modals ----------
        function openModal(id, focusId) {
            lastFocus = document.activeElement;
            var modal = document.getElementById(id);
            modal.hidden = false;
            var target = focusId ? document.getElementById(focusId) : modal.querySelector('input, select, button');
            if (target) target.focus();
        }
        function closeModal(id) {
            document.getElementById(id).hidden = true;
            if (lastFocus && document.body.contains(lastFocus)) lastFocus.focus();
        }
        Array.prototype.forEach.call(document.querySelectorAll('[data-close]'), function (btn) {
            btn.addEventListener('click', function () { closeModal(btn.getAttribute('data-close')); });
        });
        Array.prototype.forEach.call(document.querySelectorAll('.a-modal'), function (modal) {
            modal.addEventListener('mousedown', function (e) {
                if (e.target === modal) {
                    if (modal.id === 'confirmModal') { cancelConfirm(); } else { closeModal(modal.id); }
                }
            });
        });
        document.addEventListener('keydown', function (e) {
            if (e.key !== 'Escape') return;
            if (!document.getElementById('confirmModal').hidden) { cancelConfirm(); return; }
            ['addUserModal', 'editUserModal'].forEach(function (id) {
                if (!document.getElementById(id).hidden) closeModal(id);
            });
        });

        // ---------- confirmation dialog ----------
        var pendingConfirm = null;
        var pendingCancel = null;
        var confirmOk = document.getElementById('confirmOk');

        function askConfirm(title, message, okText, okClass, onConfirm, onCancel) {
            document.getElementById('confirmTitle').textContent = title;
            document.getElementById('confirmMessage').textContent = message;
            confirmOk.textContent = okText;
            confirmOk.className = 'a-btn ' + okClass;
            pendingConfirm = onConfirm;
            pendingCancel = onCancel || null;
            openModal('confirmModal', 'confirmCancel');
        }
        function cancelConfirm() {
            var onCancel = pendingCancel;
            pendingConfirm = null;
            pendingCancel = null;
            closeModal('confirmModal');
            if (onCancel) onCancel();
        }
        confirmOk.addEventListener('click', function () {
            var onConfirm = pendingConfirm;
            pendingConfirm = null;
            pendingCancel = null;
            closeModal('confirmModal');
            if (onConfirm) onConfirm();
        });
        document.getElementById('confirmCancel').addEventListener('click', cancelConfirm);

        function afterAction(result, okMessage) {
            if (result.ok) {
                toast(result.data.message || okMessage, 'ok');
            } else {
                toast(result.data.message || 'The action failed. Please try again.', 'bad');
            }
            loadUsers();
        }

        // ---------- table ----------
        function setTableMessage(message) {
            tbody.textContent = '';
            var tr = make('tr', 'empty');
            var td = make('td', null, message);
            td.colSpan = 6;
            tr.appendChild(td);
            tbody.appendChild(tr);
        }

        function updateStats() {
            var active = users.filter(function (u) { return u.active; }).length;
            document.getElementById('statTotal').textContent = users.length;
            document.getElementById('statActive').textContent = active;
            document.getElementById('statSuspended').textContent = users.length - active;
            document.getElementById('statVoted').textContent = users.filter(function (u) { return u.hasVoted; }).length;
        }

        function buildRow(u) {
            var isMe = u.email === currentUserEmail;
            var roles = u.roles || [];
            var currentRole = roles[0];
            var tr = make('tr');

            tr.appendChild(make('td', 'id', '#' + u.id));

            var nameTd = make('td', 'name', u.username);
            if (isMe) nameTd.appendChild(make('span', 'a-you', 'You'));
            tr.appendChild(nameTd);

            tr.appendChild(make('td', 'email', u.email));

            // Role column: your own role is a badge; everyone else's role can be changed right here
            var roleTd = make('td');
            if (isMe) {
                roles.forEach(function (r) {
                    roleTd.appendChild(make('span', 'a-role' + (r === 'ROLE_SYSTEM_ADMINISTRATOR' ? ' is-admin' : ''), roleText(r)));
                });
            } else {
                var select = make('select', 'a-select a-role-select' + (currentRole === 'ROLE_SYSTEM_ADMINISTRATOR' ? ' is-admin' : ''));
                select.setAttribute('aria-label', 'Role of ' + u.username);
                ROLES.forEach(function (r) {
                    var opt = make('option', null, r.text);
                    opt.value = r.val;
                    opt.selected = (r.val === currentRole);
                    select.appendChild(opt);
                });
                select.addEventListener('change', function () {
                    var newRole = select.value;
                    if (!newRole || newRole === currentRole) return;
                    askConfirm('Change Role',
                        'Are you sure you want to change ' + u.username + "'s role from " + roleText(currentRole) + ' to ' + roleText(newRole) + '?',
                        'Change Role', 'a-btn-gold',
                        function () {
                            api('PUT', '/api/admin/users/' + u.id + '/role?role=' + encodeURIComponent(newRole))
                                .then(function (r) { afterAction(r, 'User role updated.'); })
                                .catch(function () { toast('Network error.', 'bad'); });
                        },
                        function () { select.value = currentRole; });
                });
                roleTd.appendChild(select);
            }
            tr.appendChild(roleTd);

            var statusTd = make('td');
            statusTd.appendChild(make('span', 'a-badge ' + (u.active ? 'ok' : 'bad'), u.active ? 'Active' : 'Suspended'));
            tr.appendChild(statusTd);

            var actionsTd = make('td');
            var actions = make('div', 'actions');

            var editBtn = make('button', 'a-btn a-btn-sm a-btn-quiet', 'Edit');
            editBtn.type = 'button';
            editBtn.disabled = isMe;
            editBtn.addEventListener('click', function () { openEdit(u); });
            actions.appendChild(editBtn);

            var toggleBtn = make('button', 'a-btn a-btn-sm a-btn-quiet ' + (u.active ? 'is-danger' : 'is-ok'), u.active ? 'Suspend' : 'Reactivate');
            toggleBtn.type = 'button';
            toggleBtn.disabled = isMe;
            toggleBtn.addEventListener('click', function () {
                var suspend = u.active;
                askConfirm(suspend ? 'Suspend User' : 'Reactivate User',
                    suspend ? 'Are you sure you want to suspend ' + u.username + '? They will not be able to log in.'
                            : 'Are you sure you want to reactivate ' + u.username + '?',
                    suspend ? 'Suspend' : 'Reactivate',
                    suspend ? 'a-btn-danger' : 'a-btn-ok',
                    function () {
                        api('PUT', '/api/admin/users/' + u.id + '/' + (suspend ? 'deactivate' : 'reactivate'))
                            .then(function (r) { afterAction(r, suspend ? 'User suspended.' : 'User reactivated.'); })
                            .catch(function () { toast('Network error.', 'bad'); });
                    });
            });
            actions.appendChild(toggleBtn);

            var deleteBtn = make('button', 'a-btn a-btn-sm a-btn-quiet is-danger', 'Delete');
            deleteBtn.type = 'button';
            deleteBtn.disabled = isMe;
            deleteBtn.addEventListener('click', function () {
                askConfirm('Delete User',
                    'Permanently delete ' + u.username + '? This action cannot be undone.',
                    'Delete Permanently', 'a-btn-danger',
                    function () {
                        api('DELETE', '/api/admin/users/' + u.id)
                            .then(function (r) { afterAction(r, 'User deleted.'); })
                            .catch(function () { toast('Network error.', 'bad'); });
                    });
            });
            actions.appendChild(deleteBtn);

            actionsTd.appendChild(actions);
            tr.appendChild(actionsTd);
            return tr;
        }

        function render() {
            var query = searchInput.value.trim().toLowerCase();
            var role = roleFilter.value;
            var shown = users.filter(function (u) {
                var text = ((u.username || '') + ' ' + (u.email || '')).toLowerCase();
                var textOk = !query || text.indexOf(query) !== -1;
                var roleOk = !role || (u.roles || []).indexOf(role) !== -1;
                return textOk && roleOk;
            });

            countLabel.textContent = users.length ? ('Showing ' + shown.length + ' of ' + users.length) : '';
            if (!shown.length) {
                setTableMessage(users.length ? 'No users match your search.' : 'No users found.');
                return;
            }
            tbody.textContent = '';
            shown.forEach(function (u) { tbody.appendChild(buildRow(u)); });
        }

        function loadUsers() {
            api('GET', '/api/admin/users')
                .then(function (result) {
                    if (!result.ok || !Array.isArray(result.data)) {
                        users = [];
                        updateStats();
                        countLabel.textContent = '';
                        setTableMessage('Unauthorized or failed to fetch users.');
                        return;
                    }
                    users = result.data.slice().sort(function (a, b) { return a.id - b.id; });
                    updateStats();
                    render();
                })
                .catch(function () { setTableMessage('Network error. Please refresh.'); });
        }

        searchInput.addEventListener('input', render);
        roleFilter.addEventListener('change', render);
        document.getElementById('refreshBtn').addEventListener('click', function () {
            setTableMessage('Loading…');
            loadUsers();
        });

        // ---------- add user ----------
        var addForm = document.getElementById('addUserForm');
        var addError = document.getElementById('addError');
        var addSubmit = document.getElementById('addSubmit');

        document.getElementById('openAddBtn').addEventListener('click', function () {
            addForm.reset();
            addError.hidden = true;
            openModal('addUserModal', 'addUsername');
        });

        addForm.addEventListener('submit', function (e) {
            e.preventDefault();
            addError.hidden = true;
            if (!addForm.checkValidity()) { addForm.reportValidity(); return; }
            var data = {
                username: document.getElementById('addUsername').value.trim(),
                email: document.getElementById('addEmail').value.trim(),
                password: document.getElementById('addPassword').value,
                role: document.getElementById('addRole').value
            };
            addSubmit.disabled = true;
            api('POST', '/api/auth/register', data)
                .then(function (result) {
                    addSubmit.disabled = false;
                    if (!result.ok) {
                        addError.textContent = String(result.data.message || 'Failed to create user.').replace(/^Error:\s*/, '');
                        addError.hidden = false;
                        return;
                    }
                    closeModal('addUserModal');
                    if (result.data.requiresOtp) {
                        toast('Voter created. They must verify their email before logging in.', 'ok',
                            { href: '/verify-registration?userId=' + encodeURIComponent(result.data.userId), text: 'Open verification page' });
                    } else {
                        toast('User created successfully.', 'ok');
                    }
                    loadUsers();
                })
                .catch(function () {
                    addSubmit.disabled = false;
                    addError.textContent = 'Network error.';
                    addError.hidden = false;
                });
        });

        // ---------- edit user ----------
        var editForm = document.getElementById('editUserForm');
        var editError = document.getElementById('editError');
        var editSubmit = document.getElementById('editSubmit');

        function openEdit(u) {
            editForm.reset();
            editError.hidden = true;
            document.getElementById('editUserId').value = u.id;
            document.getElementById('editUsername').value = u.username;
            document.getElementById('editEmail').value = u.email;
            openModal('editUserModal', 'editUsername');
        }

        editForm.addEventListener('submit', function (e) {
            e.preventDefault();
            editError.hidden = true;
            if (!editForm.checkValidity()) { editForm.reportValidity(); return; }
            var id = document.getElementById('editUserId').value;
            var data = {
                username: document.getElementById('editUsername').value.trim(),
                email: document.getElementById('editEmail').value.trim(),
                password: document.getElementById('editPassword').value
            };
            editSubmit.disabled = true;
            api('PUT', '/api/admin/users/' + encodeURIComponent(id), data)
                .then(function (result) {
                    editSubmit.disabled = false;
                    if (!result.ok) {
                        editError.textContent = String(result.data.message || 'Failed to update user details.').replace(/^Error:\s*/, '');
                        editError.hidden = false;
                        return;
                    }
                    closeModal('editUserModal');
                    toast(result.data.message || 'User details updated.', 'ok');
                    loadUsers();
                })
                .catch(function () {
                    editSubmit.disabled = false;
                    editError.textContent = 'Network error.';
                    editError.hidden = false;
                });
        });

        loadUsers();
    })();
</script>
</body>
</html>
