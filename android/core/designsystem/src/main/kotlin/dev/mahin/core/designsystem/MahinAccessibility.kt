package dev.mahin.core.designsystem

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** WCAG-oriented minimum interactive target (48dp). */
val MahinMinimumTouchTarget = 48.dp

fun Modifier.mahinMinimumTouchTarget(): Modifier =
    defaultMinSize(minWidth = MahinMinimumTouchTarget, minHeight = MahinMinimumTouchTarget)
