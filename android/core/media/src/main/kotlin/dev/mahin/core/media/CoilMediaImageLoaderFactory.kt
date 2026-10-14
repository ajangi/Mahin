package dev.mahin.core.media

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import java.io.File

object CoilMediaImageLoaderFactory {
    const val DISK_CACHE_DIR_NAME: String = "mahin_media_coil"

    fun diskCacheDirectory(context: Context): File = context.cacheDir.resolve(DISK_CACHE_DIR_NAME)

    fun buildDiskCache(
        context: Context,
        cachePolicy: MediaCachePolicy,
    ): DiskCache =
        DiskCache
            .Builder()
            .directory(diskCacheDirectory(context))
            .maxSizeBytes(cachePolicy.maxSizeBytes)
            .build()

    fun buildImageLoader(
        context: Context,
        cachePolicy: MediaCachePolicy,
    ): ImageLoader =
        ImageLoader
            .Builder(context)
            .diskCache { buildDiskCache(context, cachePolicy) }
            .build()
}
