package com.example.notes.service

import com.example.notes.model.User
import com.example.notes.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
open class UserService(
    private val repo: UserRepository,
    private val encoder: PasswordEncoder
) {
    fun register(username: String, email: String, password: String): User =
        repo.save(User(username = username, email = email, password = encoder.encode(password)))

    fun authenticate(username: String, raw: String): User = repo.findByUsername(username).takeIf {
        encoder.matches(raw, it.password)
    } ?: throw RuntimeException("Bad credentials")
}
