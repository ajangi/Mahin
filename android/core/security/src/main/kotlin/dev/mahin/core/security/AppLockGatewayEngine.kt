package dev.mahin.core.security

import dev.mahin.core.datastore.AppLockMode
import dev.mahin.core.datastore.AppLockPreferencesSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Thread-safe app-lock session; [DefaultAppLockGateway] delegates here.
 * Exposed for unit tests with a fake preferences [Flow].
 */
internal class AppLockGatewayEngine(
    preferences: Flow<AppLockPreferencesSnapshot>,
    scope: CoroutineScope,
) : AppLockGateway {
    private val sessionState = MutableStateFlow(AppLockSessionState())
    private val sessionRevisionState = MutableStateFlow(0)

    override val sessionRevision: StateFlow<Int> = sessionRevisionState.asStateFlow()

    init {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            preferences.collect { snapshot ->
                sessionState.update { current -> current.onPreferencesLoaded(snapshot) }
                sessionRevisionState.update { revision -> revision + 1 }
            }
        }
    }

    override val isLockEnabled: Boolean
        get() = sessionState.value.isLockEnabled()

    override fun arePreferencesLoaded(): Boolean = sessionState.value.preferencesLoaded

    override fun shouldShowLockGate(): Boolean = sessionState.value.shouldShowGate()

    override fun lockSettings(): AppLockSettings {
        val state = sessionState.value
        val enabled = state.preferencesLoaded && state.snapshot.mode != AppLockMode.DISABLED
        return AppLockSettings(
            enabled = enabled,
            hideRecentsPreview =
                (!state.preferencesLoaded || state.snapshot.hideRecentsWhenLocked) && enabled,
            blockScreenshots =
                (!state.preferencesLoaded || state.snapshot.blockScreenshotsOnSensitiveScreens) &&
                    enabled,
        )
    }

    override fun isSessionUnlocked(): Boolean = sessionState.value.effectiveSessionUnlocked()

    override fun markSessionUnlocked() {
        sessionState.update { current -> current.markUnlocked() }
        sessionRevisionState.update { revision -> revision + 1 }
    }

    override fun lockSession() {
        sessionState.update { current -> current.lockSession() }
        sessionRevisionState.update { revision -> revision + 1 }
    }

    override fun shouldHideRecentsPreview(): Boolean = lockSettings().hideRecentsPreview

    override fun shouldBlockScreenshots(): Boolean = lockSettings().blockScreenshots
}
