package dev.mahin.core.designsystem.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.mahin.core.designsystem.LocalMahinExtendedColors

/**
 * Contrast-aware calendar cell tints (ADR 0020). Dark theme uses higher alpha so day numbers stay readable.
 */
object MahinCalendarMarkerTints {
    @Composable
    fun periodLogged(): Color {
        val extended = LocalMahinExtendedColors.current
        return extended.healthPeriod.copy(alpha = if (isSystemInDarkTheme()) 0.55f else 0.45f)
    }

    @Composable
    fun periodPredictedFill(): Color {
        val extended = LocalMahinExtendedColors.current
        return extended.healthPeriod.copy(alpha = if (isSystemInDarkTheme()) 0.18f else 0.12f)
    }

    @Composable
    fun fertileWindow(): Color {
        val extended = LocalMahinExtendedColors.current
        return extended.healthFertility.copy(alpha = if (isSystemInDarkTheme()) 0.42f else 0.28f)
    }

    @Composable
    fun estimatedOvulation(): Color {
        val extended = LocalMahinExtendedColors.current
        return extended.healthOvulation.copy(alpha = if (isSystemInDarkTheme()) 0.5f else 0.35f)
    }

    @Composable
    fun periodPredictedBorder(): Color = LocalMahinExtendedColors.current.healthPeriod
}
