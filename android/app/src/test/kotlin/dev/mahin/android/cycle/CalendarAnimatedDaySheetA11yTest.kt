@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package dev.mahin.android.cycle

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
class CalendarAnimatedDaySheetA11yTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun daySheetOverlay_scrimHasDismissSemantics() {
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
        val description =
            composeRule
                .onNodeWithTag("calendar_day_sheet_scrim")
                .fetchSemanticsNode()
                .config[SemanticsProperties.ContentDescription]
                .joinToString()
        assertThat(description).contains("بستن")
    }

    @Test
    fun daySheetOverlay_hasPaneTitle() {
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
        val paneTitle =
            composeRule
                .onNodeWithTag("calendar_day_sheet_overlay")
                .fetchSemanticsNode()
                .config
                .getOrNull(SemanticsProperties.PaneTitle)
        assertThat(paneTitle).isEqualTo("جزئیات روز تقویم")
    }
}
