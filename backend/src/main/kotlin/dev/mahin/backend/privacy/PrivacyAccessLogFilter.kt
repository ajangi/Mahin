package dev.mahin.backend.privacy

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.UUID
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Access log without query strings, bodies, or headers that may contain health data.
 */
@Component
class PrivacyAccessLogFilter : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(PrivacyAccessLogFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestId = request.getHeader("X-Request-Id") ?: UUID.randomUUID().toString()
        MDC.put("requestId", requestId)
        response.setHeader("X-Request-Id", requestId)
        try {
            filterChain.doFilter(request, response)
            logger.info("{} {} -> {}", request.method, request.requestURI, response.status)
        } finally {
            MDC.remove("requestId")
        }
    }
}
