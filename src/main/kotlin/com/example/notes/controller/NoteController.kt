
package com.example.notes.controller

import com.example.notes.model.Note
import com.example.notes.model.dto.NoteRequest
import com.example.notes.service.NoteService
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/notes")
class NoteController(private val notes: NoteService) {

    @GetMapping
    fun all() = notes.all(currentUserId())

    @PostMapping
    fun create(@Valid @RequestBody r: NoteRequest): Note? =
        notes.create(r.title, r.content, currentUserId())

    fun currentUserId(): Int = 1 // simplified for lab
}
