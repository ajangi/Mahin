package dev.mahin.domain.cycle

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CycleDomainModuleTest {
    @Test
    fun algorithmVersionIsExplicit() {
        assertThat(CycleDomainModule.ALGORITHM_VERSION_PLACEHOLDER).isEqualTo("unspecified")
    }
}
