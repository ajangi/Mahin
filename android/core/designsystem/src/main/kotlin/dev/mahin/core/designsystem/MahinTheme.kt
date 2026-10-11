package dev.mahin.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class MahinExtendedColors(
    val healthPeriod: Color,
    val healthFertility: Color,
    val healthOvulation: Color,
    val healthPregnancy: Color,
    val statusPositive: Color,
    val statusWarning: Color,
    val statusCritical: Color,
    val brandPrimarySoft: Color,
    val brandPrimaryPressed: Color,
)

val LocalMahinExtendedColors =
    staticCompositionLocalOf {
        MahinExtendedColors(
            healthPeriod = MahinLightColors.healthPeriod,
            healthFertility = MahinLightColors.healthFertility,
            healthOvulation = MahinLightColors.healthOvulation,
            healthPregnancy = MahinLightColors.healthPregnancy,
            statusPositive = MahinLightColors.statusPositive,
            statusWarning = MahinLightColors.statusWarning,
            statusCritical = MahinLightColors.statusCritical,
            brandPrimarySoft = MahinLightColors.brandPrimarySoft,
            brandPrimaryPressed = MahinLightColors.brandPrimaryPressed,
        )
    }

private fun lightScheme(): ColorScheme =
    lightColorScheme(
        primary = MahinLightColors.brandPrimary,
        onPrimary = Color.White,
        primaryContainer = MahinLightColors.brandPrimarySoft,
        onPrimaryContainer = MahinLightColors.textPrimary,
        secondary = MahinLightColors.healthFertility,
        onSecondary = Color.White,
        tertiary = MahinLightColors.healthPregnancy,
        onTertiary = MahinLightColors.textPrimary,
        background = MahinLightColors.surfaceBackground,
        onBackground = MahinLightColors.textPrimary,
        surface = MahinLightColors.surfaceDefault,
        onSurface = MahinLightColors.textPrimary,
        surfaceVariant = MahinLightColors.surfaceSecondary,
        onSurfaceVariant = MahinLightColors.textSecondary,
        error = MahinLightColors.statusCritical,
        onError = Color.White,
    )

private fun darkScheme(): ColorScheme =
    darkColorScheme(
        primary = MahinDarkColors.brandPrimary,
        onPrimary = MahinDarkColors.surfaceBackground,
        background = MahinDarkColors.surfaceBackground,
        onBackground = MahinDarkColors.textPrimary,
        surface = MahinDarkColors.surfaceDefault,
        onSurface = MahinDarkColors.textPrimary,
        surfaceVariant = MahinDarkColors.surfaceElevated,
        onSurfaceVariant = MahinDarkColors.textSecondary,
        error = MahinDarkColors.statusCritical,
        onError = MahinDarkColors.surfaceBackground,
    )

private val mahinShapes =
    Shapes(
        extraSmall = RoundedCornerShape(MahinRadius.sm),
        small = RoundedCornerShape(MahinRadius.sm),
        medium = RoundedCornerShape(MahinRadius.md),
        large = RoundedCornerShape(MahinRadius.lg),
        extraLarge = RoundedCornerShape(MahinRadius.xl),
    )

val LocalMahinDarkTheme = staticCompositionLocalOf { false }

@Composable
fun MahinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) darkScheme() else lightScheme()
    val typography = mahinTypography(scheme.onBackground)
    val reducedMotion = rememberSystemReducedMotion()
    val extended =
        if (darkTheme) {
            MahinExtendedColors(
                healthPeriod = MahinDarkColors.healthPeriod,
                healthFertility = MahinDarkColors.healthFertility,
                healthOvulation = MahinDarkColors.healthOvulation,
                healthPregnancy = MahinDarkColors.healthPregnancy,
                statusPositive = MahinDarkColors.statusPositive,
                statusWarning = MahinDarkColors.statusWarning,
                statusCritical = MahinDarkColors.statusCritical,
                brandPrimarySoft = MahinDarkColors.brandPrimary,
                brandPrimaryPressed = MahinLightColors.brandPrimaryPressed,
            )
        } else {
            MahinExtendedColors(
                healthPeriod = MahinLightColors.healthPeriod,
                healthFertility = MahinLightColors.healthFertility,
                healthOvulation = MahinLightColors.healthOvulation,
                healthPregnancy = MahinLightColors.healthPregnancy,
                statusPositive = MahinLightColors.statusPositive,
                statusWarning = MahinLightColors.statusWarning,
                statusCritical = MahinLightColors.statusCritical,
                brandPrimarySoft = MahinLightColors.brandPrimarySoft,
                brandPrimaryPressed = MahinLightColors.brandPrimaryPressed,
            )
        }
    CompositionLocalProvider(
        LocalMahinExtendedColors provides extended,
        LocalReducedMotion provides reducedMotion,
        LocalMahinDarkTheme provides darkTheme,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = typography,
            shapes = mahinShapes,
            content = content,
        )
    }
}

object MahinThemeTokens {
    val extendedColors: MahinExtendedColors
        @Composable
        get() = LocalMahinExtendedColors.current
}
