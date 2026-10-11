package dev.mahin.domain.cycle

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.LogEditorCategory
import org.junit.Test

class LogCategoryUsageOrderTest {
    @Test
    fun pinnedOrder_preservesExplicitSequence() {
        val enabled =
            setOf(
                LogEditorCategory.SYMPTOMS,
                LogEditorCategory.MOOD,
                LogEditorCategory.PAIN,
            )
        val order =
            LogCategoryUsageOrder.orderedCategories(
                enabled = enabled,
                pinnedOrder = listOf(LogEditorCategory.PAIN, LogEditorCategory.SYMPTOMS, LogEditorCategory.MOOD),
                usageCounts = emptyMap(),
                mostUsedFirst = false,
            )
        assertThat(order)
            .containsExactly(
                LogEditorCategory.PAIN,
                LogEditorCategory.SYMPTOMS,
                LogEditorCategory.MOOD,
            ).inOrder()
    }

    @Test
    fun mostUsedFirst_breaksTiesByEnumOrdinal() {
        val enabled =
            setOf(
                LogEditorCategory.SYMPTOMS,
                LogEditorCategory.MOOD,
                LogEditorCategory.PAIN,
            )
        val order =
            LogCategoryUsageOrder.orderedCategories(
                enabled = enabled,
                pinnedOrder = enabled.toList(),
                usageCounts =
                    mapOf(
                        LogEditorCategory.SYMPTOMS to 3,
                        LogEditorCategory.MOOD to 3,
                        LogEditorCategory.PAIN to 1,
                    ),
                mostUsedFirst = true,
            )
        assertThat(order.first()).isEqualTo(LogEditorCategory.SYMPTOMS)
        assertThat(order[1]).isEqualTo(LogEditorCategory.MOOD)
        assertThat(order.last()).isEqualTo(LogEditorCategory.PAIN)
    }
}
