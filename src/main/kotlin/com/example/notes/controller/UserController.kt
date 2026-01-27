package com.example.notes.controller

import com.example.notes.model.dto.UserSummary
import com.example.notes.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management API")
class UserController(
    private val users: UserService
) {
    @GetMapping
    @Operation(summary = "List users (admin only)")
    fun listUsers(): List<UserSummary> {
        val username = SecurityContextHolder.getContext().authentication.name
        return users.listUsers(username)
    }

    @PostMapping("/me/admin")
    @Operation(summary = "Promote current user to admin")
    fun becomeAdmin(): UserSummary {
        val username = SecurityContextHolder.getContext().authentication.name
        return users.promoteToAdmin(username)
    }

    @PostMapping("/me/user")
    @Operation(summary = "Demote current user to user")
    fun becomeUser(): UserSummary {
        val username = SecurityContextHolder.getContext().authentication.name
        return users.demoteToUser(username)
    }
}
