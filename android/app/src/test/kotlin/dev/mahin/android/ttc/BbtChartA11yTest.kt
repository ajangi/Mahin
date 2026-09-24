package dev.mahin.android.ttc

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BbtChartA11yTest {
    @Test
    fun summary_usesPersianDigitsAndLatestValue() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val points =
            listOf(
                BbtChartPoint(LocalDate.of(2025, 3, 1), 36.4),
                BbtChartPoint(LocalDate.of(2025, 3, 5), 36.61),
            )
        val summary = BbtChartA11y.summary(context.resources, points)
        assertThat(summary).contains("۲")
        assertThat(summary).contains("۳۶٫۶۱")
        assertThat(summary).doesNotContain("celsius")
        assertThat(summary).doesNotContain("count=")
    }
}
