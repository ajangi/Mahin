package dev.mahin.backend.cms.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.io.Serializable
import java.util.UUID

@Entity
@Table(name = "cms_staff_role")
@IdClass(CmsStaffRoleEntity.Pk::class)
class CmsStaffRoleEntity(
    @Id
    @Column(name = "staff_id", nullable = false)
    var staffId: UUID = UUID.randomUUID(),
    @Id
    @Column(name = "role", nullable = false, length = 32)
    var role: String = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false, insertable = false, updatable = false)
    var staff: CmsStaffEntity? = null,
) {
    data class Pk(
        val staffId: UUID = UUID.randomUUID(),
        val role: String = "",
    ) : Serializable {
        companion object {
            private const val serialVersionUID: Long = 1L
        }
    }
}
