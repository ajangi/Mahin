package dev.mahin.domain.pregnancy

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PregnancyDomainModuleTest {
    @Test
    fun moduleExists() {
        assertThat(PregnancyDomainModule.java.simpleName).isEqualTo("PregnancyDomainModule")
    }
}
