package dev.mahin.android.cycle.editor

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.LogEditorCategory
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
class DayEditorCustomSymptomUiTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var harness: DayEditorTestHarness
    private lateinit var viewModel: DayEditorViewModel
    private val day = LocalDate.of(2025, 5, 5)
    private val label = "سردرد شخصی"

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Before
    fun setUp() {
        harness = DayEditorTestHarness.create(day)
        runBlocking { harness.seedCycleProfile() }
        viewModel = harness.createViewModel()
        runBlocking {
            viewModel.openDate(day)
            repeat(20) { ShadowLooper.idleMainLooper() }
        }
    }

    @After
    fun tearDown() {
        repeat(5) { ShadowLooper.idleMainLooper() }
        harness.close()
    }

    @Test
    fun addCustomSymptom_viaFieldAndButton_persistsLocalTagOnly() {
        androidComposeRule.setContent {
            val state by viewModel.uiState.collectAsState()
            MahinTheme {
                DayEditorSheetContent(
                    date = day,
                    state = state.copy(orderedCategories = listOf(LogEditorCategory.SYMPTOMS)),
                    onCustomSymptomDraft = viewModel::setCustomSymptomDraft,
                    onAddCustomSymptom = viewModel::addCustomSymptomFromDraft,
                )
            }
        }
        androidComposeRule.onNodeWithTag("day_editor_custom_symptom_field").performClick()
        androidComposeRule.onNodeWithTag("day_editor_custom_symptom_field").performTextInput(label)
        androidComposeRule.waitUntil(timeoutMillis = 5_000) {
            ShadowLooper.idleMainLooper()
            viewModel.uiState.value.customSymptomDraft == label
        }
        androidComposeRule
            .onNodeWithTag("day_editor_custom_symptom_add")
            .performScrollTo()
            .performClick()
        androidComposeRule.waitUntil(timeoutMillis = 15_000) {
            ShadowLooper.idleMainLooper()
            val log = harness.dailyLogFor(day)
            log != null && log.localCustomSymptomTags.contains(label)
        }
        assertThat(harness.dailyLogFor(day)?.symptomTags).doesNotContain(label)
        androidComposeRule.waitUntil(timeoutMillis = 5_000) {
            ShadowLooper.idleMainLooper()
            viewModel.uiState.value.customSymptoms
                .contains(label)
        }
        androidComposeRule.onNodeWithTag("day_editor_custom_symptom_chip_$label").assertExists()
        val log = harness.dailyLogFor(day)
        assertThat(log?.localCustomSymptomTags).contains(label)
        assertThat(log?.symptomTags).doesNotContain(label)
    }
}
