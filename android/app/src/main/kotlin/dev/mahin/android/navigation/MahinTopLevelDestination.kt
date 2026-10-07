package dev.mahin.android.navigation

import androidx.annotation.StringRes
import dev.mahin.android.R
import dev.mahin.core.designsystem.component.MahinNavTab
import dev.mahin.core.designsystem.icon.MahinIcons
import dev.mahin.core.model.ReproductiveMode

enum class MahinTopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: dev.mahin.core.designsystem.icon.MahinIconSpec,
) {
    Today("today", R.string.nav_today, MahinIcons.Nav.today),
    Calendar("calendar", R.string.nav_calendar, MahinIcons.Nav.calendar),
    Log("log", R.string.nav_log, MahinIcons.Nav.log),
    CycleInsights("cycle_insights", R.string.nav_cycle_insights, MahinIcons.Nav.insights),
    TtcInsights("ttc_insights", R.string.nav_ttc_insights, MahinIcons.Nav.insights),
    PregnancyHub("pregnancy_hub", R.string.nav_pregnancy_hub, MahinIcons.Nav.pregnancy),
    Plan("plan", R.string.nav_plan, MahinIcons.Nav.plan),
    Learn("learn", R.string.nav_learn, MahinIcons.Nav.learn),
    ;

    fun toNavTab(): MahinNavTab =
        MahinNavTab(
            route = route,
            labelRes = labelRes,
            icon = icon,
        )

    companion object {
        const val HISTORY_ROUTE: String = "history"
        const val SETTINGS_ROUTE: String = "settings"

        fun forMode(mode: ReproductiveMode): List<MahinTopLevelDestination> =
            when (mode) {
                ReproductiveMode.TRYING_TO_CONCEIVE ->
                    listOf(Today, Calendar, Log, TtcInsights, Learn)
                ReproductiveMode.PREGNANT ->
                    listOf(Today, PregnancyHub, Log, Plan, Learn)
                ReproductiveMode.CYCLE_TRACKING,
                ReproductiveMode.POST_PREGNANCY_TRANSITION,
                ReproductiveMode.TRACKING_PAUSED,
                -> listOf(Today, Calendar, Log, CycleInsights, Learn)
            }

        fun titleResForRoute(route: String?): Int? = entries.firstOrNull { it.route == route }?.labelRes
    }
}
