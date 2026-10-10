@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package dev.mahin.android.cycle

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import com.google.common.truth.Truth.assertThat
import dev.mahin.android.golden.M15GoldenFixtures
import dev.mahin.core.designsystem.MahinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR", manifest = Config.NONE)
class CalendarDaySheetGridA11yTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun daySheetOpen_calendarDayCellsNotInAccessibilityTree() {
        val state = M15GoldenFixtures.calendarDaySheetOpen()
        composeRule.setContent {
            MahinTheme {
                SharedTransitionLayout {
                    CycleCalendarScreenContent(
                        state = state,
                        sharedTransitionScope = this,
                        onDateSelected = {},
                        onDismissDaySheet = {},
                    )
                }
            }
        }
        composeRule.onAllNodesWithTag("jalali_day_cell").assertCountEquals(0)
    }

    @Test
    fun daySheetClosed_calendarDayCellsAreInAccessibilityTree() {
        val state = M15GoldenFixtures.calendarDaySheetOpen().copy(daySheetOpen = false)
        composeRule.setContent {
            MahinTheme {
                SharedTransitionLayout {
                    CycleCalendarScreenContent(
                        state = state,
                        sharedTransitionScope = this,
                        onDateSelected = {},
                        onDismissDaySheet = {},
                    )
                }
            }
        }
        assertThat(composeRule.onAllNodesWithTag("jalali_day_cell").fetchSemanticsNodes().size).isGreaterThan(0)
    }

    @Test
    fun scrimDismissAction_invokesOnDismiss() {
        var dismissed = false
        val state = M15GoldenFixtures.calendarDaySheetOpen()
        composeRule.setContent {
            MahinTheme {
                SharedTransitionLayout {
                    CycleCalendarScreenContent(
                        state = state,
                        sharedTransitionScope = this,
                        onDateSelected = {},
                        onDismissDaySheet = { dismissed = true },
                    )
                }
            }
        }
        val scrimDescription =
            composeRule
                .onNodeWithTag("calendar_day_sheet_scrim")
                .fetchSemanticsNode()
                .config[SemanticsProperties.ContentDescription]
                .joinToString()
        assertThat(scrimDescription).contains("بستن")
        composeRule.onNodeWithTag("calendar_day_sheet_scrim").performSemanticsAction(SemanticsActions.Dismiss)
        composeRule.waitForIdle()
        assertThat(dismissed).isTrue()
    }
}
