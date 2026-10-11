package dev.mahin.android.cycle

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.google.common.truth.Truth.assertThat
import dev.mahin.android.cycle.editor.DayEditorCoordinator
import dev.mahin.android.cycle.editor.DayEditorHost
import dev.mahin.android.cycle.editor.DayEditorTestHarness
import dev.mahin.android.cycle.editor.DayEditorViewModel
import dev.mahin.android.golden.M15GoldenFixtures
import dev.mahin.android.shell.MahinAppShellWithNavigationOverride
import dev.mahin.android.shell.MahinShellScreenOverrides
import dev.mahin.android.shell.ShellNavigationState
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
class TodayDayEditorIntegrationTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    private lateinit var harness: DayEditorTestHarness
    private lateinit var coordinator: DayEditorCoordinator
    private lateinit var viewModel: DayEditorViewModel
    private val today = LocalDate.of(2025, 6, 15)

    @Before
    fun setUp() {
        harness = DayEditorTestHarness.create(today)
        runBlocking { harness.seedCycleProfile() }
        coordinator = DayEditorCoordinator()
        viewModel = harness.createViewModel()
    }

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun quickLog_opensEditor_autosavesWithoutSaveOrDialog() {
        androidComposeRule.setContent {
            MahinTheme {
                MahinAppShellWithNavigationOverride(
                    shellNavigationStateOverride =
                        ShellNavigationState(
                            profileLoaded = true,
                            reproductiveMode = ReproductiveMode.CYCLE_TRACKING,
                        ),
                    screenOverrides =
                        MahinShellScreenOverrides(
                            today = { _, _, _, onOpenDayEditor, _, _, _ ->
                                TodayScreenContent(
                                    state = M15GoldenFixtures.todayFertileWindow().copy(today = today),
                                    actions = TodayScreenActions(onOpenLogForDate = onOpenDayEditor),
                                )
                            },
                            calendar = { Box(Modifier.fillMaxSize()) },
                            log = { Box(Modifier.fillMaxSize()) },
                            cycleInsights = { Box(Modifier.fillMaxSize()) },
                            ttcInsights = { Box(Modifier.fillMaxSize()) },
                            pregnancyHub = { _, _ -> Box(Modifier.fillMaxSize()) },
                            plan = { Box(Modifier.fillMaxSize()) },
                            learn = { Box(Modifier.fillMaxSize()) },
                            history = { Box(Modifier.fillMaxSize()) },
                            settings = { _, _, _ -> Box(Modifier.fillMaxSize()) },
                        ),
                    dayEditorCoordinatorOverride = coordinator,
                    dayEditorOverlay = { session, onDismiss ->
                        DayEditorHost(
                            session = session,
                            onDismiss = onDismiss,
                            viewModel = viewModel,
                            useModalSheet = false,
                        )
                    },
                )
            }
        }
        androidComposeRule.onNodeWithTag("today_screen_list").performScrollToNode(hasTestTag("today_quick_log_row"))
        androidComposeRule.onAllNodesWithTag("today_quick_log_chip")[0].performClick()
        androidComposeRule.waitForIdle()
        androidComposeRule.waitUntil(timeoutMillis = 8_000) {
            coordinator.session.value?.date == today
        }
        androidComposeRule.onNodeWithTag("day_editor_sheet").assertExists()
        androidComposeRule.onAllNodes(isDialog()).assertCountEquals(0)
        androidComposeRule.waitUntil(timeoutMillis = 8_000) {
            ShadowLooper.idleMainLooper()
            val state = viewModel.uiState.value
            state.date == today && !state.loading
        }
        androidComposeRule.onNodeWithTag("day_editor_period_started").performClick()
        androidComposeRule
            .onNodeWithTag("day_editor_scroll")
            .performScrollToNode(hasTestTag("day_editor_symptom_خستگی"))
        androidComposeRule.onNodeWithTag("day_editor_symptom_خستگی").performClick()
        androidComposeRule
            .onNodeWithTag("day_editor_scroll")
            .performScrollToNode(hasTestTag("day_editor_mood_آرام"))
        androidComposeRule.onNodeWithTag("day_editor_mood_آرام").performClick()
        androidComposeRule.waitUntil(timeoutMillis = 12_000) {
            ShadowLooper.idleMainLooper()
            val log = harness.dailyLogFor(today)
            log != null && log.symptomTags.contains("خستگی") && log.moodTags.contains("آرام")
        }
        assertThat(harness.periodDayFor(today)).isNotNull()
    }
}
