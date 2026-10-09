package dev.mahin.core.designsystem

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.designsystem.component.MahinCalendarDayDecoration
import dev.mahin.core.designsystem.component.MahinCalendarLegend
import dev.mahin.core.designsystem.component.MahinCalendarMarkerTintAlphas
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.component.MahinLoadingState
import dev.mahin.core.designsystem.component.MahinScreenHeader
import dev.mahin.core.designsystem.hexToColor
import dev.mahin.core.model.MahinTokenHex
import dev.mahin.core.testing.ViewModelStoreClearingRule
import dev.mahin.core.testing.roborazzi.MahinRoborazzi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "fa-rIR-w411dp-h891dp-xxhdpi")
@OptIn(ExperimentalRoborazziApi::class)
class MahinDesignSystemScreenshotTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Test
    fun emptyStateRtlLight() {
        androidComposeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme(darkTheme = false) {
                    MahinEmptyState(modifier = Modifier.fillMaxSize())
                }
            }
        }
        androidComposeRule.onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
    }

    @Test
    fun jalaliCalendarJalaliDay15OvulationRtlLight() {
        captureJalaliOvulationOnDay15(darkTheme = false)
    }

    @Test
    fun jalaliCalendarJalaliDay15OvulationRtlDark() {
        captureJalaliOvulationOnDay15(darkTheme = true)
    }

    private fun captureJalaliOvulationOnDay15(darkTheme: Boolean) {
        val ovulationFill =
            if (darkTheme) {
                hexToColor(MahinTokenHex.DARK_HEALTH_OVULATION).copy(
                    alpha = MahinCalendarMarkerTintAlphas.DARK_OVULATION,
                )
            } else {
                hexToColor(MahinTokenHex.LIGHT_HEALTH_OVULATION).copy(
                    alpha = MahinCalendarMarkerTintAlphas.LIGHT_OVULATION,
                )
            }
        androidComposeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme(darkTheme = darkTheme) {
                    MahinJalaliDatePicker(
                        selectedDate = JalaliDate(year = 1403, month = 12, day = 15),
                        onDateSelected = {},
                        modifier = Modifier.fillMaxSize(),
                        dayDecoration = { gregorian ->
                            val jalali = PersianCivilDateConverter.toJalali(gregorian)
                            if (jalali.day == 15) {
                                MahinCalendarDayDecoration(
                                    fillColor = ovulationFill,
                                    estimatedOvulation = true,
                                )
                            } else {
                                null
                            }
                        },
                    )
                }
            }
        }
        androidComposeRule.onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
    }

    @Test
    fun jalaliDatePickerRtlLight() {
        androidComposeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme(darkTheme = false) {
                    MahinJalaliDatePicker(
                        selectedDate = JalaliDate(year = 1403, month = 1, day = 1),
                        onDateSelected = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        androidComposeRule.onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
    }

    @Test
    fun screenHeaderAndCalendarLegendRtlLight() {
        androidComposeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme(darkTheme = false) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        MahinScreenHeader(
                            title = "تقویم چرخه",
                            subtitle = "راهنمای رنگ‌ها در پایین",
                        )
                        MahinCalendarLegend()
                    }
                }
            }
        }
        androidComposeRule.onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
    }

    @Test
    fun numericDisplayPersianDigitsRtlLight() {
        androidComposeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme(darkTheme = false) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "۲۸",
                            style = mahinTextStyle(MahinTypographyRole.NumericDisplay),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "هفته ۲۱",
                            style = mahinTextStyle(MahinTypographyRole.NumericDisplay),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
        androidComposeRule.onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
    }

    @Test
    fun numericDisplayPersianDigitsRtlDark() {
        androidComposeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme(darkTheme = true) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "۲۸",
                            style = mahinTextStyle(MahinTypographyRole.NumericDisplay),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "هفته ۲۱",
                            style = mahinTextStyle(MahinTypographyRole.NumericDisplay),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
        androidComposeRule.onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
    }

    @Test
    fun loadingStateRtlLight() {
        androidComposeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme(darkTheme = false) {
                    MahinLoadingState(modifier = Modifier.fillMaxSize())
                }
            }
        }
        androidComposeRule.onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
    }
}
