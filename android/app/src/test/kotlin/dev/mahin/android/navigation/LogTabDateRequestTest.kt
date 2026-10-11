package dev.mahin.android.navigation

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class LogTabDateRequestTest {
    @Test
    fun request_thenConsume_returnsDate() {
        val request = LogTabDateRequest()
        val date = LocalDate.of(2025, 3, 14)
        request.request(date)
        assertThat(request.pendingDate.value).isEqualTo(date)
        assertThat(request.consume()).isEqualTo(date)
        assertThat(request.pendingDate.value).isNull()
    }
}
