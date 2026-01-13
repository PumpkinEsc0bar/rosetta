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

    @Value("\${JWT_expiration-ms:3600000}")
    private val expirationMs: Long
) {

    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(username: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)

        return Jwts.builder()
            .claims()
            .subject(username)
            .issuedAt(now)
            .expiration(expiry)
            .and()
            .signWith(key, Jwts.SIG.HS256)
            .compact()
    }

    fun extractUsername(token: String): String =
        extractClaims(token).subject

   fun isTokenValid(token: String): Boolean =
        try {
            extractClaims(token) // если битый — exception
            true
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
