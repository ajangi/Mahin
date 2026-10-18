package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.testing.MainDispatcherRule
import java.time.LocalDate
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DayEditorViewModelCustomSymptomDraftTest {
    private val testDispatcher = StandardTestDispatcher()
    private val day = LocalDate.of(2025, 5, 6)
    private val label = "سردرد شخصی"

    @get:Rule
    val mainDispatcherRule =
        MainDispatcherRule(
            testDispatcher,
            beforeResetMain = {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
            },
        )

    @Test
    fun addCustomSymptomFromDraft_rejectsCommaInLabel() =
        runTest(testDispatcher) {
            val harness = DayEditorTestHarness.create(day)
            try {
                harness.seedCycleProfile()
                val viewModel = harness.createViewModel()
                viewModel.openDateAndAwait(day)
                viewModel.setCustomSymptomDraft("سردرد,شخصی")
                viewModel.addCustomSymptomFromDraft()
                assertThat(harness.dailyLogFor(day)).isNull()
            } finally {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
                harness.close()
            }
        }

    @Test
    fun addCustomSymptomFromDraft_persistsAndClearsDraft() =
        runTest(testDispatcher) {
            val harness = DayEditorTestHarness.create(day)
            try {
                harness.seedCycleProfile()
                val viewModel = harness.createViewModel()
                viewModel.openDateAndAwait(day)
                viewModel.setCustomSymptomDraft(label)
                viewModel.addCustomSymptomFromDraft()
                harness.awaitDailyLog(day) { it.localCustomSymptomTags.contains(label) }
                assertThat(viewModel.uiState.value.customSymptomDraft).isEmpty()
                viewModel.setCustomSymptomDraft(label)
                viewModel.addCustomSymptomFromDraft()
                val logAfterDup = harness.dailyLogFor(day)
                assertThat(
                    logAfterDup
                        ?.localCustomSymptomTags
                        ?.split(",")
                        ?.filter { it.isNotBlank() }
                        ?.size,
                ).isEqualTo(1)
            } finally {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
                harness.close()
            }
        }
}
