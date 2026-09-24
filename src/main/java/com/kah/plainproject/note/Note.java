package com.kah.plainproject.note;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

/**
 * Aggregat-Wurzel. {@code version} dient dem Optimistic Locking: Spring Data JDBC aktualisiert nur,
 * wenn die Version in der Datenbank noch der hier gehaltenen entspricht.
 */
public record Note(
        @Id Long id,
        @Version Long version,
        String title,
        String content,
        @CreatedDate Instant createdAt,
        @LastModifiedDate Instant updatedAt) {

    static Note create(String title, String content) {
        return new Note(null, null, title, content, null, null);
    }

    /** Geänderte Kopie, bezogen auf die Version, die der Client zuletzt gesehen hat. */
    Note change(long expectedVersion, String title, String content) {
        return new Note(id, expectedVersion, title, content, createdAt, updatedAt);
    }
}
