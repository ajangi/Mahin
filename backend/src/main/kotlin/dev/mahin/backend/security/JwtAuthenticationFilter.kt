package dev.mahin.backend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            val token = header.removePrefix(BEARER_PREFIX).trim()
            val subject = jwtService.parseAccessToken(token)
            if (subject != null) {
                val role =
                    when (subject) {
                        is MahinAuthSubject.RegisteredUser -> ROLE_USER
                        is MahinAuthSubject.GuestInstallation -> ROLE_GUEST
                    }
                val authentication =
                    UsernamePasswordAuthenticationToken(
                        subject,
                        null,
                        listOf(SimpleGrantedAuthority(role)),
                    )
                SecurityContextHolder.getContext().authentication = authentication
            }
        }
        filterChain.doFilter(request, response)
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        const val ROLE_USER = "ROLE_USER"
        const val ROLE_GUEST = "ROLE_GUEST"
    }
}
