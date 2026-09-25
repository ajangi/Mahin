package dev.mahin.core.notifications

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.datastore.NotificationPrivacyMode
import dev.mahin.core.model.PregnancyOutcome
import org.junit.Test

class MahinNotificationPreviewPipelineTest {
    @Test
    fun suppressesCelebratoryPreviewAfterLoss() {
        val body =
            MahinNotificationPreviewPipeline.resolveScheduledNotificationBody(
                privacyMode = NotificationPrivacyMode.DESCRIPTIVE,
                suppressCelebratoryFromStore = true,
                latestOutcome = PregnancyOutcome.PREGNANCY_LOSS,
                descriptiveFa = "هفته ۲۰ بارداری",
            )
        assertThat(body).isEqualTo(DiscreetNotificationCopy.DEFAULT_FA)
        assertThat(
            MahinNotificationPreviewPipeline.inAppCelebratoryWeekPreviewAllowed(
                suppressCelebratoryFromStore = true,
                latestOutcome = PregnancyOutcome.PREGNANCY_LOSS,
            ),
        ).isFalse()
    }
}
