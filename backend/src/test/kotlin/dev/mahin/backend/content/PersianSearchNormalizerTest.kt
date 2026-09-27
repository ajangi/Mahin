package dev.mahin.backend.content

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PersianSearchNormalizerTest {
    @Test
    fun normalizesArabicYaAndKeheh() {
        val normalized = PersianSearchNormalizer.normalize("علائم يك هفته")
        assertEquals("علائم یک هفته", normalized)
    }
}
