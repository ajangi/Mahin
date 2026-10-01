package dev.mahin.core.healthconnect

enum class HealthConnectAvailability {
    FEATURE_DISABLED,
    SDK_UNAVAILABLE,
    NOT_INSTALLED,
    UPDATE_REQUIRED,
    READY,
}

sealed interface HealthConnectSyncResult {
    data object FeatureDisabled : HealthConnectSyncResult

    data object Locked : HealthConnectSyncResult

    data object NotOptedIn : HealthConnectSyncResult

    data object PermissionsMissing : HealthConnectSyncResult

    data object PermissionsRevoked : HealthConnectSyncResult

    data class Success(
        val importedDays: Int = 0,
        val exportedDays: Int = 0,
    ) : HealthConnectSyncResult

    data class Failure(
        val errorClass: String,
    ) : HealthConnectSyncResult
}

interface HealthConnectClientGateway {
    suspend fun availability(): HealthConnectAvailability

    suspend fun grantedPermissionStrings(): Set<String>

    suspend fun importMenstruationFlowDays(): List<dev.mahin.domain.healthconnect.HealthConnectMenstruationFlowDay>

    suspend fun exportMenstruationFlowDays(
        days: List<dev.mahin.domain.healthconnect.HealthConnectMenstruationFlowDay>,
    ): Int
}
