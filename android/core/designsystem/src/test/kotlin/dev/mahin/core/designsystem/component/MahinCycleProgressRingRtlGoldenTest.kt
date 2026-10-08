package dev.mahin.core.designsystem.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.mahin.core.testing.roborazzi.captureMahinFullScreenGolden
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "fa-rIR-w411dp-h891dp-xxhdpi")
class MahinCycleProgressRingRtlGoldenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun cycleRing_rtl_segmentArcs_light_scale10() {
        composeRule.captureMahinFullScreenGolden(darkTheme = false, fontScale = 1f) {
            MahinCycleProgressRing(
                arcs =
                    listOf(
                        MahinRingArc(Color(0xFFC94F62), 0f, 0.18f),
                        MahinRingArc(
                            Color(0xFF3B8F91),
                            0.35f,
                            0.55f,
                            style = MahinRingArcStyle.Dashed,
                        ),
                    ),
                progressFraction = 0.5f,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                progressColor = MaterialTheme.colorScheme.primary,
                todayMarkerFraction = 0.5f,
                todayMarkerColor = MaterialTheme.colorScheme.primary,
                contentDescription = "روز ۱۴ چرخه، ۵ روز تا پریود بعدی (تخمینی)",
                modifier = Modifier.fillMaxSize(),
            ) {}
        }
    }
}
