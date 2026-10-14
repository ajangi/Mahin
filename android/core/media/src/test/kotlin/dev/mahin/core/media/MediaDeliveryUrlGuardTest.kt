package dev.mahin.core.media

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MediaDeliveryUrlGuardTest {
    @Test
    fun allowsCyclePathWithoutQueryOrUserIdentifier() {
        val url = "http://localhost:9000/mahin-media/cycle/illustrations/v1"
        assertThat(MediaDeliveryUrlGuard.isSafePublicMediaUrl(url)).isTrue()
    }

    @Test
    fun rejectsQueryStringsWithoutThrowing() {
        val url = "http://localhost:9000/mahin-media/placeholders/mark?v=1"
        assertThat(MediaDeliveryUrlGuard.isSafePublicMediaUrl(url)).isFalse()
    }

    @Test
    fun rejectsUserIdPathSegmentsWithoutThrowing() {
        val url = "http://localhost:9000/mahin-media/users/userId/avatar"
        assertThat(MediaDeliveryUrlGuard.isSafePublicMediaUrl(url)).isFalse()
    }
}
