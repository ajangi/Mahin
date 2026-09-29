package dev.mahin.core.security

import dev.mahin.core.datastore.AppLockMode
import dev.mahin.core.datastore.AppLockPreferencesRepository
import dev.mahin.core.datastore.AppLockPreferencesSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Singleton
class DefaultAppLockGateway
    @Inject
    constructor(
        private val preferencesRepository: AppLockPreferencesRepository,
    ) : AppLockGateway {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        @Volatile
        private var sessionState: AppLockSessionState = AppLockSessionState()

        private val _sessionVersion = MutableStateFlow(0)
        override val sessionRevision: StateFlow<Int> = _sessionVersion.asStateFlow()

        init {
            scope.launch {
                preferencesRepository.snapshot.collect { snapshot ->
                    sessionState = sessionState.onPreferencesLoaded(snapshot)
                    bumpSessionVersion()
                }
            }
        }

        override val isLockEnabled: Boolean
            get() = sessionState.isLockEnabled()

        override fun arePreferencesLoaded(): Boolean = sessionState.preferencesLoaded

        override fun shouldShowLockGate(): Boolean = sessionState.shouldShowGate()

        override fun lockSettings(): AppLockSettings {
            val enabled = sessionState.preferencesLoaded && sessionState.snapshot.mode != AppLockMode.DISABLED
            return AppLockSettings(
                enabled = enabled,
                hideRecentsPreview =
                    (!sessionState.preferencesLoaded || sessionState.snapshot.hideRecentsWhenLocked) && enabled,
                blockScreenshots =
                    (
                        !sessionState.preferencesLoaded ||
                            sessionState.snapshot.blockScreenshotsOnSensitiveScreens
                    ) && enabled,
            )
        }

        override fun isSessionUnlocked(): Boolean = sessionState.effectiveSessionUnlocked()

        override fun markSessionUnlocked() {
            sessionState = sessionState.markUnlocked()
            bumpSessionVersion()
        }

        override fun lockSession() {
            sessionState = sessionState.lockSession()
            bumpSessionVersion()
        }

        override fun shouldHideRecentsPreview(): Boolean = lockSettings().hideRecentsPreview

        override fun shouldBlockScreenshots(): Boolean = lockSettings().blockScreenshots

        private fun bumpSessionVersion() {
            _sessionVersion.value = _sessionVersion.value + 1
        }
    }
