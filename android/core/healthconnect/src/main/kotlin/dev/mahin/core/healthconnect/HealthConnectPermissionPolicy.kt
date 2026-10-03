package dev.mahin.core.healthconnect

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.MenstruationFlowRecord

/**
 * Minimal product-approved Health Connect permissions (M10). No broad "future use" scopes.
 */
object HealthConnectPermissionPolicy {
    val requiredPermissions: Set<String> =
        setOf(
            HealthPermission.getReadPermission(MenstruationFlowRecord::class),
            HealthPermission.getWritePermission(MenstruationFlowRecord::class),
        )

    fun hasAllGranted(granted: Set<String>): Boolean = requiredPermissions.all { it in granted }
}
