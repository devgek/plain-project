package com.kah.plainproject.note;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

interface NoteRepository extends ListCrudRepository<Note, Long>, PagingAndSortingRepository<Note, Long> {
}
