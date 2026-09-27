package dev.mahin.core.notifications.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mahin.core.notifications.AndroidReminderWorkScheduler
import dev.mahin.core.notifications.DefaultNotificationGateway
import dev.mahin.core.notifications.NoOpPushRegistrationGateway
import dev.mahin.core.notifications.NotificationGateway
import dev.mahin.core.notifications.PushRegistrationGateway
import dev.mahin.core.notifications.ReminderScheduler
import dev.mahin.core.notifications.ReminderWorkScheduler
import dev.mahin.core.notifications.WorkManagerReminderScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {
    @Binds
    @Singleton
    abstract fun bindNotificationGateway(impl: DefaultNotificationGateway): NotificationGateway

    @Binds
    @Singleton
    abstract fun bindReminderWorkScheduler(impl: AndroidReminderWorkScheduler): ReminderWorkScheduler

    companion object {
        @Provides
        @Singleton
        fun provideReminderScheduler(workScheduler: ReminderWorkScheduler): ReminderScheduler =
            WorkManagerReminderScheduler(workScheduler)

        @Provides
        @Singleton
        fun providePushRegistrationGateway(): PushRegistrationGateway = NoOpPushRegistrationGateway()
    }
}
