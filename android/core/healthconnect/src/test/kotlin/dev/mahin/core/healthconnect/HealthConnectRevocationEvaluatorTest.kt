package dev.mahin.core.healthconnect

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HealthConnectRevocationEvaluatorTest {
    @Test
    fun returnsFeatureDisabledWhenLaunchFlagOff() {
        assertThat(
            HealthConnectRevocationEvaluator.evaluate(
                launchFlagEnabled = false,
                userOptIn = true,
                permissionsPreviouslyGranted = true,
                allPermissionsGranted = true,
            ),
        ).isEqualTo(HealthConnectSyncResult.FeatureDisabled)
    }

    @Test
    fun detectsRevocationWhenPermissionsRemoved() {
        assertThat(
            HealthConnectRevocationEvaluator.evaluate(
                launchFlagEnabled = true,
                userOptIn = true,
                permissionsPreviouslyGranted = true,
                allPermissionsGranted = false,
            ),
        ).isEqualTo(HealthConnectSyncResult.PermissionsRevoked)
    }

    @Test
    fun returnsNullWhenHealthyAndGranted() {
        assertThat(
            HealthConnectRevocationEvaluator.evaluate(
                launchFlagEnabled = true,
                userOptIn = true,
                permissionsPreviouslyGranted = false,
                allPermissionsGranted = true,
            ),
        ).isNull()
    }
}
