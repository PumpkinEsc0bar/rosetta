package com.example.notes.service

import com.example.notes.model.Role
import com.example.notes.model.User
import com.example.notes.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

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
                password = passwordEncoder.encode(password),
                role = Role.USER
            )
        )

    fun authenticate(username: String, raw: String): User = repo.findByUsername(username)?.takeIf {
        passwordEncoder.matches(raw, it.password)
    }?.also {
        User(
            id = it.id,
            username = it.username,
            email = it.email,
            role = it.role,
        )
    } ?: throw RuntimeException("Bad credentials")

    fun findByUsername(username: String): User? = repo.findByUsername(username)

    fun findById(id: Int): User? = repo.findById(id).orElse(null)
}
