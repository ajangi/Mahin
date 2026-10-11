package dev.mahin.android.cycle

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.testing.ViewModelStoreClearingRule
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR", manifest = Config.NONE)
class CalendarPeriodManagementUiTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Test
    fun openEndedPeriod_rowRendersWithoutCrash() {
        val openPeriod =
            PeriodRecordEntity(
                id = "open-1",
                startDate = LocalDate.of(2025, 2, 1),
                endDate = null,
                note = null,
                createdAtEpochMs = 0L,
                updatedAtEpochMs = 0L,
            )
        androidComposeRule.setContent {
            MahinTheme {
                CalendarPeriodManagementSection(
                    periods = listOf(openPeriod),
                    editingRecordId = null,
                    editStartJalali = null,
                    editEndJalali = null,
                    onBeginEdit = {},
                    onEditStartSelected = {},
                    onEditEndSelected = {},
                    onClearEndDate = {},
                    onSaveEdit = {},
                    onCancelEdit = {},
                    onMergeWithNext = {},
                    onSplit = {},
                )
            }
        }
        androidComposeRule.onNodeWithTag("calendar_period_row_open-1").assertIsDisplayed()
        androidComposeRule.onNodeWithTag("calendar_period_merge_open-1").assertDoesNotExist()
    }

    @Test
    fun mergeCallback_invokedFromUi() {
        var mergedId: String? = null
        val first =
            PeriodRecordEntity(
                id = "p1",
                startDate = LocalDate.of(2025, 1, 1),
                endDate = LocalDate.of(2025, 1, 3),
                note = null,
                createdAtEpochMs = 0L,
                updatedAtEpochMs = 0L,
            )
        val second =
            PeriodRecordEntity(
                id = "p2",
                startDate = LocalDate.of(2025, 1, 4),
                endDate = LocalDate.of(2025, 1, 6),
                note = null,
                createdAtEpochMs = 0L,
                updatedAtEpochMs = 0L,
            )
        androidComposeRule.setContent {
            MahinTheme {
                CalendarPeriodManagementSection(
                    periods = listOf(first, second),
                    editingRecordId = null,
                    editStartJalali = null,
                    editEndJalali = null,
                    onBeginEdit = {},
                    onEditStartSelected = {},
                    onEditEndSelected = {},
                    onClearEndDate = {},
                    onSaveEdit = {},
                    onCancelEdit = {},
                    onMergeWithNext = { mergedId = it },
                    onSplit = {},
                )
            }
        }
        androidComposeRule.onNodeWithTag("calendar_period_merge_p1").performClick()
        assertThat(mergedId).isEqualTo("p1")
    }
}
