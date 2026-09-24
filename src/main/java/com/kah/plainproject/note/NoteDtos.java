package com.kah.plainproject.note;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.data.domain.Page;

final class NoteDtos {

    private NoteDtos() {
    }

    record CreateNoteRequest(
            @NotBlank @Size(max = 200) String title,
            String content) {
    }

    record UpdateNoteRequest(
            @NotBlank @Size(max = 200) String title,
            String content,
            @NotNull Long version) {
    }

    record NoteResponse(Long id, Long version, String title, String content, Instant createdAt, Instant updatedAt) {

        static NoteResponse of(Note note) {
            return new NoteResponse(note.id(), note.version(), note.title(), note.content(), note.createdAt(),
                    note.updatedAt());
        }
    }

    /** Stabiles JSON-Format für Seiten, statt Springs {@code PageImpl} direkt zu serialisieren. */
    record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

        static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> mapper) {
            return new PageResponse<>(page.getContent().stream().map(mapper).toList(), page.getNumber(),
                    page.getSize(), page.getTotalElements(), page.getTotalPages());
        }
    }
}
