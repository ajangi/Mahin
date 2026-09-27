package dev.mahin.backend.security

import java.util.UUID

sealed interface MahinAuthSubject {
    val deviceId: UUID

    fun ownerKey(): String

    data class RegisteredUser(
        val userId: UUID,
        override val deviceId: UUID,
    ) : MahinAuthSubject {
        override fun ownerKey(): String = "user:$userId"
    }

    data class GuestInstallation(
        val guestInstallationId: UUID,
        override val deviceId: UUID,
    ) : MahinAuthSubject {
        override fun ownerKey(): String = "guest:$guestInstallationId"
    }
}
