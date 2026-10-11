package dev.mahin.core.sync.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.sync.NoOpSyncOutbox
import dev.mahin.core.sync.SyncOutbox
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    @Binds
    @Singleton
    abstract fun bindSyncOutbox(impl: NoOpSyncOutbox): SyncOutbox
}
