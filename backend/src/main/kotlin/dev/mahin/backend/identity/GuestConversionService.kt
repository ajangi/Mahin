package dev.mahin.backend.identity

import dev.mahin.backend.device.persistence.DeviceInstallationRepository
import dev.mahin.backend.identity.persistence.GuestInstallationEntity
import dev.mahin.backend.identity.persistence.GuestInstallationRepository
import dev.mahin.backend.sync.persistence.SyncEntityRecordRepository
import dev.mahin.backend.sync.persistence.SyncOwnerStateEntity
import dev.mahin.backend.sync.persistence.SyncOwnerStateRepository
import java.time.Instant
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class GuestConversionService(
    private val guestInstallationRepository: GuestInstallationRepository,
    private val syncEntityRecordRepository: SyncEntityRecordRepository,
    private val syncOwnerStateRepository: SyncOwnerStateRepository,
    private val deviceInstallationRepository: DeviceInstallationRepository,
) {
    @Transactional
    fun convertGuestToUser(
        localUserId: UUID,
        userId: UUID,
    ): ConvertGuestResponse {
        val guest =
            guestInstallationRepository.findByLocalUserId(localUserId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "guest_not_found")
        if (guest.linkedUserId != null && guest.linkedUserId != userId) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "guest_already_linked")
        }
        if (guest.linkedUserId == null) {
            guest.linkedUserId = userId
            guest.convertedAt = Instant.now()
            guestInstallationRepository.save(guest)
        }
        val migrated = migrateSyncEntities(guest, userId)
        migrateDevices(guest.id, userId)
        mergeOwnerState(guest.id, userId)
        return ConvertGuestResponse(
            userId = userId,
            guestInstallationId = guest.id,
            migratedEntityCount = migrated,
        )
    }

    private fun migrateSyncEntities(
        guest: GuestInstallationEntity,
        userId: UUID,
    ): Int {
        val records = syncEntityRecordRepository.findGuestChangesAfter(guest.id, 0)
        records.forEach { record ->
            val existing =
                syncEntityRecordRepository.findByOwnerUserIdAndEntityTypeAndEntityId(
                    userId,
                    record.entityType,
                    record.entityId,
                )
            if (existing == null) {
                record.ownerUserId = userId
                record.guestInstallationId = null
                record.ownerScopeKey = "user:$userId"
                syncEntityRecordRepository.save(record)
            } else {
                syncEntityRecordRepository.delete(record)
            }
        }
        return records.size
    }

    private fun migrateDevices(
        guestInstallationId: UUID,
        userId: UUID,
    ) {
        deviceInstallationRepository.findAllByGuestInstallationId(guestInstallationId).forEach { device ->
            device.ownerUserId = userId
            device.guestInstallationId = null
            deviceInstallationRepository.save(device)
        }
    }

    private fun mergeOwnerState(
        guestInstallationId: UUID,
        userId: UUID,
    ) {
        val guestKey = "guest:$guestInstallationId"
        val userKey = "user:$userId"
        val guestState = syncOwnerStateRepository.findById(guestKey).orElse(null)
        val userState =
            syncOwnerStateRepository.findById(userKey).orElse(
                SyncOwnerStateEntity(ownerKey = userKey, nextServerRevision = 1),
            )
        if (guestState != null) {
            userState.nextServerRevision = maxOf(userState.nextServerRevision, guestState.nextServerRevision)
            syncOwnerStateRepository.save(userState)
            syncOwnerStateRepository.delete(guestState)
        } else if (!syncOwnerStateRepository.existsById(userKey)) {
            syncOwnerStateRepository.save(userState)
        }
    }
}
