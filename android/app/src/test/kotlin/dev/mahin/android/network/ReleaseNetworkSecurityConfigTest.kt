package dev.mahin.android.network

import android.content.res.XmlResourceParser
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.android.R
import dev.mahin.core.network.BuildConfig
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ReleaseNetworkSecurityConfigTest {
    @Test
    fun debugMergedNetworkSecurityConfig_allowsCleartextToEmulatorHost() {
        assumeTrue(BuildConfig.DEBUG)
        val parsed = loadParsedNetworkSecurityConfig()
        assertThat(parsed.cleartextDomains).contains("10.0.2.2")
    }

    @Test
    fun releaseMergedNetworkSecurityConfig_disallowsCleartext() {
        assumeFalse(BuildConfig.DEBUG)
        val parsed = loadParsedNetworkSecurityConfig()
        assertThat(parsed.cleartextDomains).isEmpty()
        assertThat(parsed.baseConfigCleartextPermitted).isFalse()
    }

    private fun loadParsedNetworkSecurityConfig(): ParsedNetworkSecurityConfig {
        val parser =
            ApplicationProvider
                .getApplicationContext<android.content.Context>()
                .resources
                .getXml(R.xml.network_security_config)
        return parseNetworkSecurityConfig(parser)
    }
}

internal data class ParsedNetworkSecurityConfig(
    val baseConfigCleartextPermitted: Boolean,
    val cleartextDomains: List<String>,
)

/**
 * Mirrors Android Network Security Config parsing: [cleartextTrafficPermitted] has no
 * namespace prefix in XML; use a null namespace when reading attributes.
 */
internal fun parseNetworkSecurityConfig(parser: XmlResourceParser): ParsedNetworkSecurityConfig {
    val cleartextDomains = mutableListOf<String>()
    var baseConfigCleartextPermitted = false
    var cleartextDomainConfig = false
    while (parser.eventType != XmlResourceParser.END_DOCUMENT) {
        val isStart = parser.eventType == XmlResourceParser.START_TAG
        val isEnd = parser.eventType == XmlResourceParser.END_TAG
        val tag = if (isStart || isEnd) parser.name else null
        if (isStart && tag == "base-config") {
            baseConfigCleartextPermitted =
                parser.getAttributeBooleanValue(null, "cleartextTrafficPermitted", false)
        }
        if (isStart && tag == "domain-config") {
            cleartextDomainConfig =
                parser.getAttributeBooleanValue(null, "cleartextTrafficPermitted", false)
        }
        if (isStart && tag == "domain" && cleartextDomainConfig) {
            cleartextDomains.add(parser.nextText().trim())
            continue
        }
        if (isEnd && tag == "domain-config") {
            cleartextDomainConfig = false
        }
        parser.next()
    }
    return ParsedNetworkSecurityConfig(
        baseConfigCleartextPermitted = baseConfigCleartextPermitted,
        cleartextDomains = cleartextDomains,
    )
}
