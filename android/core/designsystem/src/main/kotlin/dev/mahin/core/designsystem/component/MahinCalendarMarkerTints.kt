package dev.mahin.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.mahin.core.designsystem.LocalMahinDarkTheme
import dev.mahin.core.designsystem.LocalMahinExtendedColors

/**
 * Contrast-aware calendar cell tints (ADR 0020). Uses [LocalMahinDarkTheme], not system theme.
 */
object MahinCalendarMarkerTints {
    @Composable
    fun periodLogged(): Color {
        val extended = LocalMahinExtendedColors.current
        return extended.healthPeriod.copy(alpha = if (LocalMahinDarkTheme.current) 0.55f else 0.45f)
    }

    @Composable
    fun periodPredictedFill(): Color {
        val extended = LocalMahinExtendedColors.current
        return extended.healthPeriod.copy(alpha = if (LocalMahinDarkTheme.current) 0.18f else 0.12f)
    }

    @Composable
    fun fertileWindow(): Color {
        val extended = LocalMahinExtendedColors.current
        return extended.healthFertility.copy(alpha = if (LocalMahinDarkTheme.current) 0.42f else 0.28f)
    }

    @Composable
    fun estimatedOvulation(): Color {
        val extended = LocalMahinExtendedColors.current
        return extended.healthOvulation.copy(alpha = if (LocalMahinDarkTheme.current) 0.72f else 0.85f)
    }

    @Composable
    fun periodPredictedBorder(): Color = LocalMahinExtendedColors.current.healthPeriod
}
