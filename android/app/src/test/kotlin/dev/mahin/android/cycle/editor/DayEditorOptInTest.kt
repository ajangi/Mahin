package dev.mahin.android.cycle.editor

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.ttc.TtcDayLogInput
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.ViewModelStoreClearingRule
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR", manifest = Config.NONE)
class DayEditorOptInTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var harness: DayEditorTestHarness
    private lateinit var viewModel: DayEditorViewModel
    private val day = LocalDate.of(2025, 1, 1)

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Before
    fun setUp() {
        harness = DayEditorTestHarness.create(day)
        runBlocking { harness.seedCycleProfile(ReproductiveMode.TRYING_TO_CONCEIVE) }
        viewModel = harness.createViewModel()
        runBlocking { viewModel.openDate(day) }
    }

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun intercourseToggle_hiddenUntilOptIn() {
        androidComposeRule.setContent {
            val state by viewModel.uiState.collectAsState()
            MahinTheme {
                DayEditorSheetContent(
                    date = day,
                    state = state,
                )
            }
        }
        androidComposeRule.onNodeWithTag("day_editor_intercourse_toggle").assertDoesNotExist()
    }

    @Test
    fun lifestyleSleep_hiddenWhenModuleDisabled() {
        androidComposeRule.setContent {
            val state by viewModel.uiState.collectAsState()
            MahinTheme {
                DayEditorSheetContent(
                    date = day,
                    state = state,
                )
            }
        }
        androidComposeRule.onNodeWithTag("day_editor_lifestyle_sleep_toggle").assertDoesNotExist()
    }

    @Test
    fun sexualOptIn_showsIntercourseToggle_andDisableKeepsDbEntry() {
        runBlocking {
            harness.ttcRepository.upsertTtcDayLog(
                TtcDayLogInput(
                    logDate = day,
                    bbtCelsius = null,
                    cervicalMucus = null,
                    ovulationTestResult = null,
                    pregnancyTestResult = null,
                    intercourseLogged = true,
                    intercourseProtected = true,
                ),
            )
            viewModel.confirmSexualOptIn()
            awaitUntil { viewModel.uiState.value.sexualActivityEnabled }
            viewModel.disableSexualActivity()
            awaitUntil { !viewModel.uiState.value.sexualActivityEnabled }
            val ttc = harness.ttcRepository.getTtcLogForDate(day)
            assertThat(ttc?.intercourseLogged).isTrue()
        }
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
