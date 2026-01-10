
package com.example.notes.controller

import com.example.notes.model.dto.LoginRequest
import com.example.notes.model.dto.RegisterRequest
import com.example.notes.security.JwtUtil
import com.example.notes.service.UserService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val users: UserService,
    private val jwt: JwtUtil
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody r: RegisterRequest) = users.register(r.username, r.email, r.password)

    @PostMapping("/login")
    fun login(@RequestBody r: LoginRequest): Map<String, String> {
        return with(users.authenticate(r.username, r.password)) {
            mapOf("token" to jwt.generate(username))
        }
    }
}
