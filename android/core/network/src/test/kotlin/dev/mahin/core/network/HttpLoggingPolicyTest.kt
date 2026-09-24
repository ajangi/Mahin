package dev.mahin.core.network

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HttpLoggingPolicyTest {
    @Test(expected = IllegalArgumentException::class)
    fun rejectsOkHttpBodyLogger() {
        HttpLoggingPolicy.rejectBodyLoggingInterceptor("okhttp3.logging.HttpLoggingInterceptor")
    }

    @Test
    fun allowsSanitizedLogger() {
        HttpLoggingPolicy.rejectBodyLoggingInterceptor(
            SanitizedHttpLoggingInterceptor::class.java.name,
        )
        assertThat(SanitizedHttpLoggingInterceptor::class.java.simpleName)
            .isEqualTo("SanitizedHttpLoggingInterceptor")
    }
}
