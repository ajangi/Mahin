package dev.mahin.backend.notifications

import dev.mahin.backend.device.persistence.DeviceInstallationEntity
import dev.mahin.backend.device.persistence.DeviceInstallationRepository
import dev.mahin.backend.security.MahinAuthSubject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class PushTokenService(
    private val deviceInstallationRepository: DeviceInstallationRepository,
) {
    @Transactional
    @Suppress("ReturnCount")
    fun registerToken(
        subject: MahinAuthSubject,
        deviceId: UUID,
        request: RegisterPushTokenRequest,
    ): PushTokenRegistrationResponse {
        validatePushRequest(request)
        val device = loadAuthorizedDevice(subject, deviceId)
        val now = Instant.now()
        device.pushProvider = request.provider.trim().lowercase()
        device.pushTokenHash = sha256Hex(request.token)
        device.pushTokenUpdatedAt = now
        device.lastSeenAt = now
        deviceInstallationRepository.save(device)
        return PushTokenRegistrationResponse(
            deviceId = device.id,
            provider = device.pushProvider!!,
            registeredAt = now,
        )
    }

    @Transactional
    fun clearToken(
        subject: MahinAuthSubject,
        deviceId: UUID,
    ) {
        val device = loadAuthorizedDevice(subject, deviceId)
        device.pushProvider = null
        device.pushTokenHash = null
        device.pushTokenUpdatedAt = Instant.now()
        deviceInstallationRepository.save(device)
    }

    private fun validatePushRequest(request: RegisterPushTokenRequest) {
        if (request.token.isBlank() || request.token.length > 4096) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_push_token")
        }
        val provider = request.provider.trim().lowercase()
        if (provider !in ALLOWED_PROVIDERS) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported_push_provider")
        }
    }

    private fun loadAuthorizedDevice(
        subject: MahinAuthSubject,
        deviceId: UUID,
    ): DeviceInstallationEntity {
        val device =
            deviceInstallationRepository.findById(deviceId).orElse(null)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "device_not_found")
        val authorized =
            when (subject) {
                is MahinAuthSubject.RegisteredUser -> device.ownerUserId == subject.userId
                is MahinAuthSubject.GuestInstallation -> device.guestInstallationId == subject.guestInstallationId
                is MahinAuthSubject.CmsStaff -> false
            }
        if (!authorized || subject.deviceId != deviceId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "device_not_found")
        }
        return device
    }

    private fun sha256Hex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(value.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val ALLOWED_PROVIDERS = setOf("fcm", "none")
    }
}
