package dev.mahin.core.security

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.datastore.AppLockMode
import dev.mahin.core.datastore.AppLockPreferencesSnapshot
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
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

    @Test
    fun failClosedUntilFirstPreferenceEmission() =
        runTest {
            val prefsReady = CompletableDeferred<Unit>()
            val prefs =
                flow {
                    prefsReady.await()
                    emit(AppLockPreferencesSnapshot(mode = AppLockMode.PIN))
                }
            val engine = AppLockGatewayEngine(prefs, this)
            assertThat(engine.arePreferencesLoaded()).isFalse()
            assertThat(engine.shouldShowLockGate()).isTrue()
            prefsReady.complete(Unit)
            advanceUntilIdle()
            assertThat(engine.arePreferencesLoaded()).isTrue()
            assertThat(engine.shouldShowLockGate()).isTrue()
        }
}
