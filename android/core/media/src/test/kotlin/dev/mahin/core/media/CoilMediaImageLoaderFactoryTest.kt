package dev.mahin.core.media

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CoilMediaImageLoaderFactoryTest {
    @Test
    fun diskCacheUsesBoundedDirectoryAndSize() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val policy = MediaCachePolicy(maxSizeBytes = 64L * 1024L * 1024L)
        val cache = CoilMediaImageLoaderFactory.buildDiskCache(context, policy)
        assertThat(cache.directory.name).isEqualTo(CoilMediaImageLoaderFactory.DISK_CACHE_DIR_NAME)
        assertThat(cache.maxSize).isEqualTo(policy.maxSizeBytes)
    }
}
