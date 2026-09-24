package com.kah.plainproject.note;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

/** Wird von Spring MVC als 404-{@code ProblemDetail} ausgeliefert. */
class NoteNotFoundException extends ErrorResponseException {

    NoteNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND);
        setDetail("Note " + id + " wurde nicht gefunden.");
    }
}
