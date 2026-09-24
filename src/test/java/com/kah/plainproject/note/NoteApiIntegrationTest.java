package com.kah.plainproject.note;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import com.kah.plainproject.TestcontainersConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class NoteApiIntegrationTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    NoteRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void createsAndReadsANote() {
        MvcTestResult created = create("Einkauf", "Milch, Brot");

        assertThat(created).hasStatus(HttpStatus.CREATED);
        long id = idOf(created);
        assertThat(created.getResponse().getHeader("Location")).isEqualTo("/api/notes/" + id);
        assertThat(created).bodyJson().extractingPath("$.version").isEqualTo(0);
        assertThat(created).bodyJson().extractingPath("$.createdAt").isNotNull();

        MvcTestResult read = mvc.get().uri("/api/notes/{id}", id).exchange();
        assertThat(read).hasStatusOk();
        assertThat(read).bodyJson().extractingPath("$.title").isEqualTo("Einkauf");
        assertThat(read).bodyJson().extractingPath("$.content").isEqualTo("Milch, Brot");
    }

    @Test
    void listsNotesLastChangedFirstInPages() {
        long first = idOf(create("Erste", null));
        idOf(create("Zweite", null));
        idOf(create("Dritte", null));
        update(first, 0, "Erste, geändert");

        MvcTestResult page0 = mvc.get().uri("/api/notes?page=0&size=2").exchange();
        assertThat(page0).hasStatusOk();
        assertThat(page0).bodyJson().extractingPath("$.content[*].title")
                .asArray().containsExactly("Erste, geändert", "Dritte");
        assertThat(page0).bodyJson().extractingPath("$.totalElements").isEqualTo(3);
        assertThat(page0).bodyJson().extractingPath("$.totalPages").isEqualTo(2);

        MvcTestResult page1 = mvc.get().uri("/api/notes?page=1&size=2").exchange();
        assertThat(page1).bodyJson().extractingPath("$.content[*].title").asArray().containsExactly("Zweite");
    }

    @Test
    void rejectsPageSizeOutOfRange() {
        assertThat(mvc.get().uri("/api/notes?size=101")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updatesANoteAndIncrementsItsVersion() {
        long id = idOf(create("Alt", "alt"));

        MvcTestResult updated = update(id, 0, "Neu");

        assertThat(updated).hasStatusOk();
        assertThat(updated).bodyJson().extractingPath("$.title").isEqualTo("Neu");
        assertThat(updated).bodyJson().extractingPath("$.version").isEqualTo(1);
    }

    @Test
    void rejectsUpdateBasedOnStaleVersion() {
        long id = idOf(create("Titel", null));
        update(id, 0, "Von Tab A");

        MvcTestResult stale = update(id, 0, "Von Tab B");

        assertThat(stale).hasStatus(HttpStatus.CONFLICT);
        assertThat(stale).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(repository.findById(id)).get().extracting(Note::title).isEqualTo("Von Tab A");
    }

    @Test
    void rejectsInvalidNoteWithFieldErrors() {
        MvcTestResult result = mvc.post().uri("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"   \"}")
                .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.errors.title").isNotNull();
    }

    @Test
    void rejectsTitleLongerThan200Characters() {
        assertThat(create("x".repeat(201), null)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deletesANotePermanently() {
        long id = idOf(create("Weg damit", null));

        assertThat(mvc.delete().uri("/api/notes/{id}", id)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(mvc.get().uri("/api/notes/{id}", id)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void answersUnknownNoteWith404() {
        assertThat(mvc.get().uri("/api/notes/999999")).hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(update(999999, 0, "x")).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(mvc.delete().uri("/api/notes/999999")).hasStatus(HttpStatus.NOT_FOUND);
    }

    private MvcTestResult create(String title, String content) {
        return mvc.post().uri("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(title, content, null))
                .exchange();
    }

    private MvcTestResult update(long id, long version, String title) {
        return mvc.put().uri("/api/notes/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(title, null, version))
                .exchange();
    }

    private static String json(String title, String content, Long version) {
        return """
                {"title": %s, "content": %s, "version": %s}"""
                .formatted(quote(title), quote(content), version);
    }

    private static String quote(String value) {
        return value == null ? "null" : "\"" + value.replace("\"", "\\\"") + "\"";
    }

    private static long idOf(MvcTestResult result) {
        try {
            return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
