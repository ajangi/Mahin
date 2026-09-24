package dev.mahin.domain.account

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AccountDomainModuleTest {
    @Test
    fun moduleExists() {
        assertThat(AccountDomainModule::class.simpleName).isEqualTo("AccountDomainModule")
    }
}
