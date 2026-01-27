package com.example.notes.validation

import com.example.notes.model.dto.NoteRequest
import com.example.notes.model.dto.RegisterRequest
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ValidationTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun registerRequest_acceptsValidInput() {
        val req = RegisterRequest(
            username = "validuser",
            email = "user@example.com",
            password = "password123"
        )

        val violations = validator.validate(req)

        assertEquals(0, violations.size)
    }

    @Test
    fun registerRequest_rejectsBlankUsername() {
        val req = RegisterRequest(
            username = "",
            email = "user@example.com",
            password = "password123"
        )

        val violations = validator.validate(req)

        assertEquals(1, violations.size)
    }

    @Test
    fun registerRequest_rejectsInvalidEmail() {
        val req = RegisterRequest(
            username = "user",
            email = "not-an-email",
            password = "password123"
        )

        val violations = validator.validate(req)

        assertEquals(1, violations.size)
    }

    @Test
    fun registerRequest_rejectsShortPassword() {
        val req = RegisterRequest(
            username = "user",
            email = "user@example.com",
            password = "short"
        )

        val violations = validator.validate(req)

        assertEquals(1, violations.size)
    }

    @Test
    fun noteRequest_rejectsBlankFields() {
        val req = NoteRequest(
            title = "",
            content = ""
        )

        val violations = validator.validate(req)

        assertEquals(2, violations.size)
    }
}
