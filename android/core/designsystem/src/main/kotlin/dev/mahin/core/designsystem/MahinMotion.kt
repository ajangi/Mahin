package dev.mahin.core.designsystem

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * When true, motion durations should collapse to [MahinMotionDuration.INSTANT_MS]
 * (system animator-duration scale is 0 / remove animations).
 */
val LocalReducedMotion = compositionLocalOf { false }

@Composable
fun rememberSystemReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        }.getOrElse { false }
    }
}

/**
 * Effective motion duration in milliseconds, honouring [LocalReducedMotion] and
 * [rememberSystemReducedMotion] when [respectSystemSetting] is true.
 */
@Composable
fun mahinMotionDurationMs(
    tokenMs: Int,
    respectSystemSetting: Boolean = true,
): Int {
    val reduced =
        LocalReducedMotion.current ||
            (respectSystemSetting && rememberSystemReducedMotion())
    return if (reduced) MahinMotionDuration.INSTANT_MS else tokenMs
}

@Composable
fun ProvideReducedMotion(
    reducedMotion: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalReducedMotion provides reducedMotion) {
        content()
    }
}
