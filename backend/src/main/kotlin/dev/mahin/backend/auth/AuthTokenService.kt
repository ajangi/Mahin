package dev.mahin.backend.auth

import dev.mahin.backend.auth.persistence.RefreshTokenEntity
import dev.mahin.backend.auth.persistence.RefreshTokenRepository
import dev.mahin.backend.device.persistence.DeviceInstallationEntity
import dev.mahin.backend.device.persistence.DeviceInstallationRepository
import dev.mahin.backend.security.JwtService
import dev.mahin.backend.security.MahinAuthSubject
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthTokenService(
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val deviceInstallationRepository: DeviceInstallationRepository,
    @Value("\${mahin.auth.access-token-ttl-seconds}") private val accessTtlSeconds: Long,
    @Value("\${mahin.auth.refresh-token-ttl-seconds}") private val refreshTtlSeconds: Long,
) {
    @Transactional
    fun issueForUser(
        userId: UUID,
        platform: String,
        appVersion: String?,
    ): AuthTokenResponse {
        val device = registerDeviceForUser(userId, platform, appVersion)
        val subject = MahinAuthSubject.RegisteredUser(userId, device.id)
        return issueTokens(subject, userId = userId, guestInstallationId = null, device.id)
    }

    @Transactional
    fun issueForGuest(
        guestInstallationId: UUID,
        platform: String,
        appVersion: String?,
    ): AuthTokenResponse {
        val device = registerDeviceForGuest(guestInstallationId, platform, appVersion)
        val subject = MahinAuthSubject.GuestInstallation(guestInstallationId, device.id)
        return issueTokens(subject, userId = null, guestInstallationId = guestInstallationId, device.id)
    }

    @Transactional
    @Suppress("ThrowsCount")
    fun refresh(refreshToken: String): AuthTokenResponse {
        val hash = hashToken(refreshToken)
        val stored =
            refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hash)
                ?: throw AuthException("invalid_refresh_token")
        if (stored.expiresAt.isBefore(Instant.now())) {
            throw AuthException("refresh_expired")
        }
        stored.revokedAt = Instant.now()
        refreshTokenRepository.save(stored)
        return when {
            stored.userId != null ->
                issueTokens(
                    MahinAuthSubject.RegisteredUser(stored.userId!!, stored.deviceId),
                    userId = stored.userId,
                    guestInstallationId = null,
                    stored.deviceId,
                )
            stored.guestInstallationId != null ->
                issueTokens(
                    MahinAuthSubject.GuestInstallation(stored.guestInstallationId!!, stored.deviceId),
                    userId = null,
                    guestInstallationId = stored.guestInstallationId,
                    stored.deviceId,
                )
            else -> throw AuthException("invalid_refresh_token")
        }
    }

    @Transactional
    fun revoke(refreshToken: String) {
        val hash = hashToken(refreshToken)
        val stored = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hash) ?: return
        stored.revokedAt = Instant.now()
        refreshTokenRepository.save(stored)
    }

    private fun issueTokens(
        subject: MahinAuthSubject,
        userId: UUID?,
        guestInstallationId: UUID?,
        deviceId: UUID,
    ): AuthTokenResponse {
        val accessToken = jwtService.issueAccessToken(subject)
        val refreshPlain = UUID.randomUUID().toString() + UUID.randomUUID().toString()
        val refreshEntity =
            RefreshTokenEntity(
                id = UUID.randomUUID(),
                tokenHash = hashToken(refreshPlain),
                userId = userId,
                guestInstallationId = guestInstallationId,
                deviceId = deviceId,
                expiresAt = Instant.now().plusSeconds(refreshTtlSeconds),
            )
        refreshTokenRepository.save(refreshEntity)
        return AuthTokenResponse(
            accessToken = accessToken,
            refreshToken = refreshPlain,
            expiresInSeconds = accessTtlSeconds,
            userId = userId,
            guestInstallationId = guestInstallationId,
            deviceId = deviceId,
        )
    }

    private fun registerDeviceForUser(
        userId: UUID,
        platform: String,
        appVersion: String?,
    ): DeviceInstallationEntity {
        val now = Instant.now()
        val device =
            DeviceInstallationEntity(
                id = UUID.randomUUID(),
                ownerUserId = userId,
                platform = platform,
                appVersion = appVersion,
                registeredAt = now,
                lastSeenAt = now,
            )
        return deviceInstallationRepository.save(device)
    }

    private fun registerDeviceForGuest(
        guestInstallationId: UUID,
        platform: String,
        appVersion: String?,
    ): DeviceInstallationEntity {
        val now = Instant.now()
        val device =
            DeviceInstallationEntity(
                id = UUID.randomUUID(),
                guestInstallationId = guestInstallationId,
                platform = platform,
                appVersion = appVersion,
                registeredAt = now,
                lastSeenAt = now,
            )
        return deviceInstallationRepository.save(device)
    }

    fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(token.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

class AuthException(
    val code: String,
) : RuntimeException(code)
