package dev.mahin.backend.privacy.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface ExportJobRepository : JpaRepository<ExportJobEntity, UUID>
