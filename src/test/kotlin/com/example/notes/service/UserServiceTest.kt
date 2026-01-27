package com.example.notes.service

import com.example.notes.model.Role
import com.example.notes.model.User
import com.example.notes.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.server.ResponseStatusException

class UserServiceTest {
    private val repo: UserRepository = mock()
    private val encoder: PasswordEncoder = mock()
    private val service = UserService(repo, encoder)

    @Test
    fun register_encodesPasswordAndSavesUser() {
        whenever(encoder.encode("password123")).thenReturn("hashed")
        whenever(repo.save(any())).thenAnswer { (it.arguments[0] as User).copy(id = 1) }

        val created = service.register("alice", "alice@example.com", "password123")

        val captor = argumentCaptor<User>()
        verify(repo).save(captor.capture())
        val saved = captor.firstValue

        assertEquals("alice", saved.username)
        assertEquals("alice@example.com", saved.email)
        assertEquals("hashed", saved.password)
        assertEquals(Role.USER, saved.role)
        assertEquals(1, created.id)
    }

    @Test
    fun authenticate_succeedsWithCorrectCredentials() {
        val stored = User(
            id = 7,
            username = "bob",
            email = "bob@example.com",
            password = "hashed",
            role = Role.USER
        )
        whenever(repo.findByUsername("bob")).thenReturn(stored)
        whenever(encoder.matches("secret", "hashed")).thenReturn(true)

        val authed = service.authenticate("bob", "secret")

        assertEquals(7, authed.id)
        assertEquals("bob", authed.username)
        assertEquals("bob@example.com", authed.email)
        assertEquals(Role.USER, authed.role)
        assertEquals("", authed.password)
    }

    @Test
    fun authenticate_failsWithWrongPassword() {
        val stored = User(
            id = 7,
            username = "bob",
            email = "bob@example.com",
            password = "hashed",
            role = Role.USER
        )
        whenever(repo.findByUsername("bob")).thenReturn(stored)
        whenever(encoder.matches("wrong", "hashed")).thenReturn(false)

        assertThrows(RuntimeException::class.java) {
            service.authenticate("bob", "wrong")
        }
    }

    @Test
    fun listUsers_forbiddenForNonAdmin() {
        val requester = User(
            id = 1,
            username = "alice",
            email = "alice@example.com",
            password = "hashed",
            role = Role.USER
        )
        whenever(repo.findByUsername("alice")).thenReturn(requester)

        val ex = assertThrows(ResponseStatusException::class.java) {
            service.listUsers("alice")
        }

        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun listUsers_returnsSummariesForAdmin() {
        val admin = User(
            id = 1,
            username = "admin",
            email = "admin@example.com",
            password = "hashed",
            role = Role.ADMIN
        )
        val user = User(
            id = 2,
            username = "bob",
            email = "bob@example.com",
            password = "hashed2",
            role = Role.USER
        )
        whenever(repo.findByUsername("admin")).thenReturn(admin)
        whenever(repo.findAll()).thenReturn(listOf(admin, user))

        val summaries = service.listUsers("admin")

        assertEquals(2, summaries.size)
        assertEquals("admin", summaries[0].username)
        assertEquals("bob", summaries[1].username)
    }
}
