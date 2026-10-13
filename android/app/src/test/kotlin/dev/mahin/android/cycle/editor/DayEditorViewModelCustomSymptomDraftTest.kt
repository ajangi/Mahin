package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DayEditorViewModelCustomSymptomDraftTest {
    private lateinit var harness: DayEditorTestHarness
    private lateinit var viewModel: DayEditorViewModel
    private val day = LocalDate.of(2025, 5, 6)
    private val label = "سردرد شخصی"

    @Before
    fun setUp() {
        harness = DayEditorTestHarness.create(day)
        runBlocking { harness.seedCycleProfile() }
        viewModel = harness.createViewModel()
        runBlocking {
            viewModel.openDate(day)
            awaitUntil { viewModel.uiState.value.date == day }
        }
    }

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun addCustomSymptomFromDraft_rejectsCommaInLabel() {
        runBlocking {
            viewModel.setCustomSymptomDraft("سردرد,شخصی")
            viewModel.addCustomSymptomFromDraft()
            awaitUntil {
                val log = harness.dailyLogFor(day)
                log == null || log.localCustomSymptomTags.isEmpty()
            }
            assertThat(harness.dailyLogFor(day)).isNull()
        }
    }

    @Test
    fun addCustomSymptomFromDraft_persistsAndClearsDraft() {
        runBlocking {
            viewModel.setCustomSymptomDraft(label)
            viewModel.addCustomSymptomFromDraft()
            awaitDailyLogOnMain {
                val log = harness.dailyLogFor(day)
                log?.localCustomSymptomTags?.contains(label) == true
            }
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
        }
    }

    private suspend fun awaitUntil(
        timeoutMs: Long = 5_000,
        condition: () -> Boolean,
    ) = awaitDailyLogOnMain(timeoutMs, condition)
}
