package dev.mahin.core.billing.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.billing.BillingAdapter
import dev.mahin.core.billing.EntitlementApi
import dev.mahin.core.billing.GooglePlayBillingAdapter
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
    private const val DEFAULT_API_BASE = "http://10.0.2.2:8080/"

    @Provides
    @Singleton
    fun provideEntitlementApi(): EntitlementApi =
        MahinHttpClientFactory()
            .create(DEFAULT_API_BASE)
            .create(EntitlementApi::class.java)
}
