package com.example.notes.model

import jakarta.persistence.*

@Entity
@Table(name = "notes")
data class Note(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INTEGER")
    val id: Int = 0,

    val title: String,
    val content: String,

    @Column(name = "user_id", columnDefinition = "INTEGER")
    val userId: Int
)
