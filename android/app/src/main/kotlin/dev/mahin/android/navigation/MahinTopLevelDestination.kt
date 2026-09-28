package dev.mahin.android.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Today
import androidx.compose.ui.graphics.vector.ImageVector
import dev.mahin.android.R
import dev.mahin.core.model.ReproductiveMode

enum class MahinTopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Today("today", R.string.nav_today, Icons.Outlined.Today),
    Calendar("calendar", R.string.nav_calendar, Icons.Outlined.CalendarMonth),
    Log("log", R.string.nav_log, Icons.Outlined.EditNote),
    History("history", R.string.nav_history, Icons.Outlined.History),
    CycleInsights("cycle_insights", R.string.nav_cycle_insights, Icons.Outlined.ShowChart),
    TtcInsights("ttc_insights", R.string.nav_ttc_insights, Icons.Outlined.ShowChart),
    PregnancyHub("pregnancy_hub", R.string.nav_pregnancy_hub, Icons.Outlined.FavoriteBorder),
    Learn("learn", R.string.nav_learn, Icons.Outlined.MenuBook),
    ;

    companion object {
        fun forMode(mode: ReproductiveMode): List<MahinTopLevelDestination> =
            when (mode) {
                ReproductiveMode.TRYING_TO_CONCEIVE ->
                    listOf(Today, Calendar, Log, TtcInsights, Learn, History)
                ReproductiveMode.PREGNANT ->
                    listOf(Today, Calendar, Log, PregnancyHub, Learn, History)
                ReproductiveMode.CYCLE_TRACKING ->
                    listOf(Today, Calendar, Log, CycleInsights, Learn)
                else -> listOf(Today, Calendar, Log, Learn, History)
            }
    }
}
