package dev.mahin.core.notifications

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.PregnancyOutcome
import org.junit.Test

class PregnancyNotificationSuppressionTest {
    @Test
    fun suppressesAfterLossOrTermination() {
        assertThat(
            PregnancyNotificationSuppression.shouldSuppressCelebratoryPregnancyNotifications(
                suppressFlag = false,
                latestOutcome = PregnancyOutcome.PREGNANCY_LOSS,
            ),
        ).isTrue()
        assertThat(
            PregnancyNotificationSuppression.resolvePregnancyWeeklyPreview(
                suppressFlag = false,
                latestOutcome = PregnancyOutcome.TERMINATION,
                descriptiveFa = "هفته ۲۰",
            ),
        ).isNull()
    }

    @Test
    fun allowsNeutralPreviewForLiveBirth() {
        assertThat(
            PregnancyNotificationSuppression.celebratoryPreviewAllowed(
                suppressFlag = false,
                latestOutcome = PregnancyOutcome.LIVE_BIRTH,
            ),
        ).isTrue()
    }
}
