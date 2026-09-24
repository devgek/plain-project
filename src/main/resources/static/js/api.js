// Zugriff auf die REST-API. Fehlerantworten (RFC 9457 ProblemDetail) werden als ApiError geworfen.

export class ApiError extends Error {
    constructor(status, problem) {
        super(problem?.detail || `Anfrage fehlgeschlagen (HTTP ${status})`);
        this.status = status;
        this.fieldErrors = problem?.errors || {};
    }
}

async function request(method, path, body) {
    const options = { method, headers: { Accept: 'application/json' } };
    if (body !== undefined) {
        options.headers['Content-Type'] = 'application/json';
        options.body = JSON.stringify(body);
    }
    let response;
    try {
        response = await fetch(path, options);
    } catch {
        throw new ApiError(0, { detail: 'Der Server ist nicht erreichbar.' });
    }
    if (!response.ok) {
        const problem = await response.json().catch(() => null);
        throw new ApiError(response.status, problem);
    }
    return response.status === 204 ? null : response.json();
}

export const notesApi = {
    list: (page, size) => request('GET', `api/notes?page=${page}&size=${size}`),
    get: (id) => request('GET', `api/notes/${id}`),
    create: (note) => request('POST', 'api/notes', note),
    update: (id, note) => request('PUT', `api/notes/${id}`, note),
    delete: (id) => request('DELETE', `api/notes/${id}`),
};
