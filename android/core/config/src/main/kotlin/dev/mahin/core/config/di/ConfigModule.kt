package dev.mahin.core.config.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.config.FeatureFlagGateway
import dev.mahin.core.config.MetaApi
import dev.mahin.core.config.RemoteFeatureFlagGateway
import dev.mahin.core.network.BuildConfig
import dev.mahin.core.network.MahinHttpClientFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConfigBindingsModule {
    @Binds
    @Singleton
    abstract fun bindFeatureFlagGateway(impl: RemoteFeatureFlagGateway): FeatureFlagGateway
}

@Module
@InstallIn(SingletonComponent::class)
object ConfigProvidesModule {
    @Provides
    @Singleton
    fun provideMetaApi(): MetaApi =
        MahinHttpClientFactory()
            .create(BuildConfig.MAHIN_API_BASE_URL)
            .create(MetaApi::class.java)
}
