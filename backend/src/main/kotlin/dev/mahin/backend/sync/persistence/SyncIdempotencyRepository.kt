package dev.mahin.backend.sync.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface SyncIdempotencyRepository : JpaRepository<SyncIdempotencyEntity, SyncIdempotencyEntity.Key> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SyncIdempotencyEntity e where e.ownerKey = :ownerKey")
    fun deleteAllByOwnerKey(ownerKey: String)
}
