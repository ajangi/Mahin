package dev.mahin.core.healthconnect.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.healthconnect.AndroidHealthConnectClientGateway
import dev.mahin.core.healthconnect.HealthConnectClientGateway
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthConnectModule {
    @Binds
    @Singleton
    abstract fun bindHealthConnectClientGateway(impl: AndroidHealthConnectClientGateway): HealthConnectClientGateway
}
