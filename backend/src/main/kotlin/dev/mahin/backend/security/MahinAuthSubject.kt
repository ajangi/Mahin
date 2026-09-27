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

    data class CmsStaff(
        val staffId: UUID,
        val email: String,
        val roles: Set<String>,
        override val deviceId: UUID = STAFF_DEVICE_ID,
    ) : MahinAuthSubject {
        override fun ownerKey(): String = "cms:$staffId"

        companion object {
            val STAFF_DEVICE_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        }
    }
}
