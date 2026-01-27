package com.example.notes.service

import com.example.notes.model.RefreshToken
import com.example.notes.model.User
import com.example.notes.repository.RefreshTokenRepository
import com.example.notes.security.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.Date

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)

@Service
class RefreshTokenService(
    private val repo: RefreshTokenRepository,
    private val jwt: JwtUtil
) {
    fun issueForUser(user: User): TokenPair {
        val accessToken = jwt.generateAccessToken(user.username)
        val refreshToken = jwt.generateRefreshToken(user.username)
        saveRefreshToken(user.id, refreshToken)
        return TokenPair(accessToken = accessToken, refreshToken = refreshToken)
    }

    @Transactional
    fun rotate(refreshToken: String): TokenPair {
        if (!jwt.isRefreshTokenValid(refreshToken)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")
        }

        val now = nowMs()
        val stored = repo.findByToken(refreshToken)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")

        if (stored.revokedAt != null || stored.expiresAt <= now) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")
        }

        val username = jwt.extractUsername(refreshToken)
        val accessToken = jwt.generateAccessToken(username)
        val newRefreshToken = jwt.generateRefreshToken(username, Date(stored.expiresAt))

        val revoked = repo.revokeIfActive(refreshToken, now, now)
        if (revoked == 0) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")
        }
        saveRefreshToken(stored.userId, newRefreshToken)

        return TokenPair(accessToken = accessToken, refreshToken = newRefreshToken)
    }

    private fun saveRefreshToken(userId: Int, token: String) {
        val expiresAt = jwt.extractExpiration(token).time
        val refreshToken = RefreshToken(
            userId = userId,
            token = token,
            expiresAt = expiresAt,
            createdAt = nowMs()
        )
        repo.save(refreshToken)
    }

    private fun nowMs(): Long = Date().time
}
