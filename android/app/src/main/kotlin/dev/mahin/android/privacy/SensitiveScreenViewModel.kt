package dev.mahin.android.privacy

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.security.AppLockGateway
import javax.inject.Inject

@HiltViewModel
class SensitiveScreenViewModel
    @Inject
    constructor(
        private val appLockGateway: AppLockGateway,
    ) : ViewModel() {
        fun blockScreenshotsEnabled(): Boolean = appLockGateway.shouldBlockScreenshots()
    }
