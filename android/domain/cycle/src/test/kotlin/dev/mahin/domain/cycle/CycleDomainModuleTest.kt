package dev.mahin.domain.cycle

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CycleDomainModuleTest {
    @Test
    fun algorithmVersionIsExplicit() {
        assertThat(CycleDomainModule.PREDICTION_ALGORITHM_VERSION).isEqualTo("cycle-prediction-v1")
    }
}
