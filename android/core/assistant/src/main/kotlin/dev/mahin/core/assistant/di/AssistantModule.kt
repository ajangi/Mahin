package dev.mahin.core.assistant.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.assistant.AssistantApi
import dev.mahin.core.assistant.RemoteHealthAssistantGateway
import dev.mahin.core.network.BuildConfig
import dev.mahin.core.network.MahinHttpClientFactory
import dev.mahin.domain.assistant.HealthAssistantGateway
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AssistantProvidesModule {
    @Provides
    @Singleton
    fun provideAssistantApi(): AssistantApi =
        MahinHttpClientFactory()
            .create(BuildConfig.MAHIN_API_BASE_URL)
            .create(AssistantApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AssistantBindsModule {
    @Binds
    @Singleton
    abstract fun bindHealthAssistantGateway(impl: RemoteHealthAssistantGateway): HealthAssistantGateway
}
