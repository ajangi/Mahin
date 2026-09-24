package dev.mahin.core.security

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppLockGatewayTest {
    @Test
    fun defaultLockIsOffUntilM9() {
        assertThat(DisabledAppLockGateway.isLockEnabled).isFalse()
        assertThat(DisabledAppLockGateway.shouldHideRecentsPreview()).isFalse()
    }
}
