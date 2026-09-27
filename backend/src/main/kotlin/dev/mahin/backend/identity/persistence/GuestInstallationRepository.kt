package dev.mahin.backend.identity.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface GuestInstallationRepository : JpaRepository<GuestInstallationEntity, UUID> {
    fun findByLocalUserId(localUserId: UUID): GuestInstallationEntity?
}
