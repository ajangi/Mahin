package dev.mahin.backend.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "app_meta")
class AppMetaEntity(
    @Id
    @Column(name = "meta_key", nullable = false, length = 64)
    val key: String = "",
    @Column(name = "meta_value", nullable = false, length = 512)
    val value: String = "",
)
