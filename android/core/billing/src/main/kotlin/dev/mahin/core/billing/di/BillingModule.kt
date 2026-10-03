package dev.mahin.core.billing.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.billing.BillingAdapter
import dev.mahin.core.billing.EntitlementApi
import dev.mahin.core.billing.GooglePlayBillingAdapter
import dev.mahin.core.network.BuildConfig
import dev.mahin.core.network.MahinHttpClientFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingBindingsModule {
    @Binds
    @Singleton
    abstract fun bindBillingAdapter(impl: GooglePlayBillingAdapter): BillingAdapter
}

@Module
@InstallIn(SingletonComponent::class)
object BillingProvidesModule {
    @Provides
    @Singleton
    fun provideEntitlementApi(): EntitlementApi =
        MahinHttpClientFactory()
            .create(BuildConfig.MAHIN_API_BASE_URL)
            .create(EntitlementApi::class.java)
}
