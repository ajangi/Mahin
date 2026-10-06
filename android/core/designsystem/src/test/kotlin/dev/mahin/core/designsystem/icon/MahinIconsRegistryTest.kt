package dev.mahin.core.designsystem.icon

import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.xmlpull.v1.XmlPullParser

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

@RunWith(RobolectricTestRunner::class)
class MahinIconsRegistryTest {
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<android.content.Context>().resources

    @Test
    fun everyIcon_hasUniqueSemanticId_andUniqueNonBlankFaDescription() {
        val ids = MahinIcons.all.map { it.semanticId }
        assertThat(ids).containsNoDuplicates()
        assertThat(ids).hasSize(63)
        val descriptions =
            MahinIcons.all.map { spec ->
                resources.getString(spec.contentDescriptionRes).trim()
            }
        assertThat(descriptions).containsNoDuplicates()
        descriptions.forEach { assertThat(it).isNotEmpty() }
    }

    @Test
    fun directionalActionIcons_markAutoMirrored() {
        assertThat(MahinIcons.Action.back.autoMirrored).isTrue()
        assertThat(MahinIcons.Action.undo.autoMirrored).isTrue()
        assertThat(MahinIcons.Action.share.autoMirrored).isFalse()
    }
}

@RunWith(RobolectricTestRunner::class)
class MahinIconDrawableQualityTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun everyIconDrawable_hasVisibleStrokeGeometry_andUniquePathSets() {
        val pathSets = mutableMapOf<String, String>()
        MahinIcons.all.forEach { spec ->
            val signatures = parseVectorPathSignature(spec.drawableRes)
            assertThat(signatures).isNotEmpty()
            signatures.forEach { sig ->
                assertThat(sig).contains("stroke=")
                assertThat(sig).doesNotContain("pathData=|")
            }
            val key = signatures.sorted().joinToString("|")
            pathSets[spec.semanticId] = key
        }
        val duplicates =
            pathSets.entries
                .groupBy { it.value }
                .filter { it.value.size > 1 }
                .keys
        assertThat(duplicates).isEmpty()
    }

    private fun parseVectorPathSignature(drawableRes: Int): List<String> {
        val parser = context.resources.getXml(drawableRes)
        val signatures = mutableListOf<String>()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name == "path") {
                val pathData = parser.getAttributeValue(ANDROID_NS, "pathData")?.trim().orEmpty()
                val stroke = parser.getAttributeValue(ANDROID_NS, "strokeWidth")?.trim().orEmpty()
                val fill = parser.getAttributeValue(ANDROID_NS, "fillColor")?.trim().orEmpty()
                assertThat(pathData).isNotEmpty()
                assertThat(stroke).isNotEmpty()
                assertThat(stroke.toFloat()).isGreaterThan(0f)
                signatures.add("pathData=$pathData|stroke=$stroke|fill=$fill")
            }
            event = parser.next()
        }
        parser.close()
        return signatures
    }
}
