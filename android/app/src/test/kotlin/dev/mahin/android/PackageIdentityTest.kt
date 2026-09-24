package dev.mahin.android

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PackageIdentityTest {
    @Test
    fun workingApplicationNamespaceIsNotAStoreLockedDomain() {
        assertThat("dev.mahin.android").doesNotMatch(".*\\.ir$")
        assertThat("dev.mahin.android").startsWith("dev.")
    }
}
