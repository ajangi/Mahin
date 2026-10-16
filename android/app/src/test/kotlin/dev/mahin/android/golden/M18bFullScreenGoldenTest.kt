package dev.mahin.android.golden

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.mahin.android.pregnancy.PregnancyExpensesScreenContent
import dev.mahin.android.pregnancy.PregnancyWarningSignsScreenContent
import dev.mahin.android.pregnancy.PregnancyWeightChartScreenContent
import dev.mahin.android.pregnancy.PregnancyWeightChartUiState
import dev.mahin.core.testing.ViewModelStoreClearingRule
import dev.mahin.core.testing.roborazzi.captureMahinFullScreenGolden
import dev.mahin.domain.pregnancy.PregnancyWeightChartPoint
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "fa-rIR-w411dp-h891dp-xxhdpi")
class M18bFullScreenGoldenTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Test fun pregnancy_expenses_light_scale10() = captureExpenses(false, 1f)

    @Test fun pregnancy_expenses_dark_scale10() = captureExpenses(true, 1f)

    @Test fun pregnancy_expenses_light_scale13() = captureExpenses(false, 1.3f)

    @Test fun pregnancy_expenses_dark_scale13() = captureExpenses(true, 1.3f)

    @Test fun pregnancy_warning_signs_light_scale10() = captureWarningSigns(false, 1f)

    @Test fun pregnancy_warning_signs_dark_scale10() = captureWarningSigns(true, 1f)

    @Test fun pregnancy_warning_signs_light_scale13() = captureWarningSigns(false, 1.3f)

    @Test fun pregnancy_warning_signs_dark_scale13() = captureWarningSigns(true, 1.3f)

    @Test fun pregnancy_weight_chart_light_scale10() = captureWeightChart(false, 1f)

    @Test fun pregnancy_weight_chart_dark_scale10() = captureWeightChart(true, 1f)

    @Test fun pregnancy_weight_chart_light_scale13() = captureWeightChart(false, 1.3f)

    @Test fun pregnancy_weight_chart_dark_scale13() = captureWeightChart(true, 1.3f)

    private fun captureExpenses(
        dark: Boolean,
        scale: Float,
    ) {
        androidComposeRule.captureMahinFullScreenGolden(dark, scale) {
            PregnancyExpensesScreenContent(
                state = M18bGoldenFixtures.expenses(),
                onDraftCategoryChange = {},
                onDraftAmountChange = {},
                onDraftCurrencyChange = {},
                onDraftNoteChange = {},
                onSaveExpense = {},
                onDeleteExpense = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun captureWarningSigns(
        dark: Boolean,
        scale: Float,
    ) {
        androidComposeRule.captureMahinFullScreenGolden(dark, scale) {
            PregnancyWarningSignsScreenContent(
                state = M18bGoldenFixtures.warningSigns(),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun captureWeightChart(
        dark: Boolean,
        scale: Float,
    ) {
        androidComposeRule.captureMahinFullScreenGolden(dark, scale) {
            PregnancyWeightChartScreenContent(
                state =
                    PregnancyWeightChartUiState(
                        isLoading = false,
                        hasActivePregnancy = true,
                        points =
                            listOf(
                                PregnancyWeightChartPoint(12, 68.0),
                                PregnancyWeightChartPoint(16, 70.5),
                                PregnancyWeightChartPoint(20, 72.0),
                            ),
                    ),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
