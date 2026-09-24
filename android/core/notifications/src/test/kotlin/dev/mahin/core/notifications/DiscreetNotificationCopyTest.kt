package dev.mahin.core.notifications

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.datastore.NotificationPrivacyMode
import org.junit.Test

class DiscreetNotificationCopyTest {
    @Test
    fun defaultCopyIsDiscreetPersian() {
        val copy = DiscreetNotificationCopy.resolve(NotificationPrivacyMode.DISCREET, "پریود شما نزدیک است")
        assertThat(copy).isEqualTo("یادآوری شما آماده است")
        assertThat(DiscreetNotificationCopy.containsSensitiveLeak(copy)).isFalse()
    }

    @Test
    fun detectorFlagsPeriodLanguage() {
        assertThat(DiscreetNotificationCopy.containsSensitiveLeak("پریود شما نزدیک است")).isTrue()
    }
}
