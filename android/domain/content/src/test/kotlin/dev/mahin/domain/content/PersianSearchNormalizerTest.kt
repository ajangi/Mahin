package dev.mahin.domain.content

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PersianSearchNormalizerTest {
    @Test
    fun normalizesYaKehehVariants() {
        assertThat(PersianSearchNormalizer.normalize("علائم يك")).isEqualTo("علائم یک")
    }
}
