package dev.mahin.backend.content.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface ContentVersionSourceRepository : JpaRepository<ContentVersionSourceEntity, ContentVersionSourceEntity.Pk> {
    fun findByVersionId(versionId: UUID): List<ContentVersionSourceEntity>

    fun deleteByVersionId(versionId: UUID)
}
