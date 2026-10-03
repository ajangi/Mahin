package dev.mahin.core.network

import com.google.common.truth.Truth.assertThat
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Guards release artifacts against shipping the Android emulator loopback API host.
 * Run release checks with `:core:network:testReleaseUnitTest` and `verifyReleaseApkNoEmulatorApiHost`.
 */
class MahinApiBaseUrlBuildConfigTest {
    @Test
    fun debugBuild_usesEmulatorApiHost() {
        assumeTrue(BuildConfig.DEBUG)
        assertThat(BuildConfig.MAHIN_API_BASE_URL).contains("10.0.2.2")
    }

    @Test
    fun releaseBuild_doesNotUseEmulatorApiHost() {
        assumeFalse(BuildConfig.DEBUG)
        assertThat(BuildConfig.MAHIN_API_BASE_URL).doesNotContain("10.0.2.2")
        assertThat(BuildConfig.MAHIN_API_BASE_URL).startsWith("https://")
    }
}
