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
import dev.mahin.core.designsystem.component.MahinCalendarLegend
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.component.MahinLoadingState
import dev.mahin.core.designsystem.component.MahinScreenHeader
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
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyStateRtlLight() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme(darkTheme = false) {
                    MahinEmptyState(modifier = Modifier.fillMaxSize())
                }
            }
        }
        composeRule.onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
    }

    @Test
    fun jalaliDatePickerRtlLight() {
        composeRule.setContent {
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
        composeRule.onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
    }

    @Test
    fun screenHeaderAndCalendarLegendRtlLight() {
        composeRule.setContent {
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
        composeRule.onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
    }

    @Test
    fun numericDisplayPersianDigitsRtlLight() {
        composeRule.setContent {
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
        composeRule.onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
    }

    @Test
    fun numericDisplayPersianDigitsRtlDark() {
        composeRule.setContent {
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
        composeRule.onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
    }

    @Test
    fun loadingStateRtlLight() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme(darkTheme = false) {
                    MahinLoadingState(modifier = Modifier.fillMaxSize())
                }
            }
        }
        composeRule.onRoot().captureRoboImage(roborazziOptions = MahinRoborazzi.options)
    }
}
