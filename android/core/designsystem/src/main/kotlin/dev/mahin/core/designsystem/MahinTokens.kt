package dev.mahin.core.designsystem

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object MahinSpacing {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 40.dp
    val xxxl: Dp = 48.dp
}

object MahinRadius {
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
}

enum class MahinTypographyRole {
    Display,
    NumericDisplay,
    TitleLarge,
    Title,
    BodyLarge,
    Body,
    Label,
    Caption,
}
