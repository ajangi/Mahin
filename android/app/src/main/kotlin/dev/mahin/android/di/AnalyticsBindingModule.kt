package dev.mahin.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.analytics.AnalyticsGateway
import dev.mahin.core.analytics.GuardedAnalyticsGateway
import dev.mahin.core.analytics.NoOpAnalyticsGateway
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsBindingModule {
    @Provides
    @Singleton
    fun provideAnalyticsGateway(): AnalyticsGateway = GuardedAnalyticsGateway(NoOpAnalyticsGateway)
}
