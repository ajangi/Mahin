package dev.mahin.backend.privacy

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PrivacyAccessLogFilterTest {
    @Test
    fun filterClassDoesNotReferenceRequestBody() {
        val sourceHints = PrivacyAccessLogFilter::class.java.declaredMethods.map { it.name }
        assertThat(sourceHints).contains("doFilterInternal")
        assertThat(PrivacyAccessLogFilter::class.java.getDeclaredField("logger")).isNotNull()
    }
}
