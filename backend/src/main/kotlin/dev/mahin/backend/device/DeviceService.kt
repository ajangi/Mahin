package dev.mahin.backend.device

import dev.mahin.backend.device.persistence.DeviceInstallationRepository
import dev.mahin.backend.security.MahinAuthSubject
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceService(
    private val deviceInstallationRepository: DeviceInstallationRepository,
) {
    @Transactional(readOnly = true)
    fun listDevices(subject: MahinAuthSubject): DeviceListResponse {
        val devices =
            when (subject) {
                is MahinAuthSubject.RegisteredUser ->
                    deviceInstallationRepository.findAllByOwnerUserId(subject.userId)
                is MahinAuthSubject.GuestInstallation ->
                    deviceInstallationRepository.findAllByGuestInstallationId(subject.guestInstallationId)
            }
        return DeviceListResponse(
            devices =
                devices.map {
                    DeviceSessionResponse(
                        deviceId = it.id,
                        platform = it.platform,
                        appVersion = it.appVersion,
                        registeredAt = it.registeredAt,
                        lastSeenAt = it.lastSeenAt,
                    )
                },
        )
    }

    @Transactional
    @Suppress("ReturnCount")
    fun touchDevice(
        subject: MahinAuthSubject,
        deviceId: UUID,
    ): DeviceSessionResponse? {
        val device = deviceInstallationRepository.findById(deviceId).orElse(null) ?: return null
        val authorized =
            when (subject) {
                is MahinAuthSubject.RegisteredUser -> device.ownerUserId == subject.userId
                is MahinAuthSubject.GuestInstallation -> device.guestInstallationId == subject.guestInstallationId
            }
        if (!authorized || subject.deviceId != deviceId) {
            return null
        }
        device.lastSeenAt = Instant.now()
        deviceInstallationRepository.save(device)
        return DeviceSessionResponse(
            deviceId = device.id,
            platform = device.platform,
            appVersion = device.appVersion,
            registeredAt = device.registeredAt,
            lastSeenAt = device.lastSeenAt,
        )
    }
}
