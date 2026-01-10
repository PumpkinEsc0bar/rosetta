
package com.example.notes.model.dto

import jakarta.validation.constraints.NotBlank

data class NoteRequest(
    @field:NotBlank val title: String,
    @field:NotBlank val content: String
)
