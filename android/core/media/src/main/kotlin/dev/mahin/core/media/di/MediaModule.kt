package dev.mahin.core.media.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.media.MediaCachePolicy
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        cachePolicy: MediaCachePolicy,
    ): ImageLoader =
        ImageLoader
            .Builder(context)
            .diskCache {
                DiskCache
                    .Builder()
                    .directory(context.cacheDir.resolve("mahin_media_coil"))
                    .maxSizeBytes(cachePolicy.maxSizeBytes)
                    .build()
            }.build()

    @Provides
    @Singleton
    fun provideMediaCachePolicy(): MediaCachePolicy = MediaCachePolicy()
}
