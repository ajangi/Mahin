package dev.mahin.backend.privacy

import dev.mahin.backend.auth.persistence.RefreshTokenRepository
import dev.mahin.backend.auth.persistence.UserAccountRepository
import dev.mahin.backend.content.persistence.UserContentBookmarkRepository
import dev.mahin.backend.device.persistence.DeviceInstallationRepository
import dev.mahin.backend.entitlement.persistence.EntitlementGrantRepository
import dev.mahin.backend.entitlement.persistence.PlaySubscriptionRecordRepository
import dev.mahin.backend.identity.persistence.GuestInstallationRepository
import dev.mahin.backend.privacy.persistence.DeletionRequestRepository
import dev.mahin.backend.privacy.persistence.ExportJobRepository
import dev.mahin.backend.security.SecurityAuditService
import dev.mahin.backend.sync.persistence.SyncEntityRecordRepository
import dev.mahin.backend.sync.persistence.SyncIdempotencyRepository
import dev.mahin.backend.sync.persistence.SyncOwnerStateRepository
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Purges server-side user data for account deletion. Never logs health payloads.
 */
@Service
@Suppress("LongParameterList")
class UserDataErasureService(
    private val syncEntityRecordRepository: SyncEntityRecordRepository,
    private val syncOwnerStateRepository: SyncOwnerStateRepository,
    private val syncIdempotencyRepository: SyncIdempotencyRepository,
    private val deviceInstallationRepository: DeviceInstallationRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val entitlementGrantRepository: EntitlementGrantRepository,
    private val playSubscriptionRecordRepository: PlaySubscriptionRecordRepository,
    private val exportJobRepository: ExportJobRepository,
    private val bookmarkRepository: UserContentBookmarkRepository,
    private val deletionRequestRepository: DeletionRequestRepository,
    private val guestInstallationRepository: GuestInstallationRepository,
    private val userAccountRepository: UserAccountRepository,
    private val securityAuditService: SecurityAuditService,
) : RegisteredUserErasure {
    @Transactional
    override fun eraseRegisteredUser(userId: UUID) {
        val ownerKey = "user:$userId"
        val devices = deviceInstallationRepository.findAllByOwnerUserId(userId)
        val deviceIds = devices.map { it.id }
        if (deviceIds.isNotEmpty()) {
            refreshTokenRepository.deleteAllByDeviceIdIn(deviceIds)
        }
        refreshTokenRepository.deleteAllByUserId(userId)
        purgeLinkedGuestInstallations(userId)
        syncEntityRecordRepository.deleteAllByOwnerUserId(userId)
        syncIdempotencyRepository.deleteAllByOwnerKey(ownerKey)
        syncOwnerStateRepository.deleteById(ownerKey)
        deviceInstallationRepository.deleteAllByOwnerUserId(userId)
        entitlementGrantRepository.deleteAllByUserId(userId)
        playSubscriptionRecordRepository.deleteAllByUserId(userId)
        exportJobRepository.deleteAllByUserId(userId)
        bookmarkRepository.deleteAllByUserId(userId)
        deletionRequestRepository.deleteAllByUserId(userId)
        userAccountRepository.deleteById(userId)
        securityAuditService.record(
            SecurityAuditService.AuditRecord(
                actorType = "system",
                actorId = "account_deletion",
                action = "user_data_erased",
                targetType = "user",
                targetId = userId.toString(),
            ),
        )
    }

    private fun purgeLinkedGuestInstallations(userId: UUID) {
        guestInstallationRepository.findAllByLinkedUserId(userId).forEach { guest ->
            val guestKey = "guest:${guest.id}"
            refreshTokenRepository.deleteAllByGuestInstallationId(guest.id)
            syncEntityRecordRepository.deleteAllByGuestInstallationId(guest.id)
            syncIdempotencyRepository.deleteAllByOwnerKey(guestKey)
            syncOwnerStateRepository.deleteById(guestKey)
            guestInstallationRepository.deleteById(guest.id)
        }
    }
}
