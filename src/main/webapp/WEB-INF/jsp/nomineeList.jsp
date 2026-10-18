<%@ page import="java.util.List" %>
<%@ page import="com.pation.awardnomination.nominee.Nominee" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    // Makes database text safe inside HTML: & < > " become HTML codes,
    // so a name like Silva "Jr" or <b> shows exactly as typed and cannot break the page.
    String esc(String text) {
        if (text == null) { return ""; }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nominees | Award Nomination System</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@600;700&family=Manrope:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        /* ===== 1. Page ===== */
        * { box-sizing: border-box; }
        body { margin: 0; background: #f4f2f3; color: #241418; font-family: 'Manrope', 'Segoe UI', sans-serif; line-height: 1.55; }

        /* ===== 2. Top band ===== */
        .hero {
            margin-top: -28px; padding: 96px 20px 104px; color: #f3e6ea;
            background-color: #5b1630;
            background-image:
                repeating-radial-gradient(circle at 12% 135%, rgba(243, 216, 145, 0.12) 0 1px, transparent 1px 11px),
                repeating-radial-gradient(circle at 92% -45%, rgba(243, 216, 145, 0.09) 0 1px, transparent 1px 13px);
        }
        .hero-inner { max-width: 1060px; margin: 0 auto; display: flex; align-items: flex-end; justify-content: space-between; gap: 20px; flex-wrap: wrap; }
        .hero h1 { margin: 0 0 10px; font-family: 'Cormorant Garamond', Georgia, serif; font-size: clamp(2.4rem, 5.5vw, 3.4rem); line-height: 1; color: #ffffff; }
        .hero p { margin: 0; color: #ead7dc; }
        .hero-actions { display: flex; gap: 10px; flex-wrap: wrap; }

        /* ===== 3. Content ===== */
        .page { position: relative; max-width: 1100px; margin: -60px auto 100px; padding: 0 20px; }

        /* stats: click a box to show only that status */
        .stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 20px; }
        .stat { padding: 16px 18px; text-align: left; border-radius: 16px; border: 1px solid #e6dde0; background: #ffffff; font: inherit; color: inherit; cursor: pointer; box-shadow: 0 12px 30px rgba(60, 20, 35, 0.08); }
        .stat:hover { border-color: #c9b6bd; }
        .stat .label { display: block; font-size: 0.9rem; font-weight: 700; color: #6c5a60; }
        .stat .value { display: block; font-size: 2rem; font-weight: 800; font-variant-numeric: tabular-nums; }
        .stat-total .value { color: #3b0f1f; }
        .stat-approved .value { color: #166534; }
        .stat-review .value { color: #92400e; }
        .stat-draft .value { color: #475569; }

        .card { background: #ffffff; border: 1px solid #e6dde0; border-radius: 18px; overflow: hidden; }

        /* search + status filter */
        .toolbar { display: flex; align-items: center; justify-content: space-between; gap: 14px; flex-wrap: wrap; padding: 18px 20px; border-bottom: 1px solid #efe8ea; }
        .search { flex: 1; min-width: 220px; max-width: 360px; padding: 11px 14px; border: 1.5px solid #d8cdd1; border-radius: 10px; font: inherit; }
        .search:focus { outline: none; border-color: #7a1d3b; box-shadow: 0 0 0 4px rgba(122, 29, 59, 0.14); }
        .chips { display: flex; gap: 8px; flex-wrap: wrap; }
        .chip { padding: 8px 14px; border-radius: 999px; border: 1.5px solid #d8cdd1; background: #ffffff; color: #5f4d53; font: 700 0.85rem 'Manrope', 'Segoe UI', sans-serif; cursor: pointer; }
        .chip.active { background: #3b0f1f; border-color: #3b0f1f; color: #ffffff; }

        /* table */
        .table-scroll { overflow-x: auto; }
        table { width: 100%; min-width: 760px; border-collapse: collapse; }
        th { padding: 12px 20px; text-align: left; font-size: 0.85rem; font-weight: 700; color: #6c5a60; background: #faf7f8; border-bottom: 1px solid #efe8ea; }
        td { padding: 14px 20px; border-bottom: 1px solid #f1ebed; vertical-align: middle; }
        tbody tr:hover td { background: #fdf9fa; }
        .id { color: #8b7a80; font-variant-numeric: tabular-nums; }
        .name { font-weight: 700; }
        .nic { display: block; font-size: 0.85rem; color: #8b7a80; font-variant-numeric: tabular-nums; }
        .no-file { color: #a3949a; font-style: italic; }
        .empty { padding: 48px 20px; text-align: center; color: #6c5a60; }

        /* status badges */
        .badge { display: inline-block; padding: 4px 12px; border-radius: 999px; font-size: 0.82rem; font-weight: 700; white-space: nowrap; }
        .badge-draft { background: #eef2f6; color: #475569; }
        .badge-review { background: #fef3c7; color: #92400e; }
        .badge-approved { background: #dcfce7; color: #166534; }

        /* ===== 4. Buttons (colour codes are written directly) ===== */
        .btn { display: inline-flex; align-items: center; justify-content: center; padding: 12px 18px; border-radius: 12px; border: 0; font: 700 0.95rem 'Manrope', 'Segoe UI', sans-serif; text-decoration: none; cursor: pointer; }
        .btn:focus-visible, .stat:focus-visible, .chip:focus-visible { outline: 3px solid #241418; outline-offset: 3px; }
        .btn-add { background: #c08a1e; color: #241418; }
        .btn-add:hover { background: #a87712; }
        .btn-interviews { background: #ffffff; color: #5b1630; }
        .btn-interviews:hover { background: #f8eef1; }
        .btn-edit { padding: 8px 14px; background: #7a1d3b; color: #ffffff; font-size: 0.85rem; }
        .btn-edit:hover { background: #611630; }
        .btn-delete { padding: 8px 14px; background: #ffffff; color: #b42318; border: 1.5px solid #f0b4ad; font-size: 0.85rem; }
        .btn-delete:hover { background: #fdecea; }
        .actions { display: flex; gap: 8px; }

        @media (max-width: 760px) { .stats { grid-template-columns: 1fr 1fr; } }
    </style>
</head>
<body>
<%-- [INTEGRATION] system navigation bar (Dashboard / Logout) --%>
<%@ include file="/WEB-INF/jsp/common/portalBar.jspf" %>

<%
    List<Nominee> nominees = (List<Nominee>) request.getAttribute("nominees");
    String role = (String) session.getAttribute("role");
    String loggedUserNic = (String) session.getAttribute("userNic");

    // counts are calculated in NomineeController.viewNominees()
    Long totalCount = (Long) request.getAttribute("totalCount");
    Long approvedCount = (Long) request.getAttribute("approvedCount");
    Long reviewCount = (Long) request.getAttribute("reviewCount");
    Long draftCount = (Long) request.getAttribute("draftCount");
%>

<header class="hero">
    <div class="hero-inner">
        <div>
            <h1>Nominees</h1>
            <p>Review nominations, update their status and manage interviews.<% if (role != null) { %> Signed in as <strong><%= role %></strong>.<% } %></p>
        </div>
        <div class="hero-actions">
            <a class="btn btn-interviews" href="/nominee/interviews"><%= "ADMIN".equals(role) ? "Manage interviews" : "Interview slots" %></a>
            <% if ("ADMIN".equals(role)) { %>
            <a class="btn btn-add" href="/nominee/form">Add a nominee</a>
            <% } %>
        </div>
    </div>
</header>

<main class="page">

    <%-- Stats: click a box to filter the table --%>
    <section class="stats">
        <button type="button" class="stat stat-total" onclick="setFilter('All')"><span class="label">Total</span><span class="value"><%= totalCount != null ? totalCount : 0 %></span></button>
        <button type="button" class="stat stat-approved" onclick="setFilter('Approved')"><span class="label">Approved</span><span class="value"><%= approvedCount != null ? approvedCount : 0 %></span></button>
        <button type="button" class="stat stat-review" onclick="setFilter('Under Review')"><span class="label">Under review</span><span class="value"><%= reviewCount != null ? reviewCount : 0 %></span></button>
        <button type="button" class="stat stat-draft" onclick="setFilter('Draft')"><span class="label">Draft</span><span class="value"><%= draftCount != null ? draftCount : 0 %></span></button>
    </section>

    <section class="card">
        <div class="toolbar">
            <input type="search" class="search" id="searchBox" placeholder="Search by name, company or NIC" oninput="filterTable()" aria-label="Search nominees">
            <div class="chips" role="group" aria-label="Filter by status">
                <button type="button" class="chip active" data-status="All" onclick="setFilter('All')">All</button>
                <button type="button" class="chip" data-status="Draft" onclick="setFilter('Draft')">Draft</button>
                <button type="button" class="chip" data-status="Under Review" onclick="setFilter('Under Review')">Under review</button>
                <button type="button" class="chip" data-status="Approved" onclick="setFilter('Approved')">Approved</button>
            </div>
        </div>

        <div class="table-scroll">
            <table id="nomineeTable">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Nominee</th>
                    <th>Company</th>
                    <th>Evidence</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (nominees != null && !nominees.isEmpty()) {
                        for (Nominee n : nominees) {
                            String badgeClass = "badge-review";
                            if ("Draft".equals(n.getStatus())) { badgeClass = "badge-draft"; }
                            else if ("Approved".equals(n.getStatus())) { badgeClass = "badge-approved"; }
                %>
                <tr data-status="<%= n.getStatus() %>">
                    <td class="id">#<%= n.getId() %></td>
                    <td><span class="name"><%= esc(n.getFullName()) %></span><span class="nic"><%= esc(n.getNic()) %></span></td>
                    <td><%= esc(n.getCompanyName()) %></td>
                    <td><% if (n.getEvidenceFileName() != null) { %><%= esc(n.getEvidenceFileName()) %><% } else { %><span class="no-file">No file</span><% } %></td>
                    <td><span class="badge <%= badgeClass %>"><%= n.getStatus() %></span></td>
                    <td>
                        <div class="actions">
                            <% if ("ADMIN".equals(role) || ("USER".equals(role) && n.getNic() != null && n.getNic().equals(loggedUserNic))) { %>
                            <a class="btn btn-edit" href="/nominee/edit/<%= n.getId() %>">Edit</a>
                            <% } %>
                            <% if ("ADMIN".equals(role)) { %>
                            <a class="btn btn-delete" href="/nominee/delete/<%= n.getId() %>" onclick="return confirm('Delete nominee #<%= n.getId() %>? This cannot be undone.');">Delete</a>
                            <% } %>
                        </div>
                    </td>
                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>
        </div>

        <% if (nominees == null || nominees.isEmpty()) { %>
        <p class="empty">No nominees yet. Use "Add a nominee" to create the first one.</p>
        <% } %>
        <p class="empty" id="noMatch" hidden>No nominees match your search. Clear the search box or choose All.</p>
    </section>
</main>

<script>
    // ===== Search + status filter (runs in the browser, no page reload) =====
    var currentStatus = 'All';

    function setFilter(status) {
        currentStatus = status;
        var chips = document.querySelectorAll('.chip');
        for (var i = 0; i < chips.length; i++) {
            chips[i].classList.toggle('active', chips[i].getAttribute('data-status') === status);
        }
        filterTable();
    }

    function filterTable() {
        var text = document.getElementById('searchBox').value.trim().toLowerCase();
        var rows = document.querySelectorAll('#nomineeTable tbody tr');
        var shown = 0;
        for (var i = 0; i < rows.length; i++) {
            var rowText = (rows[i].cells[1].textContent + ' ' + rows[i].cells[2].textContent).toLowerCase();   // name, NIC and company
            var matchText = rowText.indexOf(text) !== -1;
            var matchStatus = currentStatus === 'All' || rows[i].getAttribute('data-status') === currentStatus;
            rows[i].hidden = !(matchText && matchStatus);
            if (matchText && matchStatus) { shown++; }
        }
        document.getElementById('noMatch').hidden = (shown > 0 || rows.length === 0);
    }
</script>

</body>
</html>
