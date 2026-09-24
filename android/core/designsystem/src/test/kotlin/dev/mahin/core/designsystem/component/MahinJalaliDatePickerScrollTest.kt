package dev.mahin.core.designsystem.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.LayoutDirection
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.designsystem.MahinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "fa-rIR")
class MahinJalaliDatePickerScrollTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun datePickerInsideVerticalScroll_composesWithoutCrash() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        MahinJalaliDatePicker(
                            selectedDate = JalaliDate(year = 1403, month = 1, day = 1),
                            onDateSelected = {},
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }
}
