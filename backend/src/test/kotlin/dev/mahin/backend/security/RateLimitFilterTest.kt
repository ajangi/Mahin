package dev.mahin.backend.security

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RateLimitFilterTest {
    @Test
    fun blocksAssistantAskWhenLimitExceeded() {
        val filter = RateLimitFilter(limitPerMinute = 1)
        val request = MockHttpServletRequest("POST", "/v1/assistant/ask")
        request.remoteAddr = "203.0.113.11"
        val response = MockHttpServletResponse()
        val chain: FilterChain = MockFilterChain()

        filter.doFilter(request, response, chain)
        filter.doFilter(request, response, chain)

        assertEquals(429, response.status)
    }

    @Test
    fun blocksAuthEndpointWhenLimitExceeded() {
        val filter = RateLimitFilter(limitPerMinute = 1)
        val request = MockHttpServletRequest("POST", "/v1/auth/login")
        request.remoteAddr = "203.0.113.10"
        val response = MockHttpServletResponse()
        val chain: FilterChain = MockFilterChain()

        filter.doFilter(request, response, chain)
        filter.doFilter(request, response, chain)

        assertEquals(429, response.status)
    }
}
