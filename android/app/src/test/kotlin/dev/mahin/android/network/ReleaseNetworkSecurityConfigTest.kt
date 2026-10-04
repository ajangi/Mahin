package dev.mahin.android.network

import android.content.res.XmlResourceParser
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.android.R
import dev.mahin.core.network.BuildConfig
import org.junit.Assume.assumeFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ANDROID_RES_NS = "http://schemas.android.com/apk/res/android"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ReleaseNetworkSecurityConfigTest {
    @Test
    fun releaseMergedNetworkSecurityConfig_hasNoCleartextDevDomains() {
        assumeFalse(BuildConfig.DEBUG)
        val parser =
            ApplicationProvider
                .getApplicationContext<android.content.Context>()
                .resources
                .getXml(R.xml.network_security_config)
        assertThat(parseCleartextDomains(parser)).isEmpty()
    }
}

internal fun parseCleartextDomains(parser: XmlResourceParser): List<String> {
    val domains = mutableListOf<String>()
    var cleartextDomainConfig = false
    while (parser.eventType != XmlResourceParser.END_DOCUMENT) {
        if (parser.eventType == XmlResourceParser.START_TAG && parser.name == "domain-config") {
            cleartextDomainConfig =
                parser.getAttributeBooleanValue(ANDROID_RES_NS, "cleartextTrafficPermitted", false)
        }
        if (parser.eventType == XmlResourceParser.START_TAG && parser.name == "domain" && cleartextDomainConfig) {
            domains.add(parser.nextText().trim())
            continue
        }
        if (parser.eventType == XmlResourceParser.END_TAG && parser.name == "domain-config") {
            cleartextDomainConfig = false
        }
        parser.next()
    }
    return domains
}
