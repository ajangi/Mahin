package dev.mahin.backend.sync.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface SyncEntityRecordRepository : JpaRepository<SyncEntityRecordEntity, UUID> {
    fun findByOwnerUserIdAndEntityTypeAndEntityId(
        ownerUserId: UUID,
        entityType: String,
        entityId: UUID,
    ): SyncEntityRecordEntity?

    fun findByGuestInstallationIdAndEntityTypeAndEntityId(
        guestInstallationId: UUID,
        entityType: String,
        entityId: UUID,
    ): SyncEntityRecordEntity?

    @Query(
        """
        select r from SyncEntityRecordEntity r
        where r.ownerUserId = :userId and r.serverRevision > :afterRevision
        order by r.serverRevision asc
        """,
    )
    fun findUserChangesAfter(
        userId: UUID,
        afterRevision: Long,
    ): List<SyncEntityRecordEntity>

    @Query(
        """
        select r from SyncEntityRecordEntity r
        where r.guestInstallationId = :guestId and r.serverRevision > :afterRevision
        order by r.serverRevision asc
        """,
    )
    fun findGuestChangesAfter(
        guestId: UUID,
        afterRevision: Long,
    ): List<SyncEntityRecordEntity>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SyncEntityRecordEntity r where r.ownerUserId = :ownerUserId")
    fun deleteAllByOwnerUserId(ownerUserId: UUID)

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SyncEntityRecordEntity r where r.guestInstallationId = :guestInstallationId")
    fun deleteAllByGuestInstallationId(guestInstallationId: UUID)
}
