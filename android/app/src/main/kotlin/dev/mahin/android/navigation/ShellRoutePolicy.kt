package dev.mahin.android.navigation

import dev.mahin.android.R
import dev.mahin.core.model.ReproductiveMode

/** Route validity for the app shell (tabs, secondaries, mode transitions). */
object ShellRoutePolicy {
    private val allTabRoutes: Set<String> = MahinTopLevelDestination.entries.map { it.route }.toSet()

    fun tabRoutesForMode(mode: ReproductiveMode): Set<String> =
        MahinTopLevelDestination.forMode(mode).map { it.route }.toSet()

    fun isTopLevelTabRoute(route: String?): Boolean = route != null && route in allTabRoutes

    fun isOrphanTabRoute(
        route: String?,
        mode: ReproductiveMode,
    ): Boolean {
        if (route == null || route !in allTabRoutes) return false
        if (route == MahinTopLevelDestination.Calendar.route && mode == ReproductiveMode.PREGNANT) {
            return false
        }
        return route !in tabRoutesForMode(mode)
    }

    fun isSecondaryRoute(
        route: String?,
        mode: ReproductiveMode,
    ): Boolean {
        if (route == null) return false
        return when (route) {
            MahinTopLevelDestination.HISTORY_ROUTE,
            MahinTopLevelDestination.SETTINGS_ROUTE,
            -> true
            MahinTopLevelDestination.Calendar.route -> mode == ReproductiveMode.PREGNANT
            else -> false
        }
    }

    fun showsShellBottomBar(
        route: String?,
        mode: ReproductiveMode,
    ): Boolean = route != null && route in tabRoutesForMode(mode)

    fun shellTitleRes(
        route: String?,
        @Suppress("UNUSED_PARAMETER") mode: ReproductiveMode,
        showBottomBar: Boolean,
    ): Int? =
        when (route) {
            MahinTopLevelDestination.HISTORY_ROUTE -> R.string.history_title
            MahinTopLevelDestination.SETTINGS_ROUTE -> R.string.settings_title
            MahinTopLevelDestination.Calendar.route ->
                if (!showBottomBar) {
                    R.string.calendar_title
                } else {
                    null
                }
            else -> MahinTopLevelDestination.titleResForRoute(route)
        }
}
