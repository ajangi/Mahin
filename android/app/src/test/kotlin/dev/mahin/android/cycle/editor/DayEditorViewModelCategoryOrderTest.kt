package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.LogEditorCategory
import dev.mahin.domain.cycle.LogCategoryUsageOrder
import org.junit.Test

class DayEditorViewModelCategoryOrderTest {
    @Test
    fun pinnedCategories_stayAheadOfMostUsed() {
        val ordered =
            LogCategoryUsageOrder.orderedCategories(
                enabled =
                    setOf(
                        LogEditorCategory.PERIOD_FLOW,
                        LogEditorCategory.SYMPTOMS,
                        LogEditorCategory.MOOD,
                    ),
                pinnedOrder = listOf(LogEditorCategory.MOOD),
                usageCounts =
                    mapOf(
                        LogEditorCategory.SYMPTOMS to 10,
                        LogEditorCategory.PERIOD_FLOW to 1,
                    ),
                mostUsedFirst = true,
            )
        assertThat(ordered.first()).isEqualTo(LogEditorCategory.MOOD)
        assertThat(ordered.drop(1).first()).isEqualTo(LogEditorCategory.SYMPTOMS)
    }
}
