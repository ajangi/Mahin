package dev.mahin.android.shell

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.mahin.core.designsystem.MahinThemeTokens
import dev.mahin.core.model.ReproductiveMode

@Composable
fun reproductiveModeShellAccent(mode: ReproductiveMode): Color {
    val extended = MahinThemeTokens.extendedColors
    return when (mode) {
        ReproductiveMode.TRYING_TO_CONCEIVE -> extended.healthFertility
        ReproductiveMode.PREGNANT -> extended.healthPregnancy
        ReproductiveMode.CYCLE_TRACKING,
        ReproductiveMode.POST_PREGNANCY_TRANSITION,
        ReproductiveMode.TRACKING_PAUSED,
        -> extended.healthPeriod
    }
}
