package dev.mahin.core.security.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.security.AppLockGateway
import dev.mahin.core.security.DeviceKeyMaterial
import dev.mahin.core.security.DisabledAppLockGateway
import dev.mahin.core.security.KeystoreDeviceKeyMaterial
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {
    @Binds
    @Singleton
    abstract fun bindDeviceKeyMaterial(impl: KeystoreDeviceKeyMaterial): DeviceKeyMaterial

    companion object {
        @Provides
        @Singleton
        fun provideAppLockGateway(): AppLockGateway = DisabledAppLockGateway
    }
}
