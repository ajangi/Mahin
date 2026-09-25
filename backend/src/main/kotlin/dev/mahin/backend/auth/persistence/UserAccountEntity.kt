package dev.mahin.backend.auth.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_account")
class UserAccountEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "email", nullable = false, unique = true)
    var email: String,
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
