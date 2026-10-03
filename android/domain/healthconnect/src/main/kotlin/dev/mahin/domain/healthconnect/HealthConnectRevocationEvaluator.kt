package dev.mahin.domain.healthconnect

internal object HealthConnectRevocationEvaluator {
    fun evaluate(
        launchFlagEnabled: Boolean,
        userOptIn: Boolean,
        permissionsPreviouslyGranted: Boolean,
        allPermissionsGranted: Boolean,
    ): HealthConnectSyncResult? =
        when {
            !launchFlagEnabled -> HealthConnectSyncResult.FeatureDisabled
            !userOptIn -> HealthConnectSyncResult.NotOptedIn
            permissionsPreviouslyGranted && !allPermissionsGranted ->
                HealthConnectSyncResult.PermissionsRevoked
            !allPermissionsGranted -> HealthConnectSyncResult.PermissionsMissing
            else -> null
        }
}
