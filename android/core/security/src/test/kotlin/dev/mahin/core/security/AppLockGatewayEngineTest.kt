package dev.mahin.core.security

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.datastore.AppLockMode
import dev.mahin.core.datastore.AppLockPreferencesSnapshot
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppLockGatewayEngineTest {
    @Test
    fun pinPrefsStayLockedUntilUnlockAndRelockAfterBackground() =
        runTest {
            val engine =
                AppLockGatewayEngine(
                    flowOf(AppLockPreferencesSnapshot(mode = AppLockMode.PIN)),
                    this,
                )
            advanceUntilIdle()
            assertThat(engine.shouldShowLockGate()).isTrue()
            engine.markSessionUnlocked()
            assertThat(engine.shouldShowLockGate()).isFalse()
            engine.lockSession()
            assertThat(engine.shouldShowLockGate()).isTrue()
        }

    @Test
    fun prefsEmissionWhileLockedDoesNotClearLockSession() =
        runTest {
            val prefs = MutableSharedFlow<AppLockPreferencesSnapshot>(replay = 1)
            prefs.emit(AppLockPreferencesSnapshot(mode = AppLockMode.PIN))
            val engine = AppLockGatewayEngine(prefs, backgroundScope)
            advanceUntilIdle()
            engine.lockSession()
            assertThat(engine.shouldShowLockGate()).isTrue()
            prefs.emit(
                AppLockPreferencesSnapshot(
                    mode = AppLockMode.PIN,
                    hideRecentsWhenLocked = false,
                ),
            )
            advanceUntilIdle()
            assertThat(engine.shouldShowLockGate()).isTrue()
        }
}
