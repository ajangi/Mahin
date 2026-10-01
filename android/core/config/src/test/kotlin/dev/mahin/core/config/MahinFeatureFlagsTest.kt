package dev.mahin.core.config

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MahinFeatureFlagsTest {
    @Test
    fun healthConnectFlagKeyIsStable() {
        assertThat(MahinFeatureFlags.HEALTH_CONNECT).isEqualTo("health_connect")
    }
}
