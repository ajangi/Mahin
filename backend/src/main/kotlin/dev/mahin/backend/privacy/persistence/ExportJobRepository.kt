package dev.mahin.backend.privacy.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface ExportJobRepository : JpaRepository<ExportJobEntity, UUID> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ExportJobEntity j where j.userId = :userId")
    fun deleteAllByUserId(userId: UUID)
}
