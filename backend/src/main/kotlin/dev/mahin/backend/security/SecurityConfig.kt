package dev.mahin.backend.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val rateLimitFilter: RateLimitFilter,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/actuator/health", "/actuator/health/**")
                    .permitAll()
                    .requestMatchers("/v3/api-docs", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/v1/meta")
                    .permitAll()
                    .requestMatchers("/v1/media/**")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/v1/content/**",
                    ).permitAll()
                    .requestMatchers(HttpMethod.POST, "/v1/admin/auth/login")
                    .permitAll()
                    .requestMatchers("/v1/admin/**")
                    .hasAuthority(JwtAuthenticationFilter.ROLE_CMS)
                    .requestMatchers(HttpMethod.POST, "/v1/content/bookmarks/**")
                    .hasAuthority(JwtAuthenticationFilter.ROLE_USER)
                    .requestMatchers(HttpMethod.DELETE, "/v1/content/bookmarks/**")
                    .hasAuthority(JwtAuthenticationFilter.ROLE_USER)
                    .requestMatchers(HttpMethod.GET, "/v1/content/bookmarks")
                    .hasAuthority(JwtAuthenticationFilter.ROLE_USER)
                    .requestMatchers(HttpMethod.POST, "/v1/identity/guest")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        "/v1/auth/register",
                        "/v1/auth/login",
                        "/v1/auth/logout",
                    ).permitAll()
                    .requestMatchers("/v1/auth/refresh")
                    .permitAll()
                    .requestMatchers(
                        "/v1/sync/**",
                    ).hasAnyAuthority(JwtAuthenticationFilter.ROLE_USER, JwtAuthenticationFilter.ROLE_GUEST)
                    .requestMatchers("/v1/identity/**")
                    .authenticated()
                    .requestMatchers("/v1/devices/**")
                    .authenticated()
                    .requestMatchers("/v1/privacy/**")
                    .hasAuthority(JwtAuthenticationFilter.ROLE_USER)
                    .anyRequest()
                    .authenticated()
            }.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
