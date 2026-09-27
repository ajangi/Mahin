package dev.mahin.backend.cms

import dev.mahin.backend.content.AdminAuthTokenResponse
import dev.mahin.backend.content.AdminLoginRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/admin/auth")
class CmsAuthController(
    private val authService: CmsAuthService,
) {
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: AdminLoginRequest,
    ): AdminAuthTokenResponse = authService.login(request)
}
