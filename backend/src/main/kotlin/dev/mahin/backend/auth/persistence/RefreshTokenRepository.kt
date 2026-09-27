package dev.mahin.backend.auth.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByTokenHashAndRevokedAtIsNull(tokenHash: String): RefreshTokenEntity?
}
