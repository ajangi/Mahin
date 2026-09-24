package dev.mahin.android.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ShowChart
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
    Log("log", R.string.nav_log, Icons.Outlined.EditNote),
    History("history", R.string.nav_history, Icons.Outlined.History),
    TtcInsights("ttc_insights", R.string.nav_ttc_insights, Icons.Outlined.ShowChart),
    ;

    companion object {
        fun forMode(isTtcMode: Boolean): List<MahinTopLevelDestination> =
            if (isTtcMode) {
                listOf(Today, Calendar, Log, TtcInsights, History)
            } else {
                listOf(Today, Calendar, Log, History)
            }
    }
}
