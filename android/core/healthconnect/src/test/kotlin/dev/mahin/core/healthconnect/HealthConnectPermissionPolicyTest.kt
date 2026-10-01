package dev.mahin.core.healthconnect

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HealthConnectPermissionPolicyTest {
    @Test
    fun requiredPermissionsAreMinimalMenstruationScopes() {
        assertThat(HealthConnectPermissionPolicy.requiredPermissions).hasSize(2)
    }

    @Test
    fun hasAllGrantedRequiresEveryPermissionString() {
        val required =
            HealthConnectPermissionPolicy.requiredPermissions
        assertThat(HealthConnectPermissionPolicy.hasAllGranted(required)).isTrue()
        assertThat(HealthConnectPermissionPolicy.hasAllGranted(emptySet())).isFalse()
    }
}
