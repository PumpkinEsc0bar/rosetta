
package com.example.notes.service

import com.example.notes.model.Note
import com.example.notes.repository.NoteRepository
import org.springframework.stereotype.Service

@Service
class NoteService(private val repo: NoteRepository) {

    fun all(userId: Int) = repo.findAllByUserId(userId)

    fun create(title: String, content: String, userId: Int) =
        repo.save(Note(title = title, content = content, userId = userId))
}
