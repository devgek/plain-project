package com.kah.plainproject.note;

import java.net.URI;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import com.kah.plainproject.note.NoteDtos.CreateNoteRequest;
import com.kah.plainproject.note.NoteDtos.NoteResponse;
import com.kah.plainproject.note.NoteDtos.PageResponse;
import com.kah.plainproject.note.NoteDtos.UpdateNoteRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes")
class NoteController {

    private final NoteService service;

    NoteController(NoteService service) {
        this.service = service;
    }

    @GetMapping
    PageResponse<NoteResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return PageResponse.of(service.list(page, size), NoteResponse::of);
    }

    @GetMapping("/{id}")
    NoteResponse get(@PathVariable long id) {
        return NoteResponse.of(service.get(id));
    }

    @PostMapping
    ResponseEntity<NoteResponse> create(@Valid @RequestBody CreateNoteRequest request) {
        Note note = service.create(request.title(), request.content());
        return ResponseEntity.created(URI.create("/api/notes/" + note.id())).body(NoteResponse.of(note));
    }

    @PutMapping("/{id}")
    NoteResponse update(@PathVariable long id, @Valid @RequestBody UpdateNoteRequest request) {
        return NoteResponse.of(service.update(id, request.version(), request.title(), request.content()));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
