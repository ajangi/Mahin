package dev.mahin.core.model

import java.util.UUID

@JvmInline
value class LocalUserId(val value: UUID)

data class GuestIdentity(
    val localUserId: LocalUserId,
    val createdAtEpochMs: Long,
)

enum class InstallationState {
    GUEST_LOCAL,
    REGISTERED,
}

/**
 * Product analytics must use a pseudonymous ID separable from medical record IDs.
 */
@JvmInline
value class AnalyticsInstallationId(val value: UUID)
