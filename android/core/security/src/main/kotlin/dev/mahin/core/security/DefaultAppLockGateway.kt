package dev.mahin.core.security

import dev.mahin.core.datastore.AppLockMode
import dev.mahin.core.datastore.AppLockPreferencesRepository
import dev.mahin.core.datastore.AppLockPreferencesSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
        private var cachedSnapshot: AppLockPreferencesSnapshot = AppLockPreferencesSnapshot()

        @Volatile
        private var sessionUnlocked: Boolean = false

        init {
            scope.launch {
                var previousMode = cachedSnapshot.mode
                preferencesRepository.snapshot.collect { snapshot ->
                    cachedSnapshot = snapshot
                    when {
                        snapshot.mode == AppLockMode.DISABLED -> sessionUnlocked = true
                        previousMode == AppLockMode.DISABLED && snapshot.mode != AppLockMode.DISABLED -> {
                            sessionUnlocked = false
                        }
                    }
                    previousMode = snapshot.mode
                }
            }
        }

        override val isLockEnabled: Boolean
            get() = cachedSnapshot.mode != AppLockMode.DISABLED

        override fun lockSettings(): AppLockSettings =
            AppLockSettings(
                enabled = isLockEnabled,
                hideRecentsPreview = cachedSnapshot.hideRecentsWhenLocked && isLockEnabled,
                blockScreenshots = cachedSnapshot.blockScreenshotsOnSensitiveScreens && isLockEnabled,
            )

        override fun isSessionUnlocked(): Boolean = sessionUnlocked || !isLockEnabled

        override fun markSessionUnlocked() {
            sessionUnlocked = true
        }

        override fun lockSession() {
            if (isLockEnabled) {
                sessionUnlocked = false
            }
        }

        override fun shouldHideRecentsPreview(): Boolean = lockSettings().hideRecentsPreview

        override fun shouldBlockScreenshots(): Boolean = lockSettings().blockScreenshots
    }
