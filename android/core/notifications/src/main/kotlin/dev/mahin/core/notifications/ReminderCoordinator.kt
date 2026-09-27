package dev.mahin.core.notifications

import android.content.Context
import android.content.Intent
import dev.mahin.core.datastore.NotificationPreferencesRepository
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking

@Singleton
class ReminderCoordinator
    @Inject
    constructor(
        private val scheduler: ReminderScheduler,
    ) {
        fun requestRefresh() {
            scheduler.requestFullRefresh()
        }
    }

class ReminderTimezoneChangedReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        if (intent?.action != Intent.ACTION_TIMEZONE_CHANGED) return
        val entryPoint =
            dagger.hilt.android.EntryPointAccessors.fromApplication(
                context.applicationContext,
                ReminderRefreshEntryPointInterface::class.java,
            )
        runBlocking {
            entryPoint.notificationPreferencesRepository().setZoneId(ZoneId.systemDefault().id)
        }
        entryPoint.reminderCoordinator().requestRefresh()
    }
}

object ReminderRefreshEntryPoint {
    fun refresh(context: Context) {
        val entryPoint =
            dagger.hilt.android.EntryPointAccessors.fromApplication(
                context.applicationContext,
                ReminderRefreshEntryPointInterface::class.java,
            )
        entryPoint.reminderCoordinator().requestRefresh()
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface ReminderRefreshEntryPointInterface {
    fun reminderCoordinator(): ReminderCoordinator

    fun notificationPreferencesRepository(): NotificationPreferencesRepository
}
