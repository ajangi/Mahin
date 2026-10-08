package dev.mahin.android.cycle

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.designsystem.MahinTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR", manifest = Config.NONE)
class CalendarDaySheetContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun fertileDay_showsNonContraceptionCopy() {
        composeRule.setContent {
            MahinTheme {
                CalendarDaySheetContent(
                    jalali = JalaliDate(1403, 12, 1),
                    gregorian = LocalDate.of(2025, 2, 20),
                    markers = DayMarkers(fertileWindow = true),
                    logLines = emptyList(),
                    onEditLog = {},
                )
            }
        }
        composeRule.onNodeWithTag("fertile_not_contraception_copy").assertExists()
    }
}
