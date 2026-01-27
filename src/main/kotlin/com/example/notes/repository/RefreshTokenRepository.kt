package com.example.notes.repository

import com.example.notes.model.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Int> {
    fun findByToken(token: String): RefreshToken?

    @Modifying
    @Query(
        """
        update RefreshToken rt
        set rt.revokedAt = :revokedAt
        where rt.token = :token
          and rt.revokedAt is null
          and rt.expiresAt > :now
        """
    )
    fun revokeIfActive(
        @Param("token") token: String,
        @Param("revokedAt") revokedAt: Long,
        @Param("now") now: Long
    ): Int
}
