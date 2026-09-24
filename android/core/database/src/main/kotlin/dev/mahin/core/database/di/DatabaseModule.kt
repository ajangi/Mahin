package dev.mahin.core.database.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.database.DatabaseEncryptionMode
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.MahinDatabaseFactory
import dev.mahin.core.database.MahinDatabaseProvider
import dev.mahin.core.security.DeviceKeyMaterial
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideMahinDatabase(
        @ApplicationContext context: Context,
        keyMaterial: DeviceKeyMaterial,
    ): MahinDatabase =
        MahinDatabaseFactory.build(
            context = context,
            keyMaterial = keyMaterial,
            encryptionMode = DatabaseEncryptionMode.KEYSTORE_SQLCIPHER,
        )

    @Provides
    @Singleton
    fun provideMahinDatabaseProvider(database: MahinDatabase): MahinDatabaseProvider =
        object : MahinDatabaseProvider {
            override fun database(): MahinDatabase = database
        }
}
