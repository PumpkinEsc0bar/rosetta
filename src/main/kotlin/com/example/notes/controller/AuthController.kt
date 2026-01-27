package com.example.notes.controller

import com.example.notes.model.dto.LoginRequest
import com.example.notes.model.dto.RefreshRequest
import com.example.notes.model.dto.RegisterRequest
import com.example.notes.security.SuspiciousRequestTracker
import com.example.notes.service.RefreshTokenService
import com.example.notes.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/auth")
class AuthController(
    private val users: UserService,
    private val refreshTokens: RefreshTokenService,
    private val suspiciousRequests: SuspiciousRequestTracker
) {
    private val log = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/register")
    fun register(@Valid @RequestBody r: RegisterRequest): Map<String, String> {
        val user = users.register(r.username, r.email, r.password)
        val tokens = refreshTokens.issueForUser(user)
        return mapOf(
            "accessToken" to tokens.accessToken,
            "refreshToken" to tokens.refreshToken
        )
    }

    @PostMapping("/login")
    fun login(@RequestBody r: LoginRequest, request: HttpServletRequest): Map<String, String> {
        try {
            val user = users.authenticate(r.username, r.password)
            val tokens = refreshTokens.issueForUser(user)
            return mapOf(
                "accessToken" to tokens.accessToken,
                "refreshToken" to tokens.refreshToken
            )
        } catch (_: RuntimeException) {
            log.warn(
                "Failed login attempt remoteIp={} userAgent={}",
                request.remoteAddr,
                request.getHeader("User-Agent")
            )
            if (suspiciousRequests.record("login:${request.remoteAddr}")) {
                log.warn(
                    "Suspicious repeated login failures remoteIp={} path={}",
                    request.remoteAddr,
                    request.requestURI
                )
            }
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials")
        }
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody r: RefreshRequest, request: HttpServletRequest): Map<String, String> {
        try {
            val tokens = refreshTokens.rotate(r.refreshToken)
            return mapOf(
                "accessToken" to tokens.accessToken,
                "refreshToken" to tokens.refreshToken
            )
        } catch (ex: ResponseStatusException) {
            if (ex.statusCode == HttpStatus.UNAUTHORIZED) {
                log.warn(
                    "Invalid refresh token remoteIp={} userAgent={}",
                    request.remoteAddr,
                    request.getHeader("User-Agent")
                )
                if (suspiciousRequests.record("refresh:${request.remoteAddr}")) {
                    log.warn(
                        "Suspicious repeated refresh failures remoteIp={} path={}",
                        request.remoteAddr,
                        request.requestURI
                    )
                }
            }
            throw ex
        }
    }
}
