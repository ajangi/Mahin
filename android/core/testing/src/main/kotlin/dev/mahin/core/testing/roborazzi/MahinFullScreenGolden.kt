package dev.mahin.core.testing.roborazzi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.designsystem.ProvideReducedMotion

/**
 * Renders [content] as a full-screen fa-IR RTL surface under [MahinTheme] and captures a Roborazzi golden.
 *
 * Uses the qualifier-provided screen [Density] (e.g. xxhdpi) and only overrides [fontScale].
 */
@OptIn(ExperimentalRoborazziApi::class)
fun ComposeContentTestRule.captureMahinFullScreenGolden(
    darkTheme: Boolean,
    fontScale: Float,
    beforeCapture: (ComposeContentTestRule.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    setContent {
        val baseDensity = LocalDensity.current
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl,
            LocalDensity provides
                Density(
                    density = baseDensity.density,
                    fontScale = fontScale,
                ),
        ) {
            MahinTheme(darkTheme = darkTheme) {
                ProvideReducedMotion(reducedMotion = true) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        content()
                    }
                }
            }
        }
    }
    waitForIdle()
    beforeCapture?.invoke(this)
    waitForIdle()
    onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
}
