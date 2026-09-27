package dev.mahin.backend.cms

import dev.mahin.backend.api.currentCmsRoles
import dev.mahin.backend.content.CreateCmsStaffRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/v1/admin/staff")
class CmsStaffController(
    private val authService: CmsAuthService,
) {
    @PostMapping
    fun createStaff(
        @RequestBody request: CreateCmsStaffRequest,
    ): Map<String, String> {
        if (!currentCmsRoles().contains(CmsRole.SUPER_ADMIN)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "super_admin_required")
        }
        val roles =
            request.roles.map { CmsRole.valueOf(it) }.toSet()
        val staff =
            authService.createStaff(
                email = request.email,
                password = request.password,
                displayName = request.displayName,
                roles = roles,
            )
        return mapOf("staffId" to staff.id.toString())
    }
}
