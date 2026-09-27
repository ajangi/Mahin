package dev.mahin.backend.notifications

import dev.mahin.backend.api.currentMahinSubject
import java.util.UUID
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/devices/{deviceId}/push-token")
class PushTokenController(
    private val pushTokenService: PushTokenService,
) {
    @PutMapping
    fun registerPushToken(
        @PathVariable deviceId: UUID,
        @RequestBody body: RegisterPushTokenRequest,
    ): PushTokenRegistrationResponse = pushTokenService.registerToken(currentMahinSubject(), deviceId, body)

    @DeleteMapping
    fun clearPushToken(
        @PathVariable deviceId: UUID,
    ) {
        pushTokenService.clearToken(currentMahinSubject(), deviceId)
    }
}
