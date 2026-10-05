package dev.mahin.android.cycle

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.core.designsystem.MahinTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Regression: [HistoryScreenContent] uses a single bounded [androidx.compose.foundation.lazy.LazyColumn].
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], manifest = Config.NONE, qualifiers = "fa-rIR")
class HistoryScreenScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun historyScreen_withPeriodList_composesWithoutCrash() {
        val samplePeriods =
            listOf(
                PeriodRecordEntity(
                    id = "p1",
                    startDate = LocalDate.of(2025, 1, 1),
                    endDate = LocalDate.of(2025, 1, 5),
                    note = null,
                    createdAtEpochMs = 0L,
                    updatedAtEpochMs = 0L,
                ),
                PeriodRecordEntity(
                    id = "p2",
                    startDate = LocalDate.of(2025, 2, 1),
                    endDate = LocalDate.of(2025, 2, 4),
                    note = null,
                    createdAtEpochMs = 0L,
                    updatedAtEpochMs = 0L,
                ),
            )
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme {
                    HistoryScreenContent(periods = samplePeriods)
                }
            }
        }
        composeRule.waitForIdle()
    }
}
