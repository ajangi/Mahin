package dev.mahin.backend.identity

import dev.mahin.backend.auth.AuthException
import dev.mahin.backend.auth.AuthTokenService
import dev.mahin.backend.identity.persistence.GuestInstallationEntity
import dev.mahin.backend.identity.persistence.GuestInstallationRepository
import dev.mahin.backend.security.JwtService
import dev.mahin.backend.security.MahinAuthSubject
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class GuestProofService(
    private val jwtService: JwtService,
    private val authTokenService: AuthTokenService,
    private val guestInstallationRepository: GuestInstallationRepository,
) {
    @Suppress("ThrowsCount")
    fun requireGuestProof(
        expectedLocalUserId: UUID,
        guestAccessToken: String?,
        guestRefreshToken: String?,
        linkingUserId: UUID? = null,
    ): GuestInstallationEntity {
        if (guestAccessToken.isNullOrBlank() && guestRefreshToken.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "guest_proof_required")
        }
        val guestInstallationId =
            when {
                !guestAccessToken.isNullOrBlank() -> guestInstallationIdFromAccessToken(guestAccessToken)
                else ->
                    try {
                        authTokenService.resolveGuestInstallationFromRefreshToken(guestRefreshToken!!)
                    } catch (_: AuthException) {
                        throw ResponseStatusException(HttpStatus.FORBIDDEN, "guest_proof_invalid")
                    }
            }
        val guest =
            guestInstallationRepository.findById(guestInstallationId).orElse(null)
                ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "guest_proof_invalid")
        if (guest.localUserId != expectedLocalUserId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "guest_local_user_mismatch")
        }
        if (guest.linkedUserId != null && linkingUserId != null && guest.linkedUserId != linkingUserId) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "guest_already_linked")
        }
        return guest
    }

    private fun guestInstallationIdFromAccessToken(guestAccessToken: String): UUID {
        val subject =
            jwtService.parseAccessToken(guestAccessToken)
                ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "guest_proof_invalid")
        return when (subject) {
            is MahinAuthSubject.GuestInstallation -> subject.guestInstallationId
            is MahinAuthSubject.RegisteredUser ->
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "guest_proof_invalid")
        }
    }
}
