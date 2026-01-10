
package com.example.notes.repository

import com.example.notes.model.Note
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NoteRepository : JpaRepository<Note, Int> {
    fun findAllByUserId(userId: Int): List<Note>
}
