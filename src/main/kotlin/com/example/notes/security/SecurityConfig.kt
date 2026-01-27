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
import org.slf4j.LoggerFactory
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.csrf.CookieCsrfTokenRepository

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtUtil: JwtUtil
) {
    private val log = LoggerFactory.getLogger(SecurityConfig::class.java)

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
            .csrf { csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .headers {
                it.contentTypeOptions { }
                it.frameOptions { frame -> frame.deny() }
                it.referrerPolicy { policy ->
                    policy.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
                }
                it.contentSecurityPolicy { csp ->
                    csp.policyDirectives(
                        "default-src 'self'; " +
                            "script-src 'self' 'unsafe-inline'; " +
                            "style-src 'self' 'unsafe-inline'; " +
                            "img-src 'self' data:; " +
                            "connect-src 'self'; " +
                            "base-uri 'self'; " +
                            "form-action 'self'; " +
                            "frame-ancestors 'none'"
                    )
                }
            }
            .exceptionHandling {
                it.authenticationEntryPoint { request, response, _ ->
                    log.warn(
                        "Unauthorized request method={} path={} remoteIp={}",
                        request.method,
                        request.requestURI,
                        request.remoteAddr
                    )
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                }
                it.accessDeniedHandler { request, response, _ ->
                    log.warn(
                        "Forbidden request method={} path={} remoteIp={}",
                        request.method,
                        request.requestURI,
                        request.remoteAddr
                    )
                    response.sendError(HttpServletResponse.SC_FORBIDDEN)
                }
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                // PUBLIC
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

                // API JWT
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
