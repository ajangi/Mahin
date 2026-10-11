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
        }
        viewModel = harness.createViewModel()
        runBlocking { viewModel.openDate(day) }
    }

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun sexualOptIn_toggleVisibleOnlyWhenEnabled_dbRetainedOnDisable() {
        androidComposeRule.setContent {
            val state by viewModel.uiState.collectAsState()
            MahinTheme {
                DayEditorSheetContent(
                    date = day,
                    state = state,
                    onToggleIntercourse = viewModel::toggleIntercourse,
                )
            }
        }
        androidComposeRule.onNodeWithTag("day_editor_intercourse_toggle").assertDoesNotExist()
        viewModel.confirmSexualOptIn()
        androidComposeRule.waitUntil(timeoutMillis = 8_000) {
            ShadowLooper.idleMainLooper()
            viewModel.uiState.value.sexualActivityEnabled
        }
        androidComposeRule.onNodeWithTag("day_editor_intercourse_toggle").assertExists()
        viewModel.disableSexualActivity()
        androidComposeRule.waitUntil(timeoutMillis = 8_000) {
            ShadowLooper.idleMainLooper()
            !viewModel.uiState.value.sexualActivityEnabled
        }
        androidComposeRule.onNodeWithTag("day_editor_intercourse_toggle").assertDoesNotExist()
        val ttc = runBlocking { harness.ttcRepository.getTtcLogForDate(day) }
        assertThat(ttc?.intercourseLogged).isTrue()
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
}
