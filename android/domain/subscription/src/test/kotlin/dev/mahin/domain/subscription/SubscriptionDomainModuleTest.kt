package dev.mahin.domain.subscription

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SubscriptionDomainModuleTest {
    @Test
    fun moduleExists() {
        assertThat(SubscriptionDomainModule.java.simpleName).isEqualTo("SubscriptionDomainModule")
    }
}
