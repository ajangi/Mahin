package dev.mahin.domain.cycle

import dev.mahin.core.model.LogEditorCategory

/**
 * Deterministic local-only ordering for the day editor. Pinned order wins; optional
 * most-used-first breaks ties using stable category enum order.
 */
object LogCategoryUsageOrder {
    fun orderedCategories(
        enabled: Set<LogEditorCategory>,
        pinnedOrder: List<LogEditorCategory>,
        usageCounts: Map<LogEditorCategory, Int>,
        mostUsedFirst: Boolean,
    ): List<LogEditorCategory> {
        val pinnedFiltered = pinnedOrder.filter { it in enabled }
        val unpinned = enabled.filter { it !in pinnedFiltered.toSet() }
        val unpinnedOrdered =
            if (mostUsedFirst) {
                unpinned.sortedWith(
                    compareByDescending<LogEditorCategory> { usageCounts[it] ?: 0 }
                        .thenBy { it.ordinal },
                )
            } else {
                unpinned.sortedBy { it.ordinal }
            }
        return pinnedFiltered + unpinnedOrdered
    }
}
