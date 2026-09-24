package dev.mahin.core.datastore

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PrivacyPreferenceKeysTest {
    @Test
    fun defaultNotificationModeIsDiscreet() {
        assertThat(NotificationPrivacyMode.DISCREET.name).isEqualTo("DISCREET")
        assertThat(PrivacyPreferenceKeys.notificationPrivacyMode.name)
            .isEqualTo("notification_privacy_mode")
    }
}
