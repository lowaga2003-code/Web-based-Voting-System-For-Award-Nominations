/*
 * MODULE 6 - rich-text editor for policy drafts (Legal & Governance Officer).
 * Uses the browser's built-in editing commands. The formatted text is copied into the hidden "content"
 * field; the server keeps only safe tags (PolicyHtml.sanitize) before it saves the draft.
 * Without JavaScript the plain HTML box stays visible and still works.
 */
(function () {
    'use strict';

    var wrap = document.querySelector('[data-pm-editor]');
    if (!wrap || typeof document.execCommand !== 'function') {
        return;
    }
    var box = wrap.querySelector('[data-pm-editor-box]');
    var editable = wrap.querySelector('[data-pm-editable]');
    var source = wrap.querySelector('[data-pm-editor-source]');
    if (!box || !editable || !source || !source.form) {
        return;
    }
    var form = source.form;
    var toolbar = box.querySelector('.pm-toolbar');

    // switch from the HTML box to the editor
    editable.innerHTML = source.value;
    source.hidden = true;
    source.required = false;              // a hidden field can't show the browser's "required" message
    box.hidden = false;
    var label = wrap.querySelector('label[for="' + source.id + '"]');
    if (label) {
        label.removeAttribute('for');
        label.id = label.id || 'contentLabel';
        editable.removeAttribute('aria-label');
        editable.setAttribute('aria-labelledby', label.id);
        label.addEventListener('click', function () { editable.focus(); });
    }
    try {
        document.execCommand('defaultParagraphSeparator', false, 'p');
        document.execCommand('styleWithCSS', false, false);
    } catch (ignored) {
        // older browsers
    }

    var dirty = false;
    var saved = null;

    function sync() {
        source.value = editable.innerHTML;
    }

    function selectionInEditor() {
        var sel = window.getSelection();
        return sel && sel.rangeCount > 0 && editable.contains(sel.anchorNode);
    }

    function restoreSelection() {
        editable.focus();
        if (saved) {
            var sel = window.getSelection();
            sel.removeAllRanges();
            sel.addRange(saved);
        }
    }

    function updateState() {
        ['bold', 'italic', 'underline'].forEach(function (cmd) {
            var button = toolbar.querySelector('[data-cmd="' + cmd + '"]');
            var on = false;
            try {
                on = selectionInEditor() && document.queryCommandState(cmd);
            } catch (ignored) {
                on = false;
            }
            if (button) {
                button.setAttribute('aria-pressed', on ? 'true' : 'false');
            }
        });
    }

    function run(cmd) {
        switch (cmd) {
            case 'h2':
            case 'h3':
                document.execCommand('formatBlock', false, cmd.toUpperCase());
                break;
            case 'p':
                document.execCommand('formatBlock', false, 'P');
                break;
            case 'quote':
                document.execCommand('formatBlock', false, 'BLOCKQUOTE');
                break;
            case 'bold':
            case 'italic':
            case 'underline':
                document.execCommand(cmd, false, null);
                break;
            case 'ul':
                document.execCommand('insertUnorderedList', false, null);
                break;
            case 'ol':
                document.execCommand('insertOrderedList', false, null);
                break;
            case 'link':
                var url = window.prompt('Link address - https://..., mailto:... or a page of this site such as /policy/terms', 'https://');
                if (url === null) {
                    return;
                }
                url = url.trim();
                if (/^(https?:\/\/\S+|mailto:\S+|\/(?!\/)\S*|#\S*)$/i.test(url)) {
                    restoreSelection();
                    document.execCommand('createLink', false, url);
                } else {
                    window.alert('Use an address that starts with https://, http://, mailto: or / (a page of this site).');
                    return;
                }
                break;
            case 'unlink':
                document.execCommand('unlink', false, null);
                break;
            case 'clear':
                document.execCommand('removeFormat', false, null);
                document.execCommand('unlink', false, null);
                document.execCommand('formatBlock', false, 'P');
                break;
            default:
                return;
        }
        dirty = true;
        sync();
        updateState();
    }

    document.addEventListener('selectionchange', function () {
        if (selectionInEditor()) {
            saved = window.getSelection().getRangeAt(0).cloneRange();
        }
        updateState();
    });

    // keep the selection in the text when a toolbar button is pressed with the mouse
    toolbar.addEventListener('mousedown', function (event) {
        if (event.target.closest('button')) {
            event.preventDefault();
        }
    });
    toolbar.addEventListener('click', function (event) {
        var button = event.target.closest('button[data-cmd]');
        if (!button) {
            return;
        }
        restoreSelection();
        run(button.getAttribute('data-cmd'));
    });

    editable.addEventListener('input', function () {
        dirty = true;
        sync();
    });

    // paste as plain text, so no colours, fonts or scripts come in from other programs
    editable.addEventListener('paste', function (event) {
        var data = event.clipboardData || window.clipboardData;
        if (!data) {
            return;
        }
        event.preventDefault();
        document.execCommand('insertText', false, data.getData('text/plain'));
    });

    // Ctrl+S saves the draft
    document.addEventListener('keydown', function (event) {
        if ((event.ctrlKey || event.metaKey) && (event.key === 's' || event.key === 'S')) {
            var saveButton = form.querySelector('button[name="action"][value="save"]');
            if (saveButton) {
                event.preventDefault();
                saveButton.click();
            }
        }
    });

    form.addEventListener('input', function (event) {
        if (event.target !== editable) {
            dirty = true;
        }
    });

    form.addEventListener('submit', function (event) {
        sync();
        if (!editable.textContent.replace(/\s+/g, '')) {
            event.preventDefault();
            var id = 'contentEditorError';
            var msg = document.getElementById(id);
            if (!msg) {
                msg = document.createElement('p');
                msg.id = id;
                msg.className = 'pm-field-error';
                msg.setAttribute('role', 'alert');
                box.insertAdjacentElement('afterend', msg);
            }
            msg.textContent = 'The policy text can\'t be empty.';
            editable.focus();
            return;
        }
        dirty = false;
    });

    var deleteForm = document.getElementById('deleteDraftForm');
    if (deleteForm) {
        deleteForm.addEventListener('submit', function () { dirty = false; });
    }

    window.addEventListener('beforeunload', function (event) {
        if (dirty) {
            event.preventDefault();
            event.returnValue = '';
        }
    });
})();
