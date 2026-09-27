package dev.mahin.core.content.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.content.ContentApi
import dev.mahin.core.network.MahinHttpClientFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContentModule {
    private const val DEFAULT_API_BASE = "http://10.0.2.2:8080/"

    @Provides
    @Singleton
    fun provideContentApi(): ContentApi =
        MahinHttpClientFactory()
            .create(DEFAULT_API_BASE)
            .create(ContentApi::class.java)
}
