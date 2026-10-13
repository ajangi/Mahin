package dev.mahin.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.android.learn.DefaultPlayerSessionTicker
import dev.mahin.android.learn.PlayerSessionTicker
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LearnModule {
    @Provides
    @Singleton
    fun providePlayerSessionTicker(): PlayerSessionTicker = DefaultPlayerSessionTicker
}
