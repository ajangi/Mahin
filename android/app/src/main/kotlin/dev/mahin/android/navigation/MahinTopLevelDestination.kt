package dev.mahin.android.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Today
import androidx.compose.ui.graphics.vector.ImageVector
import dev.mahin.android.R

enum class MahinTopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Today("today", R.string.nav_today, Icons.Outlined.Today),
    Calendar("calendar", R.string.nav_calendar, Icons.Outlined.CalendarMonth),
    Showcase("showcase", R.string.nav_showcase, Icons.Outlined.Palette),
}
