import { ApiError, notesApi } from './api.js';

const PAGE_SIZE = 20;
const dateFormat = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' });

const state = {
    page: 0,
    totalPages: 0,
    selected: null, // die gerade bearbeitete Note, oder null beim Anlegen
};

const $ = (selector) => document.querySelector(selector);
const el = {
    list: $('#note-list'),
    emptyList: $('#empty-list'),
    pager: $('#pager'),
    pageInfo: $('#page-info'),
    prevPage: $('#prev-page'),
    nextPage: $('#next-page'),
    newNote: $('#new-note'),
    form: $('#note-form'),
    formHeading: $('#form-heading'),
    title: $('#title'),
    content: $('#content'),
    timestamps: $('#timestamps'),
    save: $('#save'),
    delete: $('#delete'),
    conflict: $('#conflict'),
    reloadNote: $('#reload-note'),
    deleteModal: $('#delete-modal'),
    deleteModalNote: $('#delete-modal-note'),
    confirmDelete: $('#confirm-delete'),
    toasts: $('#toasts'),
};

// ---- Liste ----------------------------------------------------------------

async function loadList() {
    let result;
    try {
        result = await notesApi.list(state.page, PAGE_SIZE);
    } catch (error) {
        showError(error);
        return;
    }
    // Die aktuelle Seite kann nach dem Löschen leer sein: eine Seite zurück.
    if (result.content.length === 0 && state.page > 0) {
        state.page = result.totalPages > 0 ? result.totalPages - 1 : 0;
        return loadList();
    }
    state.totalPages = result.totalPages;
    renderList(result.content);
}

function renderList(notes) {
    el.list.replaceChildren(...notes.map(renderListItem));
    el.emptyList.classList.toggle('d-none', notes.length > 0);
    el.pager.classList.toggle('d-none', state.totalPages <= 1);
    el.pageInfo.textContent = `Seite ${state.page + 1} von ${state.totalPages}`;
    el.prevPage.disabled = state.page === 0;
    el.nextPage.disabled = state.page >= state.totalPages - 1;
}

function renderListItem(note) {
    const item = document.createElement('button');
    item.type = 'button';
    item.className = 'list-group-item list-group-item-action';
    item.classList.toggle('active', note.id === state.selected?.id);
    item.setAttribute('aria-current', String(note.id === state.selected?.id));

    const title = document.createElement('div');
    title.className = 'note-item-title fw-semibold';
    title.textContent = note.title;
    const changed = document.createElement('small');
    changed.textContent = `Geändert: ${formatDate(note.updatedAt)}`;

    item.append(title, changed);
    item.addEventListener('click', () => selectNote(note.id));
    return item;
}

// ---- Formular -------------------------------------------------------------

async function selectNote(id) {
    try {
        showNote(await notesApi.get(id));
    } catch (error) {
        showError(error);
        if (error.status === 404) {
            showNote(null);
            loadList();
        }
    }
}

function showNote(note) {
    state.selected = note;
    el.formHeading.textContent = note ? 'Note bearbeiten' : 'Neue Note';
    el.title.value = note?.title ?? '';
    el.content.value = note?.content ?? '';
    el.delete.classList.toggle('d-none', !note);
    el.timestamps.classList.toggle('d-none', !note);
    if (note) {
        el.timestamps.textContent =
            `Erstellt: ${formatDate(note.createdAt)} · Geändert: ${formatDate(note.updatedAt)}`;
    }
    el.conflict.classList.add('d-none');
    clearFieldErrors();
    loadList(); // aktualisiert Markierung und Reihenfolge
}

async function saveNote(event) {
    event.preventDefault();
    clearFieldErrors();
    const data = { title: el.title.value, content: el.content.value || null };

    el.save.disabled = true;
    try {
        const saved = state.selected
            ? await notesApi.update(state.selected.id, { ...data, version: state.selected.version })
            : await notesApi.create(data);
        if (!state.selected) {
            state.page = 0; // neue Notes stehen oben
        }
        showNote(saved);
        showToast('Gespeichert.', 'success');
    } catch (error) {
        if (error.status === 400 && Object.keys(error.fieldErrors).length > 0) {
            showFieldErrors(error.fieldErrors);
        } else if (error.status === 409) {
            el.conflict.classList.remove('d-none');
        } else {
            showError(error);
        }
    } finally {
        el.save.disabled = false;
    }
}

function showFieldErrors(errors) {
    for (const [field, message] of Object.entries(errors)) {
        const input = el.form.elements[field];
        const feedback = el.form.querySelector(`[data-error-for="${field}"]`);
        if (input && feedback) {
            input.classList.add('is-invalid');
            feedback.textContent = message;
        } else {
            showToast(message, 'danger');
        }
    }
    el.form.querySelector('.is-invalid')?.focus();
}

function clearFieldErrors() {
    el.form.querySelectorAll('.is-invalid').forEach((input) => input.classList.remove('is-invalid'));
}

// ---- Löschen --------------------------------------------------------------

function askDelete() {
    el.deleteModalNote.textContent = state.selected.title;
    bootstrap.Modal.getOrCreateInstance(el.deleteModal).show();
}

async function deleteSelected() {
    bootstrap.Modal.getOrCreateInstance(el.deleteModal).hide();
    try {
        await notesApi.delete(state.selected.id);
        showToast('Gelöscht.', 'success');
    } catch (error) {
        if (error.status !== 404) {
            showError(error);
            return;
        }
    }
    showNote(null);
}

// ---- Meldungen ------------------------------------------------------------

function showError(error) {
    const message = error instanceof ApiError ? error.message : 'Unerwarteter Fehler.';
    showToast(message, 'danger');
    if (!(error instanceof ApiError)) {
        console.error(error);
    }
}

function showToast(message, variant) {
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-bg-${variant} border-0`;
    toast.setAttribute('role', variant === 'danger' ? 'alert' : 'status');
    toast.setAttribute('aria-live', variant === 'danger' ? 'assertive' : 'polite');

    const wrapper = document.createElement('div');
    wrapper.className = 'd-flex';
    const body = document.createElement('div');
    body.className = 'toast-body';
    body.textContent = message;
    const close = document.createElement('button');
    close.type = 'button';
    close.className = 'btn-close btn-close-white me-2 m-auto';
    close.setAttribute('data-bs-dismiss', 'toast');
    close.setAttribute('aria-label', 'Schließen');

    wrapper.append(body, close);
    toast.append(wrapper);
    el.toasts.append(toast);
    toast.addEventListener('hidden.bs.toast', () => toast.remove());
    bootstrap.Toast.getOrCreateInstance(toast, { delay: variant === 'danger' ? 8000 : 3000 }).show();
}

function formatDate(iso) {
    return dateFormat.format(new Date(iso));
}

// ---- Start ----------------------------------------------------------------

el.newNote.addEventListener('click', () => {
    showNote(null);
    el.title.focus();
});
el.form.addEventListener('submit', saveNote);
el.delete.addEventListener('click', askDelete);
el.confirmDelete.addEventListener('click', deleteSelected);
el.reloadNote.addEventListener('click', () => selectNote(state.selected.id));
el.prevPage.addEventListener('click', () => {
    state.page--;
    loadList();
});
el.nextPage.addEventListener('click', () => {
    state.page++;
    loadList();
});

showNote(null);
