package dev.mahin.domain.healthconnect

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HealthConnectRevocationEvaluatorTest {
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
}
