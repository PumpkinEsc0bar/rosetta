package com.example.notes.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "refresh_tokens")
data class RefreshToken(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INTEGER")
    val id: Int = 0,

    @Column(name = "user_id", columnDefinition = "INTEGER")
    val userId: Int,

    @Column(unique = true)
    val token: String,

    @Column(name = "expires_at", columnDefinition = "INTEGER")
    val expiresAt: Long,

    @Column(name = "created_at", columnDefinition = "INTEGER")
    val createdAt: Long,

    @Column(name = "revoked_at", columnDefinition = "INTEGER")
    val revokedAt: Long? = null
)
