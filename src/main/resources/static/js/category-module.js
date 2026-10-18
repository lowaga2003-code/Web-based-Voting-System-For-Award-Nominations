/* Module 2 - Award Category Management: clock, countdowns, live statistics, form helpers. */
(function () {
  "use strict";

  function pad(n) { return (n < 10 ? "0" : "") + n; }

  /* ---------- departure-board clock ---------- */
  function startClock() {
    var clock = document.querySelector("[data-cm-clock]");
    if (!clock) return;
    var flaps = clock.querySelectorAll(".cm-flap");
    function tick() {
      var d = new Date();
      var text = pad(d.getHours()) + pad(d.getMinutes()) + pad(d.getSeconds());
      for (var i = 0; i < flaps.length && i < text.length; i++) {
        if (flaps[i].textContent !== text.charAt(i)) flaps[i].textContent = text.charAt(i);
      }
    }
    tick();
    setInterval(tick, 1000);
  }

  /* ---------- countdowns: <span data-cm-until="2026-10-12T18:00:00"> ---------- */
  function parseLocal(iso) {
    var m = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?/.exec(iso || "");
    if (!m) return null;
    return new Date(+m[1], +m[2] - 1, +m[3], +m[4], +m[5], +(m[6] || 0));
  }

  function format(ms, long) {
    if (ms <= 0) return "now";
    var s = Math.floor(ms / 1000);
    var d = Math.floor(s / 86400); s -= d * 86400;
    var h = Math.floor(s / 3600); s -= h * 3600;
    var m = Math.floor(s / 60); s -= m * 60;
    if (long) return (d > 0 ? d + "d " : "") + pad(h) + "h " + pad(m) + "m " + pad(s) + "s";
    if (d > 0) return d + "d " + h + "h";
    if (h > 0) return h + "h " + m + "m";
    return m + "m " + pad(s) + "s";
  }

  function startCountdowns() {
    var nodes = document.querySelectorAll("[data-cm-until]");
    if (!nodes.length) return;
    var items = [];
    for (var i = 0; i < nodes.length; i++) {
      var t = parseLocal(nodes[i].getAttribute("data-cm-until"));
      if (t) items.push({ el: nodes[i], t: t, long: nodes[i].hasAttribute("data-cm-long") });
    }
    function tick() {
      var now = Date.now();
      for (var j = 0; j < items.length; j++) items[j].el.textContent = format(items[j].t.getTime() - now, items[j].long);
    }
    tick();
    setInterval(tick, 1000);
  }

  /* ---------- live statistics (polls /manager/awards/api/live) ---------- */
  function setText(selector, value) {
    var nodes = document.querySelectorAll(selector);
    for (var i = 0; i < nodes.length; i++) {
      var v = String(value);
      if (nodes[i].textContent !== v) {
        nodes[i].textContent = v;
        var kpi = nodes[i].closest(".cm-kpi");
        if (kpi) { kpi.classList.remove("flash"); void kpi.offsetWidth; kpi.classList.add("flash"); }
      }
    }
  }

  function startLive() {
    var host = document.querySelector("[data-cm-live-url]");
    if (!host || !window.fetch) return;
    var url = host.getAttribute("data-cm-live-url");
    function apply(data) {
      var t = data.totals || {};
      for (var key in t) if (Object.prototype.hasOwnProperty.call(t, key)) setText('[data-cm-live="' + key + '"]', t[key]);
      var gauge = document.querySelector("[data-cm-gauge]");
      if (gauge && t.turnout !== undefined) gauge.style.setProperty("--p", t.turnout);
      var nom = Math.max(1, t.nominations || 0);
      ["approved", "underReview", "drafts"].forEach(function (k) {
        var bar = document.querySelector('[data-cm-stack="' + k + '"]');
        if (bar && t[k] !== undefined) bar.style.width = (t[k] * 100 / nom).toFixed(2) + "%";
      });
      (data.categories || []).forEach(function (c) {
        setText('[data-cm-cat-votes="' + c.id + '"]', c.votes);
        (c.nominees || []).forEach(function (n) {
          var key = c.id + ":" + n.id;
          setText('[data-cm-vote="' + key + '"]', n.votes);
          setText('[data-cm-pct="' + key + '"]', n.pct);
          var bars = document.querySelectorAll('[data-cm-bar="' + key + '"]');
          for (var i = 0; i < bars.length; i++) {
            bars[i].style.width = n.pct + "%";
            if (bars[i].parentNode) bars[i].parentNode.classList.toggle("lead", !!n.leader);
          }
        });
      });
      var hours = data.hours || [];
      var peak = 1;
      hours.forEach(function (v) { if (v > peak) peak = v; });
      hours.forEach(function (v, i) {
        var bar = document.querySelector('[data-cm-hour="' + i + '"]');
        if (!bar) return;
        bar.style.height = (v === 0 ? 2 : Math.max(4, v * 100 / peak)).toFixed(2) + "%";
        bar.classList.toggle("zero", v === 0);
        bar.title = v + " vote(s)";
      });
      var stamp = document.querySelector("[data-cm-updated]");
      if (stamp) { var d = new Date(); stamp.textContent = "LIVE \u00b7 " + pad(d.getHours()) + ":" + pad(d.getMinutes()) + ":" + pad(d.getSeconds()); }
    }
    function poll() {
      fetch(url, { headers: { "Accept": "application/json" }, credentials: "same-origin" })
        .then(function (r) { return r.ok ? r.json() : null; })
        .then(function (data) { if (data) apply(data); })
        .catch(function () { /* keep the last numbers */ });
    }
    setInterval(poll, 10000);
  }

  /* ---------- whole board rows are clickable ---------- */
  function rowLinks() {
    document.addEventListener("click", function (e) {
      var row = e.target.closest ? e.target.closest("tr[data-href]") : null;
      if (!row || e.target.closest("a, button, form")) return;
      window.location.href = row.getAttribute("data-href");
    });
  }

  /* ---------- confirmation before destructive forms ---------- */
  function confirms() {
    document.addEventListener("submit", function (e) {
      var form = e.target;
      var msg = form.getAttribute && form.getAttribute("data-cm-confirm");
      if (msg && !window.confirm(msg)) e.preventDefault();
    });
  }

  /* ---------- rule form: only show the field the chosen rule needs ---------- */
  function ruleForm() {
    var select = document.querySelector("[data-cm-rule-type]");
    if (!select) return;
    var parts = document.querySelectorAll("[data-cm-show-for]");
    function sync() {
      for (var i = 0; i < parts.length; i++) {
        parts[i].hidden = parts[i].getAttribute("data-cm-show-for") !== select.value;
      }
    }
    select.addEventListener("change", sync);
    sync();
  }

  /* ---------- timeline form: live preview of the two windows ---------- */
  function timelinePreview() {
    var form = document.querySelector("[data-cm-timeline-form]");
    if (!form) return;
    var track = form.querySelector("[data-cm-preview]");
    var msg = form.querySelector("[data-cm-preview-msg]");
    var input = function (k) { return form.querySelector('[data-cm-t="' + k + '"]'); };
    var keys = ["ns", "ne", "vs", "ve"];
    function sync() {
      var t = keys.map(function (k) { return parseLocal(input(k) && input(k).value); });
      var n = track.querySelector(".n"), v = track.querySelector(".v");
      if (t.some(function (x) { return !x; })) {
        msg.textContent = "Fill in all four dates."; msg.className = "cm-preview-msg"; return;
      }
      var problem = null;
      if (t[1] <= t[0]) problem = "Nominations must close after they open.";
      else if (t[2] < t[1]) problem = "Voting must open on or after the nomination close.";
      else if (t[3] <= t[2]) problem = "Voting must close after it opens.";
      var min = Math.min(t[0], t[2]), max = Math.max(t[1], t[3]);
      var span = Math.max(1, max - min);
      function place(el, a, b) {
        el.style.left = ((a - min) * 100 / span).toFixed(2) + "%";
        el.style.width = Math.max(1, (b - a) * 100 / span).toFixed(2) + "%";
      }
      place(n, t[0], t[1]);
      place(v, t[2], t[3]);
      if (problem) { msg.textContent = problem; msg.className = "cm-preview-msg bad"; return; }
      var days = function (a, b) { return Math.round((b - a) / 36e5 / 24 * 10) / 10; };
      msg.textContent = "Nominations " + days(t[0], t[1]) + " days, gap " + days(t[1], t[2]) + " days, voting " + days(t[2], t[3]) + " days.";
      msg.className = "cm-preview-msg";
    }
    keys.forEach(function (k) { var el = input(k); if (el) el.addEventListener("input", sync); });
    sync();
  }

  document.addEventListener("DOMContentLoaded", function () {
    startClock();
    startCountdowns();
    startLive();
    rowLinks();
    confirms();
    ruleForm();
    timelinePreview();
  });
})();
