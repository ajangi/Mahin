package dev.mahin.android

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import dagger.hilt.android.AndroidEntryPoint
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.notifications.ReminderCoordinator
import dev.mahin.core.security.AppLockGateway
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject lateinit var reminderCoordinator: ReminderCoordinator
    @Inject lateinit var appLockGateway: AppLockGateway

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reminderCoordinator.requestRefresh()
        applyRecentsPrivacy()
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme {
                    MahinRoot()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        appLockGateway.lockSession()
        applyRecentsPrivacy()
    }

    private fun applyRecentsPrivacy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(!appLockGateway.shouldHideRecentsPreview())
        }
    }
}
