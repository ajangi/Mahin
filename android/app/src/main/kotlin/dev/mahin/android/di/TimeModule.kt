package dev.mahin.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.common.LocalDateClock
import dev.mahin.core.common.SystemLocalDateClock
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides
    @Singleton
    fun provideLocalDateClock(): LocalDateClock = SystemLocalDateClock()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
