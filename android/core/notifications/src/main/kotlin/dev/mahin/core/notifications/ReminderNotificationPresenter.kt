package dev.mahin.core.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotificationPresenter
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val gateway: NotificationGateway,
    ) {
        fun ensureChannel() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val channel =
                NotificationChannel(
                    MahinNotificationChannels.REMINDERS,
                    "یادآوری‌ها",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "یادآوری‌های محلی ماهین"
                    setShowBadge(true)
                }
            manager.createNotificationChannel(channel)
        }

        fun showReminder(request: ReminderNotificationRequest) {
            if (!gateway.canPost(request.privacyMode)) return
            ensureChannel()
            val body =
                MahinNotificationPreviewPipeline.resolveScheduledNotificationBody(
                    privacyMode = request.privacyMode,
                    suppressCelebratoryFromStore = request.suppressCelebratory,
                    latestOutcome = request.latestOutcome,
                    descriptiveFa = request.descriptiveFa ?: request.category.defaultDescriptiveFa(),
                )
            if (body.isBlank()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val notification =
                NotificationCompat
                    .Builder(context, MahinNotificationChannels.REMINDERS)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()
            postNotification(request.notificationId, notification)
        }

        @SuppressLint("MissingPermission")
        private fun postNotification(
            notificationId: Int,
            notification: android.app.Notification,
        ) {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }
