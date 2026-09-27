package dev.mahin.backend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RateLimitFilter(
    @Value("\${mahin.auth.rate-limit-per-minute}") private val limitPerMinute: Int,
) : OncePerRequestFilter() {
    private val buckets = ConcurrentHashMap<String, WindowCounter>()

    @Suppress("ReturnCount")
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (!request.requestURI.startsWith("/v1/auth") && !request.requestURI.startsWith("/v1/identity")) {
            filterChain.doFilter(request, response)
            return
        }
        val key = request.remoteAddr ?: "unknown"
        val windowStartMs = System.currentTimeMillis() / WINDOW_MS * WINDOW_MS
        val counter =
            buckets.compute(key) { _, existing ->
                if (existing == null || existing.windowStartMs != windowStartMs) {
                    WindowCounter(windowStartMs, AtomicInteger(0))
                } else {
                    existing
                }
            } ?: return
        if (counter.count.incrementAndGet() > limitPerMinute) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.writer.write("""{"code":"rate_limited","message":"Too many requests","requestId":"unknown"}""")
            return
        }
        filterChain.doFilter(request, response)
    }

    private data class WindowCounter(
        val windowStartMs: Long,
        val count: AtomicInteger,
    )

    companion object {
        private const val WINDOW_MS = 60_000L
    }
}
