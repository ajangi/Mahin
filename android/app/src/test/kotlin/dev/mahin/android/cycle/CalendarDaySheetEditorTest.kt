package dev.mahin.android.cycle

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.designsystem.MahinTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR-w411dp-h891dp-xxhdpi", manifest = Config.NONE)
class CalendarDaySheetEditorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun editLogButton_invokesOpenDayEditor() {
        var opened = false
        composeRule.setContent {
            MahinTheme {
                CalendarDaySheetContent(
                    jalali = JalaliDate(1404, 1, 20),
                    gregorian = LocalDate.of(2025, 4, 10),
                    markers = null,
                    logLines = emptyList(),
                    onEditLog = { opened = true },
                    modifier = Modifier.testTag("calendar_day_sheet"),
                )
            }
        }
        composeRule.onNodeWithTag("calendar_day_sheet").assertExists()
        composeRule.onNodeWithText("ویرایش ثبت").performClick()
        composeRule.waitForIdle()
        assertThat(opened).isTrue()
    }
}
