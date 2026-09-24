package dev.mahin.domain.content

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ContentDomainModuleTest {
    @Test
    fun moduleExists() {
        assertThat(ContentDomainModule::class.simpleName).isEqualTo("ContentDomainModule")
    }
}
