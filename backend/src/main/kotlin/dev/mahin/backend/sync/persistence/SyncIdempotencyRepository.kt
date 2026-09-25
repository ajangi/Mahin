package dev.mahin.backend.sync.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface SyncIdempotencyRepository : JpaRepository<SyncIdempotencyEntity, SyncIdempotencyEntity.Key>
