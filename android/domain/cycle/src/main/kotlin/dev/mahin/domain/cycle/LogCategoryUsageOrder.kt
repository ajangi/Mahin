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
        val pinned =
            pinnedOrder.filter { it in enabled } +
                enabled.filter { it !in pinnedOrder }.sortedBy { it.ordinal }
        if (!mostUsedFirst) return pinned
        return pinned.sortedWith(
            compareByDescending<LogEditorCategory> { usageCounts[it] ?: 0 }
                .thenBy { it.ordinal },
        )
    }
}
