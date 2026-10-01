package dev.mahin.core.config.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.config.FeatureFlagGateway
import dev.mahin.core.config.MetaApi
import dev.mahin.core.config.RemoteFeatureFlagGateway
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
    private const val DEFAULT_API_BASE = "http://10.0.2.2:8080/"

    @Provides
    @Singleton
    fun provideMetaApi(): MetaApi =
        MahinHttpClientFactory()
            .create(DEFAULT_API_BASE)
            .create(MetaApi::class.java)
}
