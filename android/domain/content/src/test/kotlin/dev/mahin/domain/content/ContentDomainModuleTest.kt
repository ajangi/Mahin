package dev.mahin.domain.content

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ContentDomainModuleTest {
    @Test
    fun moduleExists() {
        assertThat(ContentDomainModule.java.simpleName).isEqualTo("ContentDomainModule")
    }
}
