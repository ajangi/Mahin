package dev.mahin.core.notifications

import android.content.Context
import android.content.Intent
import javax.inject.Inject
import javax.inject.Singleton

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
        ReminderRefreshEntryPoint.refresh(context)
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
}
