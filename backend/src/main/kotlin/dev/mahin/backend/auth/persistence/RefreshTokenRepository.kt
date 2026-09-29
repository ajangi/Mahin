package dev.mahin.backend.auth.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByTokenHashAndRevokedAtIsNull(tokenHash: String): RefreshTokenEntity?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RefreshTokenEntity t where t.userId = :userId")
    fun deleteAllByUserId(userId: UUID)

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RefreshTokenEntity t where t.deviceId in :deviceIds")
    fun deleteAllByDeviceIdIn(deviceIds: Collection<UUID>)

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RefreshTokenEntity t where t.guestInstallationId = :guestInstallationId")
    fun deleteAllByGuestInstallationId(guestInstallationId: UUID)
}
