package dev.mahin.backend.content

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ContentPublicVisibilityTest {
    @Test
    fun placeholderHiddenFromPublicApiInProduction() {
        assertTrue(ContentPublicVisibility.isPlaceholderHiddenInProduction(true, "production"))
        assertTrue(ContentPublicVisibility.isPlaceholderHiddenInProduction(true, "staging"))
        assertFalse(ContentPublicVisibility.isPlaceholderHiddenInProduction(true, "test"))
        assertFalse(ContentPublicVisibility.isPlaceholderHiddenInProduction(true, "local"))
        assertFalse(ContentPublicVisibility.isPlaceholderHiddenInProduction(false, "production"))
    }

    @Test
    fun placeholderHiddenUnlessEnvironmentIsExplicitlyLocalOrTest() {
        assertFalse(ContentPublicVisibility.isPlaceholderHiddenInProduction(true, "local"))
        assertFalse(ContentPublicVisibility.isPlaceholderHiddenInProduction(true, "test"))
        assertTrue(ContentPublicVisibility.isPlaceholderHiddenInProduction(true, "production"))
    }
}
