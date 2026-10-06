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

/** Motion durations from [design/tokens.json] `motionMs` (Compose [androidx.compose.animation.core.Duration]). */
object MahinMotionDuration {
    const val INSTANT_MS: Int = 0
    const val FAST_MS: Int = 150
    const val NORMAL_MS: Int = 250
    const val SLOW_MS: Int = 400
}

/**
 * CSS cubic-bezier strings from [design/tokens.json] `motionEasing`.
 * Map to Compose easing curves at call sites (M15+ transitions).
 */
object MahinMotionEasing {
    const val STANDARD = "cubic-bezier(0.2, 0.0, 0.0, 1.0)"
    const val EMPHASIZED = "cubic-bezier(0.2, 0.0, 0.0, 1.0)"
    const val DECELERATE = "cubic-bezier(0.0, 0.0, 0.0, 1.0)"
    const val ACCELERATE = "cubic-bezier(0.3, 0.0, 1.0, 1.0)"
}
