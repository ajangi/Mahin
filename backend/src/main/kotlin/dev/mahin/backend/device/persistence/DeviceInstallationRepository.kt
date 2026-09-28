package dev.mahin.backend.device.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface DeviceInstallationRepository : JpaRepository<DeviceInstallationEntity, UUID> {
    fun findAllByOwnerUserId(userId: UUID): List<DeviceInstallationEntity>

    fun findAllByGuestInstallationId(guestInstallationId: UUID): List<DeviceInstallationEntity>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from DeviceInstallationEntity d where d.ownerUserId = :ownerUserId")
    fun deleteAllByOwnerUserId(ownerUserId: UUID)
}
