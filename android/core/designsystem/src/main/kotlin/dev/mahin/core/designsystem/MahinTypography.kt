package dev.mahin.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.mahin.core.designsystem.R

val VazirmatnFontFamily =
    FontFamily(
        Font(R.font.vazirmatn_regular, FontWeight.Normal),
        Font(R.font.vazirmatn_medium, FontWeight.Medium),
        Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
        Font(R.font.vazirmatn_bold, FontWeight.Bold),
    )

fun mahinTypography(colorPrimary: androidx.compose.ui.graphics.Color): Typography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = VazirmatnFontFamily,
                fontSize = 40.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorPrimary,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = VazirmatnFontFamily,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        titleLarge =
            TextStyle(
                fontFamily = VazirmatnFontFamily,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Medium,
            ),
        titleMedium =
            TextStyle(
                fontFamily = VazirmatnFontFamily,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = VazirmatnFontFamily,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = VazirmatnFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
            ),
        labelLarge =
            TextStyle(
                fontFamily = VazirmatnFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            ),
        labelSmall =
            TextStyle(
                fontFamily = VazirmatnFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
    )

@Composable
fun mahinTextStyle(role: MahinTypographyRole): TextStyle {
    val typography = androidx.compose.material3.MaterialTheme.typography
    return when (role) {
        MahinTypographyRole.Display -> typography.displayLarge
        MahinTypographyRole.NumericDisplay ->
            typography.headlineLarge.copy(fontFeatureSettings = "tnum")
        MahinTypographyRole.TitleLarge -> typography.titleLarge
        MahinTypographyRole.Title -> typography.titleMedium
        MahinTypographyRole.BodyLarge -> typography.bodyLarge
        MahinTypographyRole.Body -> typography.bodyMedium
        MahinTypographyRole.Label -> typography.labelLarge
        MahinTypographyRole.Caption -> typography.labelSmall
    }
}
