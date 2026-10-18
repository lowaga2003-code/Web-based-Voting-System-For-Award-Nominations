<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.pation.awardnomination.nominee.Interview" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    // Makes database text safe inside HTML: & < > " become HTML codes,
    // so a location like Hall "A" or <b> shows exactly as typed and cannot break the page.
    String esc(String text) {
        if (text == null) { return ""; }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    // The date box sends "2026-10-15". This makes it easy to read, e.g. pattern "EEEE" gives "Thursday".
    // If the text is in another form, it is shown as it is.
    String showDate(String text, String pattern) {
        try {
            return LocalDate.parse(text).format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
        } catch (Exception e) {
            return esc(text);
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Interviews | Award Nomination System</title>
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
        .hero-inner { max-width: 960px; margin: 0 auto; }
        .hero .back { color: #f3d891; font-weight: 700; font-size: 0.92rem; text-decoration: none; }
        .hero .back:hover { text-decoration: underline; }
        .hero h1 { margin: 14px 0 10px; font-family: 'Cormorant Garamond', Georgia, serif; font-size: clamp(2.4rem, 5.5vw, 3.4rem); line-height: 1; color: #ffffff; }
        .hero p { margin: 0; max-width: 60ch; color: #ead7dc; }

        /* ===== 3. Layout and cards ===== */
        .page { position: relative; max-width: 1000px; margin: -60px auto 110px; padding: 0 20px; display: grid; gap: 20px; align-items: start; }
        .page.with-form { grid-template-columns: 330px 1fr; }      /* admin: form on the left, slots on the right */
        .page > * { min-width: 0; }
        .column { display: grid; gap: 20px; min-width: 0; }
        .card { padding: 28px; background: #ffffff; border: 1px solid #e6dde0; border-radius: 18px; box-shadow: 0 18px 40px rgba(60, 20, 35, 0.10); }
        .card h2 { margin: 0 0 4px; font-family: 'Cormorant Garamond', Georgia, serif; font-size: 1.7rem; line-height: 1.15; color: #3b0f1f; }
        .card .lead { margin: 0 0 18px; color: #6c5a60; }

        /* ===== 4. Your interview (nominee) - looks like a certificate ===== */
        .card.ticket { padding: 8px; }
        .ticket-inner { display: flex; align-items: center; gap: 20px; padding: 22px; border: 1.5px solid #e2c27a; border-radius: 12px; }
        .seal { flex: none; width: 58px; height: 58px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #c08a1e; color: #ffffff; box-shadow: 0 0 0 5px #f6e7c4; }
        .seal svg { width: 30px; height: 30px; }
        .ticket .when { margin: 4px 0 2px; font-size: 1.1rem; font-weight: 800; }
        .ticket .where { margin: 0; color: #5f4d53; }
        .ticket .where a { color: #7a1d3b; font-weight: 700; }
        .ticket .hint { margin: 10px 0 0; font-size: 0.9rem; color: #6c5a60; }

        /* ===== 5. Slots: each one has a small calendar page ===== */
        .slot-head { display: flex; align-items: baseline; justify-content: space-between; gap: 12px; flex-wrap: wrap; margin-bottom: 18px; }
        .count { margin: 0; font-weight: 700; color: #6c5a60; }
        .slots { list-style: none; margin: 0; padding: 0; display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 14px; }
        .slot { display: flex; flex-direction: column; gap: 14px; padding: 16px; border: 1.5px solid #e6dde0; border-radius: 14px; background: #ffffff; }
        .slot-top { display: flex; align-items: center; gap: 14px; }
        .calendar { flex: none; width: 72px; overflow: hidden; border: 1.5px solid #e6dde0; border-radius: 10px; text-align: center; }
        .calendar .month { display: block; padding: 3px 4px; background: #7a1d3b; color: #ffffff; font-size: 0.72rem; font-weight: 700; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
        .calendar .day { display: block; padding: 4px; font-size: 1.7rem; font-weight: 800; line-height: 1.15; color: #3b0f1f; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
        .slot-info { min-width: 0; }
        .slot-info strong { display: block; font-size: 1.02rem; }
        .slot-info span { display: block; font-size: 0.92rem; color: #5f4d53; overflow-wrap: anywhere; }

        .slot.taken { background: #faf8f9; }                       /* booked by someone else */
        .slot.taken .calendar .month { background: #a3949a; }
        .slot.taken .calendar .day, .slot.taken .slot-info strong, .slot.taken .slot-info span { color: #8b7a80; }
        .slot.mine { border-color: #c08a1e; box-shadow: 0 0 0 4px #f6e7c4; }   /* the nominee's own slot */

        .badge { display: inline-block; align-self: flex-start; padding: 4px 12px; border-radius: 999px; font-size: 0.82rem; font-weight: 700; }
        .badge-available { background: #dcfce7; color: #166534; }
        .badge-booked { background: #f3e6ea; color: #7a1d3b; }
        .badge-taken { background: #eef2f6; color: #475569; }
        .badge-mine { background: #fbf3df; color: #8a5a00; }
        .booked-by { margin-top: -8px; font-size: 0.85rem; color: #6c5a60; }
        .empty { margin: 0; padding: 28px 10px; text-align: center; color: #6c5a60; }

        /* ===== 6. Add a slot (admin) + validation messages ===== */
        .field { margin-bottom: 16px; }
        .field label { display: block; margin-bottom: 6px; font-weight: 700; }
        .field input { width: 100%; padding: 12px 14px; border: 1.5px solid #d8cdd1; border-radius: 10px; font: inherit; color: #241418; background: #ffffff; }
        .field input:focus { outline: none; border-color: #7a1d3b; box-shadow: 0 0 0 4px rgba(122, 29, 59, 0.14); }
        .field .help { margin: 6px 0 0; font-size: 0.85rem; color: #6c5a60; }
        .field .error { display: none; margin: 6px 0 0; font-size: 0.85rem; font-weight: 700; color: #b42318; }
        .field.has-error input { border-color: #b42318; }
        .field.has-error .error { display: block; }
        .field.has-error .help { display: none; }

        /* ===== 7. Buttons (colour codes are written directly) ===== */
        .btn { display: inline-flex; align-items: center; justify-content: center; width: 100%; padding: 12px 18px; border-radius: 12px; border: 0; font: 700 0.95rem 'Manrope', 'Segoe UI', sans-serif; text-decoration: none; cursor: pointer; }
        .btn:focus-visible { outline: 3px solid #241418; outline-offset: 3px; }
        .btn-book { background: #7a1d3b; color: #ffffff; }
        .btn-book:hover { background: #611630; }
        .btn-move { background: #ffffff; color: #7a1d3b; border: 1.5px solid #7a1d3b; }
        .btn-move:hover { background: #f8eef1; }
        .btn-add { margin-top: 4px; background: #c08a1e; color: #241418; }
        .btn-add:hover { background: #a87712; }

        /* ===== 8. Phones ===== */
        @media (max-width: 820px) { .page.with-form { grid-template-columns: 1fr; } }
        @media (max-width: 560px) {
            .card { padding: 22px 18px; }
            .ticket-inner { flex-direction: column; align-items: flex-start; padding: 18px; }
        }
    </style>
</head>
<body>
<%-- [INTEGRATION] system navigation bar (Dashboard / Logout) --%>
<%@ include file="/WEB-INF/jsp/common/portalBar.jspf" %>

<%
    List<Interview> interviews = (List<Interview>) request.getAttribute("interviews");
    Interview bookedInterview = (Interview) request.getAttribute("bookedInterview");
    String role = (String) session.getAttribute("role");
    String userNic = (String) session.getAttribute("userNic");
    boolean isAdmin = "ADMIN".equals(role);
    boolean isUser = "USER".equals(role);

    // back link: admin -> nominee list, nominee -> own profile, others -> nominee portal
    String backUrl = "/nominee/home";
    String backText = "Back to the nominee portal";
    if (isAdmin) { backUrl = "/nominee/list"; backText = "Back to the nominee list"; }
    else if (isUser) { backUrl = "/nominee/profile"; backText = "Back to my nomination"; }

    // count free and booked slots for the heading
    int availableCount = 0;
    int totalSlots = (interviews != null) ? interviews.size() : 0;
    if (interviews != null) {
        for (Interview s : interviews) {
            if ("Available".equals(s.getStatus())) { availableCount++; }
        }
    }
%>

<header class="hero">
    <div class="hero-inner">
        <a class="back" href="<%= backUrl %>"><%= backText %></a>
        <% if (isAdmin) { %>
        <h1>Interview slots</h1>
        <p>Add interview times and see which slots nominees have booked.</p>
        <% } else if (isUser) { %>
        <h1>Book an interview</h1>
        <p><%= bookedInterview != null ? "Your interview is booked. You can move it to another free slot if the time does not suit you." : "Choose a free slot below to meet the selection committee." %></p>
        <% } else { %>
        <h1>Interview slots</h1>
        <p>Log in with your NIC on the nominee portal to book a slot.</p>
        <% } %>
    </div>
</header>

<main class="page<%= isAdmin ? " with-form" : "" %>">

    <%-- ADMIN: add a new slot. POST /nominee/interview/add -> NomineeController.addInterviewSlot() --%>
    <% if (isAdmin) { %>
    <form class="card" action="/nominee/interview/add" method="post" onsubmit="return validateSlot()" novalidate>
        <h2>Add a slot</h2>
        <p class="lead">Nominees can book it as soon as you add it.</p>
        <div class="field">
            <label for="interviewDate">Date</label>
            <input type="date" id="interviewDate" name="interviewDate" required oninput="clearError('interviewDate')">
            <p class="error" aria-live="polite"></p>
        </div>
        <div class="field">
            <label for="interviewTime">Time</label>
            <input type="text" id="interviewTime" name="interviewTime" placeholder="10:00 AM" required oninput="clearError('interviewTime')">
            <p class="help">Write it like 10:00 AM or 2:30 PM.</p>
            <p class="error" aria-live="polite"></p>
        </div>
        <div class="field">
            <label for="location">Location or meeting link</label>
            <input type="text" id="location" name="location" placeholder="Hall A, or an online meeting link" required oninput="clearError('location')">
            <p class="error" aria-live="polite"></p>
        </div>
        <button type="submit" class="btn btn-add">Add slot</button>
    </form>
    <% } %>

    <div class="column">

        <%-- NOMINEE: the slot they booked (bookedInterview comes from NomineeController.viewInterviews()) --%>
        <% if (bookedInterview != null) { %>
        <section class="card ticket">
            <div class="ticket-inner">
                <span class="seal">
                    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M6 12.5l4 4L18 8" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </span>
                <div>
                    <h2>Your interview is booked</h2>
                    <p class="when"><%= showDate(bookedInterview.getInterviewDate(), "EEEE, d MMMM yyyy") %> at <%= esc(bookedInterview.getInterviewTime()) %></p>
                    <% String place = bookedInterview.getLocation(); %>
                    <% if (place != null && place.startsWith("http")) { %>
                    <p class="where">Online: <a href="<%= esc(place) %>" target="_blank" rel="noopener">open the meeting link</a></p>
                    <% } else { %>
                    <p class="where"><%= esc(place) %></p>
                    <% } %>
                    <p class="hint">To change the time, choose another free slot below.</p>
                </div>
            </div>
        </section>
        <% } %>

        <%-- All slots --%>
        <section class="card">
            <div class="slot-head">
                <h2>All slots</h2>
                <p class="count"><%= availableCount %> free, <%= totalSlots - availableCount %> booked</p>
            </div>

            <% if (interviews == null || interviews.isEmpty()) { %>
            <p class="empty"><%= isAdmin ? "No slots yet. Add the first one with the form." : "The award office has not opened interview slots yet. Please check again later." %></p>
            <% } else { %>
            <ul class="slots">
                <%
                    for (Interview slot : interviews) {
                        boolean available = "Available".equals(slot.getStatus());
                        boolean mine = isUser && userNic != null && userNic.equals(slot.getNomineeNic());
                        String slotClass = mine ? " mine" : (available ? "" : " taken");
                %>
                <li class="slot<%= slotClass %>">
                    <div class="slot-top">
                        <span class="calendar">
                            <span class="month"><%= showDate(slot.getInterviewDate(), "MMM yyyy") %></span>
                            <span class="day"><%= showDate(slot.getInterviewDate(), "d") %></span>
                        </span>
                        <div class="slot-info">
                            <strong><%= showDate(slot.getInterviewDate(), "EEEE") %></strong>
                            <span><%= esc(slot.getInterviewTime()) %></span>
                            <span><%= esc(slot.getLocation()) %></span>
                        </div>
                    </div>

                    <%-- Slot එකේ status එකයි role එකයි බලලා button එක හෝ badge එක පෙන්වනවා --%>
                    <% if (mine) { %>
                    <span class="badge badge-mine">Your slot</span>
                    <% } else if (available && isUser && bookedInterview == null) { %>
                    <a class="btn btn-book" href="/nominee/interview/book/<%= slot.getId() %>">Book this slot</a>
                    <% } else if (available && isUser) { %>
                    <a class="btn btn-move" href="/nominee/interview/book/<%= slot.getId() %>" onclick="return confirm('Move your interview to this slot? Your current slot will be free for others.');">Change to this slot</a>
                    <% } else if (available) { %>
                    <span class="badge badge-available">Available</span>
                    <% } else if (isAdmin) { %>
                    <span class="badge badge-booked">Booked</span>
                    <span class="booked-by">Booked by NIC <%= esc(slot.getNomineeNic()) %></span>
                    <% } else { %>
                    <span class="badge badge-taken">Booked</span>
                    <% } %>
                </li>
                <% } %>
            </ul>
            <% } %>
        </section>
    </div>
</main>

<%-- AI assistant for nominees (admins do not need it on this page) --%>
<% if (!isAdmin) { %>
<%@ include file="nomineeChatbot.jspf" %>
<% } %>

<script>
    // ===== Add-slot form validation (admin) =====
    function showError(id, message) {
        var box = document.getElementById(id).closest('.field');
        box.classList.add('has-error');
        box.querySelector('.error').textContent = message;
    }

    function clearError(id) {
        document.getElementById(id).closest('.field').classList.remove('has-error');
    }

    // today's date as "yyyy-mm-dd", the same form the date box uses
    function todayText() {
        var d = new Date();
        var month = ('0' + (d.getMonth() + 1)).slice(-2);
        var day = ('0' + d.getDate()).slice(-2);
        return d.getFullYear() + '-' + month + '-' + day;
    }

    function validateSlot() {
        var ok = true;
        var date = document.getElementById('interviewDate').value;
        var time = document.getElementById('interviewTime').value.trim();
        var place = document.getElementById('location').value.trim();

        if (date === '') { showError('interviewDate', 'Choose a date.'); ok = false; }
        else if (date < todayText()) { showError('interviewDate', 'Choose today or a later date.'); ok = false; }

        if (!/^(0?[1-9]|1[0-2]):[0-5][0-9] ?(AM|PM)$/i.test(time)) { showError('interviewTime', 'Write the time like 10:00 AM.'); ok = false; }

        if (place === '') { showError('location', 'Enter a room name or an online meeting link.'); ok = false; }

        if (ok) {
            // save it neatly: "2:30 pm" becomes "2:30 PM", without extra spaces
            document.getElementById('interviewTime').value = time.toUpperCase();
            document.getElementById('location').value = place;
        }
        return ok;
    }

    // past days cannot be picked in the date box
    var dateBox = document.getElementById('interviewDate');
    if (dateBox) { dateBox.min = todayText(); }
</script>

</body>
</html>
