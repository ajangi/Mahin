package dev.mahin.core.content.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.content.CmsMahinIllustrationSource
import dev.mahin.core.content.ContentApi
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.content.DailyTipDataSource
import dev.mahin.core.media.illustration.MahinIllustrationSource
import dev.mahin.core.network.BuildConfig
import dev.mahin.core.network.MahinHttpClientFactory
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object ContentModule {
    @Provides
    @Singleton
    fun provideContentApi(): ContentApi =
        MahinHttpClientFactory()
            .create(BuildConfig.MAHIN_API_BASE_URL)
            .create(ContentApi::class.java)

    @Provides
    @Singleton
    fun provideContentJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ContentBindingModule {
    @Binds
    @Singleton
    abstract fun bindIllustrationSource(impl: CmsMahinIllustrationSource): MahinIllustrationSource

    @Binds
    @Singleton
    abstract fun bindDailyTipDataSource(impl: ContentRepository): DailyTipDataSource
}
