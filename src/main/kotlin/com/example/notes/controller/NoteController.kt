package com.example.notes.controller

import com.example.notes.model.Note
import com.example.notes.model.dto.NoteRequest
import com.example.notes.service.NoteService
import com.example.notes.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

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

    @PostMapping
    @Operation(summary = "Create note")
    fun create(@Valid @RequestBody r: NoteRequest): Note? =
        notes.create(r.title, r.content, currentUserId())

    private fun currentUserId(): Int {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = users.findByUsername(username)
            ?: throw RuntimeException("User not found")
        return user.id
    }}
