package dev.mahin.backend.device.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface DeviceInstallationRepository : JpaRepository<DeviceInstallationEntity, UUID> {
    fun findAllByOwnerUserId(userId: UUID): List<DeviceInstallationEntity>

    fun findAllByGuestInstallationId(guestInstallationId: UUID): List<DeviceInstallationEntity>
}
