package dev.mahin.core.security

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppLockGatewayTest {
    @Test
    fun disabledLockDoesNotRequireUnlock() {
        assertThat(DisabledAppLockGateway.isLockEnabled).isFalse()
        assertThat(DisabledAppLockGateway.requiresUnlockForSensitiveAction()).isFalse()
        assertThat(DisabledAppLockGateway.shouldHideRecentsPreview()).isFalse()
    }
}
