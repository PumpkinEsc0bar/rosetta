package com.example.notes.model.dto

import com.example.notes.model.Role

data class UserSummary(
    val id: Int,
    val username: String,
    val email: String,
    val role: Role
)
