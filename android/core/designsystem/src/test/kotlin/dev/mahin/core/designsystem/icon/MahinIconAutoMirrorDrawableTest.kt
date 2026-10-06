package dev.mahin.core.designsystem.icon

import android.graphics.drawable.VectorDrawable
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MahinIconAutoMirrorDrawableTest {
    @Test
    fun directionalDrawables_haveAutoMirroredVectorFlag() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        listOf(
            MahinIcons.Action.back,
            MahinIcons.Action.share,
            MahinIcons.Action.undo,
        ).forEach { spec ->
            val drawable = context.getDrawable(spec.drawableRes)
            assertThat(drawable).isInstanceOf(VectorDrawable::class.java)
            assertThat((drawable as VectorDrawable).isAutoMirrored).isTrue()
        }
    }
}
