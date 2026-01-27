
package com.example.notes.service

import com.example.notes.model.Note
import com.example.notes.repository.NoteRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class NoteService(private val repo: NoteRepository) {

    fun all(userId: Int) = repo.findAllByUserId(userId)

    fun create(title: String, content: String, userId: Int) =
        repo.save(Note(title = title, content = content, userId = userId))

    fun get(id: Int, userId: Int): Note = ownedNote(id, userId)

    fun update(id: Int, title: String, content: String, userId: Int): Note {
        val existing = ownedNote(id, userId)
        return repo.save(existing.copy(title = title, content = content))
    }

    fun delete(id: Int, userId: Int) {
        val existing = ownedNote(id, userId)
        repo.delete(existing)
    }

    private fun ownedNote(id: Int, userId: Int): Note =
        repo.findByIdAndUserId(id, userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found")
}
