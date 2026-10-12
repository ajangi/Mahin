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
    fun mostUsedFirst_ordersUnpinnedByUsage_keepsPinnedFirst() {
        val enabled =
            setOf(
                LogEditorCategory.SYMPTOMS,
                LogEditorCategory.MOOD,
                LogEditorCategory.PAIN,
                LogEditorCategory.NOTES,
            )
        val order =
            LogCategoryUsageOrder.orderedCategories(
                enabled = enabled,
                pinnedOrder = listOf(LogEditorCategory.PAIN, LogEditorCategory.SYMPTOMS),
                usageCounts =
                    mapOf(
                        LogEditorCategory.MOOD to 5,
                        LogEditorCategory.NOTES to 2,
                        LogEditorCategory.SYMPTOMS to 1,
                    ),
                mostUsedFirst = true,
            )
        assertThat(order.take(2)).containsExactly(LogEditorCategory.PAIN, LogEditorCategory.SYMPTOMS).inOrder()
        assertThat(order.drop(2).first()).isEqualTo(LogEditorCategory.MOOD)
        assertThat(order.last()).isEqualTo(LogEditorCategory.NOTES)
    }
}
