package dev.mahin.backend.notifications

/** Vendor-neutral push dispatch boundary. Production FCM/APNs wiring is deferred. */
interface PushNotificationDispatcher {
    fun dispatchHealthSafePing(deviceId: java.util.UUID): PushDispatchResult
}

data class PushDispatchResult(
    val accepted: Boolean,
    val detail: String,
)

class NoOpPushNotificationDispatcher : PushNotificationDispatcher {
    override fun dispatchHealthSafePing(deviceId: java.util.UUID): PushDispatchResult =
        PushDispatchResult(accepted = false, detail = "push_dispatch_not_configured")
}
