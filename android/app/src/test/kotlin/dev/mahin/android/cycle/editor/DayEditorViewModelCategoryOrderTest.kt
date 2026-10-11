package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.LogEditorCategory
import dev.mahin.domain.cycle.LogCategoryUsageOrder
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
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

    @Test
    fun loadDay_derivesSymptomUsageAbovePeriodFromRoomHistory() {
        val day = LocalDate.of(2025, 6, 1)
        val harness = DayEditorTestHarness.create(day)
        runBlocking {
            harness.seedCycleProfile()
            repeat(5) { offset ->
                val d = day.minusDays(offset.toLong() + 1)
                harness.cycleRepository.upsertDailyLog(
                    date = d,
                    moodTags = emptySet(),
                    symptomTags = setOf("خستگی"),
                    painSeverity = null,
                    painLocation = null,
                    painNote = null,
                    note = null,
                )
            }
            val vm = harness.createViewModel()
            vm.openDate(day)
            awaitUntil {
                val counts = vm.uiState.value.categoryUsageCounts
                (counts[LogEditorCategory.SYMPTOMS] ?: 0) > (counts[LogEditorCategory.PERIOD_FLOW] ?: 0)
            }
        }
        harness.close()
    }

    @Test
    fun moveCategoryEarlier_promotesPinnedCategory() {
        val day = LocalDate.of(2025, 6, 2)
        val harness = DayEditorTestHarness.create(day)
        runBlocking {
            harness.seedCycleProfile()
            val vm = harness.createViewModel()
            vm.openDate(day)
            awaitUntil {
                vm.uiState.value.orderedCategories
                    .isNotEmpty()
            }
            val before = vm.uiState.value.orderedCategories
            val target = before.last()
            vm.moveCategoryEarlier(target)
            awaitUntil {
                val after = vm.uiState.value.orderedCategories
                after.indexOf(target) < before.indexOf(target)
            }
        }
        harness.close()
    }

    private suspend fun awaitUntil(
        timeoutMs: Long = 5_000,
        condition: () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            ShadowLooper.idleMainLooper()
        }
        throw AssertionError("Condition not met within ${timeoutMs}ms")
    }
}
