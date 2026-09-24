package com.kah.plainproject.note;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class NoteService {

    private static final Sort LAST_CHANGED_FIRST = Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id"));

    private final NoteRepository repository;

    NoteService(NoteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    Page<Note> list(int page, int size) {
        return repository.findAll(PageRequest.of(page, size, LAST_CHANGED_FIRST));
    }

    @Transactional(readOnly = true)
    Note get(long id) {
        return repository.findById(id).orElseThrow(() -> new NoteNotFoundException(id));
    }

    Note create(String title, String content) {
        return repository.save(Note.create(title, content));
    }

    /**
     * @throws org.springframework.dao.OptimisticLockingFailureException wenn die Note seit
     *         {@code expectedVersion} geändert wurde
     */
    Note update(long id, long expectedVersion, String title, String content) {
        return repository.save(get(id).change(expectedVersion, title, content));
    }

    void delete(long id) {
        if (!repository.existsById(id)) {
            throw new NoteNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
