package dev.mahin.backend.auth.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface UserAccountRepository : JpaRepository<UserAccountEntity, UUID> {
    fun findByEmailIgnoreCase(email: String): UserAccountEntity?

    fun existsByEmailIgnoreCase(email: String): Boolean
}
