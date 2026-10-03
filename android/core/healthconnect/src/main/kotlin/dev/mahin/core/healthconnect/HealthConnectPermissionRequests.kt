package dev.mahin.core.healthconnect

import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.PermissionController

object HealthConnectPermissionRequests {
    fun createPermissionResultContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()
}
