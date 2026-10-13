package dev.mahin.core.media.di

import android.content.Context
import coil.ImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.media.CoilMediaImageLoaderFactory
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
    ): ImageLoader = CoilMediaImageLoaderFactory.buildImageLoader(context, cachePolicy)

    @Provides
    @Singleton
    fun provideMediaCachePolicy(): MediaCachePolicy = MediaCachePolicy()
}
