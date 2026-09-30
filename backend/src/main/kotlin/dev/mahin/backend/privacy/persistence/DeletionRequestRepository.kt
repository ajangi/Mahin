package dev.mahin.backend.privacy.persistence

import java.time.Instant
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface DeletionRequestRepository : JpaRepository<DeletionRequestEntity, UUID> {
    fun findAllByStatusAndScheduledAtLessThanEqual(
        status: String,
        scheduledAt: Instant,
    ): List<DeletionRequestEntity>

    fun findAllByStatusAndProcessingStartedAtBefore(
        status: String,
        processingStartedAt: Instant,
    ): List<DeletionRequestEntity>

    fun findTopByUserIdOrderByRequestedAtDesc(userId: UUID): DeletionRequestEntity?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from DeletionRequestEntity d where d.userId = :userId")
    fun deleteAllByUserId(userId: UUID)
}
