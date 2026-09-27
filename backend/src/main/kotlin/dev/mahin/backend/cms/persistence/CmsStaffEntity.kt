package dev.mahin.backend.cms.persistence

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "cms_staff")
@Suppress("LongParameterList")
class CmsStaffEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(name = "email", nullable = false, unique = true, length = 320)
    var email: String = "",
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String = "",
    @Column(name = "display_name", nullable = false, length = 200)
    var displayName: String = "",
    @Column(name = "active", nullable = false)
    var active: Boolean = true,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
    @OneToMany(mappedBy = "staff", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    val roles: MutableList<CmsStaffRoleEntity> = mutableListOf(),
)
