package dev.mahin.backend.api

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class ApiExceptionHandler {
    private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(ResponseStatusException::class)
    fun handleStatus(
        ex: ResponseStatusException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.valueOf(ex.statusCode.value())
        val requestId = request.getHeader("X-Request-Id") ?: "unknown"
        logger.warn("status={} path={} code={}", status.value(), request.requestURI, ex.reason)
        return ResponseEntity.status(status).body(
            ErrorResponse(
                code = ex.reason ?: "error",
                message = "Request could not be completed",
                requestId = requestId,
            ),
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val requestId = request.getHeader("X-Request-Id") ?: "unknown"
        logger.error("Unhandled error path={} type={}", request.requestURI, ex.javaClass.simpleName)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(
                code = "internal_error",
                message = "Request could not be completed",
                requestId = requestId,
            ),
        )
    }
}
