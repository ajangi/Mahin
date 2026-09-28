package dev.mahin.backend.security

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SecurityAuditEventRepository : JpaRepository<SecurityAuditEventEntity, UUID>
