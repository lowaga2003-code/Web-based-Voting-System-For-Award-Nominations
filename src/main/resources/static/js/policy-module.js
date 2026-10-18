/*
 * MODULE 6 - Policy & Consent Management: small page behaviour.
 * Every page also works without this file - the server checks every rule again.
 */
(function () {
    'use strict';

    function each(list, fn) {
        Array.prototype.forEach.call(list, fn);
    }

    /** Shows a message under a form field and moves the focus to it. */
    function showFieldError(field, message) {
        var id = field.id + '-error';
        var msg = document.getElementById(id);
        if (!msg) {
            msg = document.createElement('p');
            msg.id = id;
            msg.className = 'pm-field-error';
            msg.setAttribute('role', 'alert');
            field.insertAdjacentElement('afterend', msg);
            var described = field.getAttribute('aria-describedby');
            field.setAttribute('aria-describedby', described ? described + ' ' + id : id);
            field.addEventListener('input', function () {
                if (field.value.trim()) {
                    msg.textContent = '';
                    field.removeAttribute('aria-invalid');
                }
            });
        }
        msg.textContent = message;
        field.setAttribute('aria-invalid', 'true');
        field.focus();
    }

    // 1. "I agree" boxes: the button stays disabled until the box is ticked,
    //    and the version seals on the page are stamped when it is.
    each(document.querySelectorAll('[data-pm-agree]'), function (box) {
        var buttons = box.form ? box.form.querySelectorAll('[data-pm-agree-button]') : [];
        var scope = box.closest('[data-pm-stamp-scope]');
        function sync() {
            each(buttons, function (button) { button.disabled = !box.checked; });
            if (scope) {
                scope.classList.toggle('pm-stamped', box.checked);
            }
        }
        box.addEventListener('change', sync);
        sync();
    });

    // 2. Required notes (data-pm-needs-note="textarea id") and confirmations (data-pm-confirm="question")
    document.addEventListener('click', function (event) {
        var el = event.target.closest('[data-pm-needs-note], [data-pm-confirm]');
        if (!el) {
            return;
        }
        var noteId = el.getAttribute('data-pm-needs-note');
        if (noteId) {
            var note = document.getElementById(noteId);
            if (note && !note.value.trim()) {
                event.preventDefault();
                showFieldError(note, el.getAttribute('data-pm-needs-note-message') || 'Please fill in this field first.');
                return;
            }
        }
        var question = el.getAttribute('data-pm-confirm');
        if (question && !window.confirm(question)) {
            event.preventDefault();
        }
    });

    // 3. Request form: "I understand my account will be closed" only for "Delete my personal data"
    var requestForm = document.querySelector('[data-pm-request-form]');
    if (requestForm) {
        var confirmBox = requestForm.querySelector('[data-pm-delete-confirm]');
        var confirmInput = confirmBox ? confirmBox.querySelector('input') : null;
        var syncDelete = function () {
            var chosen = requestForm.querySelector('input[name="requestType"]:checked');
            var isDelete = !!chosen && chosen.value === 'DELETE_MY_DATA';
            if (confirmBox && confirmInput) {
                confirmBox.hidden = !isDelete;
                confirmInput.required = isDelete;
                if (!isDelete) {
                    confirmInput.checked = false;
                }
            }
        };
        requestForm.addEventListener('change', syncDelete);
        syncDelete();
    }

    // 4. Policy editor: the publish button shows the version number and the effect of the chosen change type
    var publishButton = document.querySelector('[data-pm-publish]');
    if (publishButton) {
        var versionLabel = publishButton.querySelector('[data-pm-publish-version]');
        var syncPublish = function () {
            var chosen = document.querySelector('input[name="change"]:checked');
            if (!chosen || !chosen.getAttribute('data-pm-version')) {
                return;
            }
            var version = chosen.getAttribute('data-pm-version');
            if (versionLabel) {
                versionLabel.textContent = version;
            }
            publishButton.setAttribute('data-pm-confirm', chosen.value === 'major'
                ? 'Publish version ' + version + ' now? Every user will have to agree to it before they can continue. Published versions can\'t be changed.'
                : 'Publish version ' + version + ' now? Users are told about it but don\'t have to agree again. Published versions can\'t be changed.');
        };
        document.addEventListener('change', function (event) {
            if (event.target.name === 'change') {
                syncPublish();
            }
        });
        syncPublish();
    }
})();
