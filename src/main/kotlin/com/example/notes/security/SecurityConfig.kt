package com.example.notes.security

import com.example.notes.service.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtUtil: JwtUtil
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(userService: UserService): UserDetailsService = UserDetailsService { username ->
        val user = userService.findByUsername(username)
            ?: throw RuntimeException("User not found")
        User(
            user.username,
            user.password,
            emptyList()
        )
    }

    @Bean
    fun jwtAuthFilter(userDetailsService: UserDetailsService): JwtAuthFilter =
        JwtAuthFilter(jwtUtil, userDetailsService)


    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtAuthFilter: JwtAuthFilter): SecurityFilterChain {

        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                // 🔓 PUBLIC
                it.requestMatchers(
                    "/", "/index.html",
                    "/css/**", "/js/**", "/favicon.ico",

                    // swagger / docs
                    "/docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",

                    // auth
                    "/auth/**"
                ).permitAll()

                // 🔒 API JWT
                it.requestMatchers("/notes/**", "/api/notes/**").authenticated()//                it.anyRequest().permitAll()
                it.anyRequest().authenticated()
            }

            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }
}
