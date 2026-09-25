package dev.mahin.backend.auth

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val authTokenService: AuthTokenService,
) {
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest,
    ): AuthTokenResponse = authService.register(request)

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): AuthTokenResponse = authService.login(request)

    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshRequest,
    ): AuthTokenResponse =
        try {
            authTokenService.refresh(request.refreshToken)
        } catch (ex: AuthException) {
            throw org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED,
                ex.code,
                ex,
            )
        }

    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody request: LogoutRequest,
    ) {
        authTokenService.revoke(request.refreshToken)
    }
}
