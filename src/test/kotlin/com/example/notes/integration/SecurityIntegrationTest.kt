package com.example.notes.integration

import com.example.notes.model.Role
import com.example.notes.model.User
import com.example.notes.repository.NoteRepository
import com.example.notes.repository.UserRepository
import com.example.notes.security.JwtUtil
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Date

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "DB_URL=jdbc:sqlite:/tmp/notes-it.db",
        "JWT_SECRET=tests_need_a_long_secret_key_for_hs256_1234567890",
        "spring.flyway.enabled=false"
    ]
)
class SecurityIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var noteRepository: NoteRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    @Value("\${JWT_SECRET}")
    private lateinit var jwtSecret: String

    @BeforeEach
    fun setUp() {
        noteRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun authorizedUser_accessAllowed() {
        val user = createUser("alice", "password123")
        val token = jwtUtil.generateAccessToken(user.username)

        mockMvc.perform(
            get("/api/notes")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)
    }

    @Test
    fun unauthorizedUser_accessDenied() {
        mockMvc.perform(get("/api/notes"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun invalidToken_requestRejected() {
        mockMvc.perform(
            get("/api/notes")
                .header("Authorization", "Bearer not-a-real-token")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun expiredToken_requestRejected() {
        val expiredToken = buildExpiredToken("alice")

        mockMvc.perform(
            get("/api/notes")
                .header("Authorization", "Bearer $expiredToken")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun csrf_missingToken_rejected() {
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"alice","password":"password123"}""")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun csrf_validToken_allowsRequest() {
        createUser("alice", "password123")

        mockMvc.perform(
            post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"alice","password":"password123"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
    }

    private fun createUser(username: String, rawPassword: String): User =
        userRepository.save(
            User(
                username = username,
                email = "$username@example.com",
                password = passwordEncoder.encode(rawPassword),
                role = Role.USER
            )
        )

    private fun buildExpiredToken(username: String): String {
        val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        val now = System.currentTimeMillis()
        return Jwts.builder()
            .subject(username)
            .claim("type", "access")
            .issuedAt(Date(now - 10_000))
            .expiration(Date(now - 1_000))
            .signWith(key, Jwts.SIG.HS256)
            .compact()
    }
}
