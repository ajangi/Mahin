package dev.mahin.backend.api

import dev.mahin.backend.security.MahinAuthSubject
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException

fun currentMahinSubject(): MahinAuthSubject {
    val authentication = SecurityContextHolder.getContext().authentication
    val principal = authentication?.principal
    if (principal is MahinAuthSubject) {
        return principal
    }
    throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized")
}

fun currentRegisteredUser(): MahinAuthSubject.RegisteredUser {
    val subject = currentMahinSubject()
    if (subject is MahinAuthSubject.RegisteredUser) {
        return subject
    }
    throw ResponseStatusException(HttpStatus.FORBIDDEN, "registered_account_required")
}
