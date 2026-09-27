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
                val authorities =
                    when (subject) {
                        is MahinAuthSubject.RegisteredUser ->
                            listOf(SimpleGrantedAuthority(ROLE_USER))
                        is MahinAuthSubject.GuestInstallation ->
                            listOf(SimpleGrantedAuthority(ROLE_GUEST))
                        is MahinAuthSubject.CmsStaff -> {
                            val cmsRoles =
                                subject.roles.map { role ->
                                    SimpleGrantedAuthority("ROLE_CMS_$role")
                                }
                            listOf(SimpleGrantedAuthority(ROLE_CMS)) + cmsRoles
                        }
                    }
                val authentication =
                    UsernamePasswordAuthenticationToken(
                        subject,
                        null,
                        authorities,
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
        const val ROLE_CMS = "ROLE_CMS"
    }
}
