package dev.mahin.core.notifications

import dev.mahin.core.datastore.NotificationPrivacyMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultNotificationGateway
    @Inject
    constructor() : NotificationGateway {
        override fun canPost(mode: NotificationPrivacyMode): Boolean = mode != NotificationPrivacyMode.OFF
    }
