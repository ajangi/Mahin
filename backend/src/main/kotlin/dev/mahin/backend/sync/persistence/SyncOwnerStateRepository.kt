package dev.mahin.backend.sync.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface SyncOwnerStateRepository : JpaRepository<SyncOwnerStateEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SyncOwnerStateEntity s where s.ownerKey = :ownerKey")
    fun findForUpdate(ownerKey: String): SyncOwnerStateEntity?
}
