package dev.mahin.core.security

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.datastore.AppLockMode
import dev.mahin.core.datastore.AppLockPreferencesSnapshot
import org.junit.Test

class AppLockSessionStateTest {
    @Test
    fun coldStartIsFailClosed() {
        val state = AppLockSessionState()
        assertThat(state.shouldShowLoading()).isTrue()
        assertThat(state.shouldShowGate()).isTrue()
        assertThat(state.effectiveSessionUnlocked()).isFalse()
    }

    @Test
    fun pinModeColdStartAfterPrefsLoadRequiresUnlock() {
        val loaded =
            AppLockSessionState().onPreferencesLoaded(
                AppLockPreferencesSnapshot(mode = AppLockMode.PIN),
            )
        assertThat(loaded.shouldShowGate()).isTrue()
        val unlocked = loaded.markUnlocked()
        assertThat(unlocked.shouldShowGate()).isFalse()
        val lockedAgain = unlocked.lockSession()
        assertThat(lockedAgain.shouldShowGate()).isTrue()
    }

    @Test
    fun disabledModeNeverShowsGateAfterLoad() {
        val loaded =
            AppLockSessionState().onPreferencesLoaded(
                AppLockPreferencesSnapshot(mode = AppLockMode.DISABLED),
            )
        assertThat(loaded.shouldShowGate()).isFalse()
    }
}
