package com.example.notes.controller

import com.example.notes.repository.UserRepository
import com.example.notes.service.NoteService
import com.example.notes.service.UserService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class UiController(
    private val userService: UserService,
    private val noteService: NoteService
) {

    @GetMapping("/", "/index.html")
    fun index(): String = "index"

    @GetMapping("/notes")
    fun notes(model: Model): String {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userService.findByUsername(username) ?: throw RuntimeException("User not found")
        val notes = noteService.all(user.id)
        model.addAttribute("notes", notes)
        return "notes"
    }
}
