/* Voting - ballot selection, review dialog and countdowns. The server checks every rule again. */
(function () {
  "use strict";

  function pad(n) { return (n < 10 ? "0" : "") + n; }

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
    if (long) return (d > 0 ? d + "d " : "") + pad(h) + ":" + pad(m) + ":" + pad(s);
    if (d > 0) return d + "d " + h + "h";
    if (h > 0) return h + "h " + m + "m";
    return m + "m " + pad(s) + "s";
  }

  function countdowns() {
    var nodes = document.querySelectorAll("[data-vt-until]");
    var items = [];
    for (var i = 0; i < nodes.length; i++) {
      var t = parseLocal(nodes[i].getAttribute("data-vt-until"));
      if (t) items.push({ el: nodes[i], t: t, long: nodes[i].hasAttribute("data-vt-long") });
    }
    if (!items.length) return;
    function tick() {
      var now = Date.now();
      for (var j = 0; j < items.length; j++) items[j].el.textContent = format(items[j].t.getTime() - now, items[j].long);
    }
    tick();
    setInterval(tick, 1000);
  }

  function ballot() {
    var form = document.querySelector("[data-vt-ballot]");
    if (!form) return;
    var choice = form.querySelector("[data-vt-choice]");
    var dialog = document.querySelector("[data-vt-dialog]");
    var confirmed = false;

    function selected() { return form.querySelector('input[name="nomineeId"]:checked'); }

    form.addEventListener("change", function (e) {
      if (e.target.name === "nomineeId" && choice) {
        choice.textContent = e.target.getAttribute("data-vt-name");
      }
    });

    form.addEventListener("submit", function (e) {
      if (confirmed || !dialog || typeof dialog.showModal !== "function") return;
      var pick = selected();
      if (!pick) return;
      e.preventDefault();
      dialog.querySelector("[data-vt-dialog-name]").textContent = pick.getAttribute("data-vt-name");
      dialog.showModal();
    });

    if (dialog) {
      dialog.querySelector("[data-vt-cancel]").addEventListener("click", function () { dialog.close(); });
      dialog.querySelector("[data-vt-go]").addEventListener("click", function () {
        confirmed = true;
        var btn = this;
        btn.disabled = true;
        btn.textContent = "Sealing...";
        dialog.close();
        if (typeof form.requestSubmit === "function") form.requestSubmit(); else form.submit();
      });
    }
  }

  document.addEventListener("DOMContentLoaded", function () {
    countdowns();
    ballot();
  });
})();
