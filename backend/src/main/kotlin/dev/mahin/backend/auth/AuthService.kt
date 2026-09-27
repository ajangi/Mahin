package dev.mahin.backend.auth

import dev.mahin.backend.auth.persistence.UserAccountEntity
import dev.mahin.backend.auth.persistence.UserAccountRepository
import dev.mahin.backend.identity.GuestConversionService
import dev.mahin.backend.identity.GuestProofService
import java.time.Instant
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userAccountRepository: UserAccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authTokenService: AuthTokenService,
    private val guestConversionService: GuestConversionService,
    private val guestProofService: GuestProofService,
) {
    @Transactional
    fun register(request: RegisterRequest): AuthTokenResponse {
        val normalizedEmail = request.email.trim().lowercase()
        if (userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "email_taken")
        }
        if (request.localUserId != null) {
            val guest =
                guestProofService.requireGuestProof(
                    expectedLocalUserId = request.localUserId,
                    guestAccessToken = request.guestAccessToken,
                    guestRefreshToken = request.guestRefreshToken,
                )
            if (guest.linkedUserId != null) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "guest_already_linked")
            }
        }
        val now = Instant.now()
        val user =
            userAccountRepository.save(
                UserAccountEntity(
                    id = UUID.randomUUID(),
                    email = normalizedEmail,
                    passwordHash = passwordEncoder.encode(request.password),
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        if (request.localUserId != null) {
            guestConversionService.convertGuestToUser(request.localUserId, user.id)
        }
        return authTokenService.issueForUser(user.id, request.platform, request.appVersion)
    }

    @Transactional
    fun login(request: LoginRequest): AuthTokenResponse {
        val normalizedEmail = request.email.trim().lowercase()
        val user =
            userAccountRepository.findByEmailIgnoreCase(normalizedEmail)
                ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_credentials")
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_credentials")
        }
        return authTokenService.issueForUser(user.id, request.platform, request.appVersion)
    }
}
