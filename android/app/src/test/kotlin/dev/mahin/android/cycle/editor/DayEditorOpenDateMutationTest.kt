package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.PainLocation
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DayEditorOpenDateMutationTest {
    private val day = LocalDate.of(2025, 8, 15)

    @After
    fun tearDown() {
        harness?.close()
    }

    private var harness: DayEditorTestHarness? = null

    @Test
    fun setPain_immediatelyAfterOpenDate_doesNotNoOp() {
        harness = DayEditorTestHarness.create(day)
        runBlocking { harness!!.seedCycleProfile() }
        val viewModel = harness!!.createViewModel()
        runBlocking {
            viewModel.openDate(day)
            viewModel.setPain(PainLocation.HEAD, 4)
            awaitDailyLogOnMain { viewModel.uiState.value.painSeverity == 4 }
            assertThat(harness!!.dailyLogFor(day)?.painSeverity).isEqualTo(4)
        }
    }
}
