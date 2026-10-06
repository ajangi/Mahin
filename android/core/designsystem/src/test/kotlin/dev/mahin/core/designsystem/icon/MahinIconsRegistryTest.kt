package dev.mahin.core.designsystem.icon

import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MahinIconsRegistryTest {
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<android.content.Context>().resources

    @Test
    fun everyIcon_hasUniqueSemanticId_andNonBlankFaDescription() {
        val ids = MahinIcons.all.map { it.semanticId }
        assertThat(ids).containsNoDuplicates()
        assertThat(ids).hasSize(63)
        MahinIcons.all.forEach { spec ->
            val description = resources.getString(spec.contentDescriptionRes)
            assertThat(description.trim()).isNotEmpty()
        }
    }

    @Test
    fun directionalActionIcons_markAutoMirrored() {
        assertThat(MahinIcons.Action.back.autoMirrored).isTrue()
        assertThat(MahinIcons.Action.share.autoMirrored).isTrue()
        assertThat(MahinIcons.Action.undo.autoMirrored).isTrue()
    }
}
