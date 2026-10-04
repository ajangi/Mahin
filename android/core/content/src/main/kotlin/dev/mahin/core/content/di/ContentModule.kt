package dev.mahin.core.content.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.content.ContentApi
import dev.mahin.core.network.BuildConfig
import dev.mahin.core.network.MahinHttpClientFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContentModule {
    @Provides
    @Singleton
    fun provideContentApi(): ContentApi =
        MahinHttpClientFactory()
            .create(BuildConfig.MAHIN_API_BASE_URL)
            .create(ContentApi::class.java)
}
