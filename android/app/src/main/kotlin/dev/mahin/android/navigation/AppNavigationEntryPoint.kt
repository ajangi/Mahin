package dev.mahin.android.navigation

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppNavigationEntryPoint {
    fun logTabDateRequest(): LogTabDateRequest
}
