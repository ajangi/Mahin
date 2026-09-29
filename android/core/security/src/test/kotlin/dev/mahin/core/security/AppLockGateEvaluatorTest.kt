package dev.mahin.core.security

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.datastore.AppLockMode
import org.junit.Test

class AppLockGateEvaluatorTest {
    @Test
    fun coldStartShowsLoadingAndGate() {
        assertThat(AppLockGateEvaluator.shouldShowLoading(preferencesLoaded = false)).isTrue()
        assertThat(
            AppLockGateEvaluator.shouldShowGate(
                preferencesLoaded = false,
                mode = AppLockMode.DISABLED,
                sessionUnlocked = true,
            ),
        ).isTrue()
    }

    @Test
    fun pinModeRequiresUnlock() {
        assertThat(
            AppLockGateEvaluator.shouldShowGate(
                preferencesLoaded = true,
                mode = AppLockMode.PIN,
                sessionUnlocked = false,
            ),
        ).isTrue()
        assertThat(
            AppLockGateEvaluator.shouldShowGate(
                preferencesLoaded = true,
                mode = AppLockMode.PIN,
                sessionUnlocked = true,
            ),
        ).isFalse()
    }

    @Test
    fun lockSessionShowsGateAgain() {
        assertThat(
            AppLockGateEvaluator.shouldShowGate(
                preferencesLoaded = true,
                mode = AppLockMode.PIN,
                sessionUnlocked = false,
            ),
        ).isTrue()
    }
}
