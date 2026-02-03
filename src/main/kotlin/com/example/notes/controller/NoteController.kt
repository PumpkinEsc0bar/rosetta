package com.example.notes.controller

import com.example.notes.model.Note
import com.example.notes.model.dto.NoteRequest
import com.example.notes.service.NoteService
import com.example.notes.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/notes")
@Tag(name = "Notes", description = "Notes management API")
class NoteController(
    private val notes: NoteService,
    private val users: UserService
) {

    @GetMapping
    @Operation(summary = "Get all notes")
    fun all() = notes.all(currentUserId())

    @GetMapping("/{id}")
    @Operation(summary = "Get note")
    fun get(@PathVariable id: Int): Note = notes.get(id, currentUserId())

    @PostMapping
    @Operation(summary = "Create note")
    fun create(@Valid @RequestBody r: NoteRequest): Note? =
        notes.create(r.title, r.content, currentUserId())

    @PutMapping("/{id}")
    @Operation(summary = "Update note")
    fun update(@PathVariable id: Int, @Valid @RequestBody r: NoteRequest): Note =
        notes.update(id, r.title, r.content, currentUserId())

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete note")
    fun delete(@PathVariable id: Int) {
        notes.delete(id, currentUserId())
    }

    private fun currentUserId(): Int {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated request")
        val username = authentication.name
        val user = users.findByUsername(username)
            ?: throw RuntimeException("User not found")
        return user.id
    }
}
