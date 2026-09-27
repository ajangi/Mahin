package dev.mahin.backend.entitlement.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "play_subscription_record")
@Suppress("LongParameterList")
class PlaySubscriptionRecordEntity(
    @Id val id: UUID,
    @Column(name = "user_id", nullable = false) val userId: UUID,
    @Column(name = "product_id", nullable = false) val productId: String,
    @Column(name = "purchase_token_hash", nullable = false) val purchaseTokenHash: String,
    @Column(name = "subscription_state", nullable = false) val subscriptionState: String,
    @Column(name = "expires_at") val expiresAt: Instant?,
    @Column(name = "acknowledged_at", nullable = false) val acknowledgedAt: Instant,
    @Column(name = "updated_at", nullable = false) val updatedAt: Instant,
)
