package dev.mahin.backend.cms.persistence

import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface CmsStaffRepository : JpaRepository<CmsStaffEntity, UUID> {
    fun findByEmailIgnoreCase(email: String): Optional<CmsStaffEntity>
}
