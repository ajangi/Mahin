package dev.mahin.backend.identity

import dev.mahin.backend.auth.AuthTokenService
import dev.mahin.backend.identity.persistence.GuestInstallationEntity
import dev.mahin.backend.identity.persistence.GuestInstallationRepository
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class IdentityService(
    private val guestInstallationRepository: GuestInstallationRepository,
    private val authTokenService: AuthTokenService,
    private val guestConversionService: GuestConversionService,
    private val guestProofService: GuestProofService,
) {
    @Transactional
    fun bootstrapGuest(request: GuestBootstrapRequest): GuestBootstrapResponse {
        val existing = guestInstallationRepository.findByLocalUserId(request.localUserId)
        val guest =
            existing
                ?: guestInstallationRepository.save(
                    GuestInstallationEntity(
                        id = UUID.randomUUID(),
                        localUserId = request.localUserId,
                        createdAt = Instant.now(),
                    ),
                )
        val tokens = authTokenService.issueForGuest(guest.id, request.platform, request.appVersion)
        return GuestBootstrapResponse(
            guestInstallationId = guest.id,
            localUserId = guest.localUserId,
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            expiresInSeconds = tokens.expiresInSeconds,
            deviceId = tokens.deviceId,
        )
    }

    fun convertGuest(
        request: ConvertGuestRequest,
        userId: UUID,
    ): ConvertGuestResponse {
        guestProofService.requireGuestProof(
            expectedLocalUserId = request.localUserId,
            guestAccessToken = request.guestAccessToken,
            guestRefreshToken = request.guestRefreshToken,
            linkingUserId = userId,
        )
        return guestConversionService.convertGuestToUser(request.localUserId, userId)
    }
}
