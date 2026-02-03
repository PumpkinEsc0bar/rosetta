package com.example.notes.service

import com.example.notes.model.Role
import com.example.notes.model.User
import com.example.notes.model.dto.UserSummary
import com.example.notes.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
open class UserService(
    private val repo: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun register(username: String, email: String, password: String): User =
        repo.save(
            User(
                username = username,
                email = email,
                password = passwordEncoder.encode(password)
                    ?: throw IllegalStateException("Password encoding failed"),
                role = Role.USER
            )
        )

    fun listUsers(requesterUsername: String): List<UserSummary> {
        val requester = repo.findByUsername(requesterUsername)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        if (requester.role != Role.ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden")
        }
        return repo.findAll().map { user ->
            UserSummary(
                id = user.id,
                username = user.username,
                email = user.email,
                role = user.role
            )
        }
    }

    fun promoteToAdmin(username: String): UserSummary {
        val user = repo.findByUsername(username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        val updated = if (user.role == Role.ADMIN) user else user.copy(role = Role.ADMIN)
        val saved = repo.save(updated)
        return UserSummary(
            id = saved.id,
            username = saved.username,
            email = saved.email,
            role = saved.role
        )
    }

    fun demoteToUser(username: String): UserSummary {
        val user = repo.findByUsername(username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        val updated = if (user.role == Role.USER) user else user.copy(role = Role.USER)
        val saved = repo.save(updated)
        return UserSummary(
            id = saved.id,
            username = saved.username,
            email = saved.email,
            role = saved.role
        )
    }

    fun authenticate(username: String, raw: String): User = repo.findByUsername(username)?.takeIf {
        passwordEncoder.matches(raw, it.password)
    }?.let {
        User(
            id = it.id,
            username = it.username,
            email = it.email,
            role = it.role
        )
    } ?: throw RuntimeException("Bad credentials")

    fun findByUsername(username: String): User? = repo.findByUsername(username)

    fun findById(id: Int): User? = repo.findById(id).orElse(null)
}
