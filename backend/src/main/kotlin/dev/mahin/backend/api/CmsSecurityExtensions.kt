package dev.mahin.backend.api

import dev.mahin.backend.cms.CmsRole
import dev.mahin.backend.security.MahinAuthSubject
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

fun currentCmsStaff(): MahinAuthSubject.CmsStaff {
    val subject = currentMahinSubject()
    if (subject is MahinAuthSubject.CmsStaff) {
        return subject
    }
    throw ResponseStatusException(HttpStatus.FORBIDDEN, "cms_staff_required")
}

fun currentCmsRoles(): Set<CmsRole> =
    currentCmsStaff()
        .roles
        .mapNotNull { name ->
            runCatching { CmsRole.valueOf(name) }.getOrNull()
        }.toSet()
