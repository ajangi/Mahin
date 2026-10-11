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
        return extended.healthPeriod.copy(
            alpha =
                if (LocalMahinDarkTheme.current) {
                    MahinCalendarMarkerTintAlphas.DARK_PERIOD
                } else {
                    MahinCalendarMarkerTintAlphas.LIGHT_PERIOD
                },
        )
    }

    @Composable
    fun periodPredictedFill(): Color {
        val extended = LocalMahinExtendedColors.current
        return extended.healthPeriod.copy(
            alpha =
                if (LocalMahinDarkTheme.current) {
                    MahinCalendarMarkerTintAlphas.DARK_PERIOD_PREDICTED
                } else {
                    MahinCalendarMarkerTintAlphas.LIGHT_PERIOD_PREDICTED
                },
        )
    }

    @Composable
    fun fertileWindow(): Color {
        val extended = LocalMahinExtendedColors.current
        return extended.healthFertility.copy(
            alpha =
                if (LocalMahinDarkTheme.current) {
                    MahinCalendarMarkerTintAlphas.DARK_FERTILE
                } else {
                    MahinCalendarMarkerTintAlphas.LIGHT_FERTILE
                },
        )
    }

    @Composable
    fun estimatedOvulation(): Color {
        val extended = LocalMahinExtendedColors.current
        return extended.healthOvulation.copy(
            alpha =
                if (LocalMahinDarkTheme.current) {
                    MahinCalendarMarkerTintAlphas.DARK_OVULATION
                } else {
                    MahinCalendarMarkerTintAlphas.LIGHT_OVULATION
                },
        )
    }

    @Composable
    fun periodPredictedBorder(): Color = LocalMahinExtendedColors.current.healthPeriod
}
