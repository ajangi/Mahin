package dev.mahin.backend.notifications

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PushNotificationConfiguration {
    @Bean
    fun pushNotificationDispatcher(): PushNotificationDispatcher = NoOpPushNotificationDispatcher()
}
