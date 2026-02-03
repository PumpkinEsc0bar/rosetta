package com.example.notes.controller

import com.example.notes.service.NoteService
import com.example.notes.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.server.ResponseStatusException

@Controller
class UiController(
    private val userService: UserService,
    private val noteService: NoteService
) {

    @GetMapping("/", "/index.html")
    fun index(): String = "index"

    @GetMapping("/notes")
    fun notes(model: Model): String {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated request")
        val username = authentication.name
        val user = userService.findByUsername(username) ?: throw RuntimeException("User not found")
        val notes = noteService.all(user.id)
        model.addAttribute("notes", notes)
        return "notes"
    }
}
