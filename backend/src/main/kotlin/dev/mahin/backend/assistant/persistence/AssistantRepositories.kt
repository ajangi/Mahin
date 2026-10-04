package dev.mahin.backend.assistant.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AssistantConsentRepository : JpaRepository<AssistantConsentEntity, UUID>

interface AssistantInteractionLogRepository : JpaRepository<AssistantInteractionLogEntity, UUID> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM AssistantInteractionLogEntity l WHERE l.userId = :userId")
    fun deleteAllByUserId(
        @Param("userId") userId: UUID,
    )
}
