package dev.mahin.core.healthconnect.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.healthconnect.AndroidHealthConnectRemoteClient
import dev.mahin.core.healthconnect.HealthConnectCoordinator
import dev.mahin.core.healthconnect.HealthConnectCoordinatorFacade
import dev.mahin.domain.healthconnect.HealthConnectRemoteClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthConnectModule {
    @Binds
    @Singleton
    abstract fun bindHealthConnectRemoteClient(impl: AndroidHealthConnectRemoteClient): HealthConnectRemoteClient

    @Binds
    @Singleton
    abstract fun bindHealthConnectCoordinatorFacade(impl: HealthConnectCoordinator): HealthConnectCoordinatorFacade
}
