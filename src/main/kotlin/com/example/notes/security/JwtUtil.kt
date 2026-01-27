package com.example.notes.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil(

    @Value("\${JWT_SECRET}")
    secret: String,

    @Value("\${JWT_access-expiration-ms:60000}")
    private val accessExpirationMs: Long,

    @Value("\${JWT_refresh-expiration-ms:604800000}")
    private val refreshExpirationMs: Long
) {

    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateAccessToken(username: String): String =
        generateToken(username, "access", accessExpirationMs)

    fun generateRefreshToken(username: String): String =
        generateToken(username, "refresh", refreshExpirationMs)

    fun generateRefreshToken(username: String, expiresAt: Date): String =
        generateToken(username, "refresh", expiresAt)

    private fun generateToken(username: String, type: String, expirationMs: Long): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)

        return generateToken(username, type, expiry)
    }

    private fun generateToken(username: String, type: String, expiry: Date): String {
        val now = Date()
        return Jwts.builder()
            .claims()
            .subject(username)
            .add("type", type)
            .issuedAt(now)
            .expiration(expiry)
            .and()
            .signWith(key, Jwts.SIG.HS256)
            .compact()
    }

    fun extractUsername(token: String): String =
        extractClaims(token).subject

    fun extractTokenType(token: String): String? =
        extractClaims(token)["type"] as? String

    fun extractExpiration(token: String): Date =
        extractClaims(token).expiration

    fun isAccessTokenValid(token: String): Boolean = isTokenValid(token, "access")

    fun isRefreshTokenValid(token: String): Boolean = isTokenValid(token, "refresh")

    private fun isTokenValid(token: String, expectedType: String): Boolean =
        try {
            val claims = extractClaims(token) // если битый — exception
            claims["type"] == expectedType
        } catch (_: Exception) {
            false
        }

    // 📦 Claims
    private fun extractClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}
