package dev.mahin.core.designsystem

import androidx.compose.ui.graphics.Color
import dev.mahin.core.model.MahinTokenHex

private fun hex(value: String): Color {
    val digits = value.removePrefix("#")
    return Color(("FF$digits").toLong(16))
}

object MahinLightColors {
    val brandPrimary: Color = hex(MahinTokenHex.LIGHT_BRAND_PRIMARY)
    val brandPrimaryPressed: Color = hex(MahinTokenHex.LIGHT_BRAND_PRIMARY_PRESSED)
    val brandPrimarySoft: Color = hex(MahinTokenHex.LIGHT_BRAND_PRIMARY_SOFT)
    val surfaceBackground: Color = hex(MahinTokenHex.LIGHT_SURFACE_BACKGROUND)
    val surfaceDefault: Color = hex(MahinTokenHex.LIGHT_SURFACE_DEFAULT)
    val surfaceSecondary: Color = hex(MahinTokenHex.LIGHT_SURFACE_SECONDARY)
    val textPrimary: Color = hex(MahinTokenHex.LIGHT_TEXT_PRIMARY)
    val textSecondary: Color = hex(MahinTokenHex.LIGHT_TEXT_SECONDARY)
    val healthPeriod: Color = hex(MahinTokenHex.LIGHT_HEALTH_PERIOD)
    val healthFertility: Color = hex(MahinTokenHex.LIGHT_HEALTH_FERTILITY)
    val healthOvulation: Color = hex(MahinTokenHex.LIGHT_HEALTH_OVULATION)
    val healthPregnancy: Color = hex(MahinTokenHex.LIGHT_HEALTH_PREGNANCY)
    val statusPositive: Color = hex(MahinTokenHex.LIGHT_STATUS_POSITIVE)
    val statusWarning: Color = hex(MahinTokenHex.LIGHT_STATUS_WARNING)
    val statusCritical: Color = hex(MahinTokenHex.LIGHT_STATUS_CRITICAL)
}

object MahinDarkColors {
    val surfaceBackground: Color = hex(MahinTokenHex.DARK_SURFACE_BACKGROUND)
    val surfaceDefault: Color = hex(MahinTokenHex.DARK_SURFACE_DEFAULT)
    val surfaceElevated: Color = hex(MahinTokenHex.DARK_SURFACE_ELEVATED)
    val brandPrimary: Color = hex(MahinTokenHex.DARK_BRAND_PRIMARY)
    val textPrimary: Color = hex(MahinTokenHex.DARK_TEXT_PRIMARY)
    val textSecondary: Color = hex(MahinTokenHex.DARK_TEXT_SECONDARY)
}
