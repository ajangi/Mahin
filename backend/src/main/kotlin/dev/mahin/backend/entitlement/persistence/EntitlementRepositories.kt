package dev.mahin.backend.entitlement.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PlaySubscriptionRecordRepository : JpaRepository<PlaySubscriptionRecordEntity, UUID> {
    fun findByUserIdAndPurchaseTokenHash(
        userId: UUID,
        purchaseTokenHash: String,
    ): PlaySubscriptionRecordEntity?
}

interface EntitlementGrantRepository : JpaRepository<EntitlementGrantEntity, UUID> {
    @Query(
        """
        SELECT g FROM EntitlementGrantEntity g
        WHERE g.userId = :userId
          AND g.revokedAt IS NULL
          AND (g.expiresAt IS NULL OR g.expiresAt > :now)
        ORDER BY g.createdAt DESC
        """,
    )
    fun findActiveGrants(
        userId: UUID,
        now: java.time.Instant,
    ): List<EntitlementGrantEntity>
}
