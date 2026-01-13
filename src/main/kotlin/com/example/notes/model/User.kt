package com.example.notes.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INTEGER")
    val id: Int = 0,

    val username: String,

    val email: String,

    val password: String = "",

    @Enumerated(EnumType.STRING)
    val role: Role = Role.USER
)

enum class Role {
    USER,
    ADMIN
}
