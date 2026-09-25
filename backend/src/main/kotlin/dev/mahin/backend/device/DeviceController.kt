package dev.mahin.backend.device

import dev.mahin.backend.api.currentMahinSubject
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/v1/devices")
class DeviceController(
    private val deviceService: DeviceService,
) {
    @GetMapping
    fun listDevices(): DeviceListResponse = deviceService.listDevices(currentMahinSubject())

    @PostMapping("/{deviceId}/heartbeat")
    fun heartbeat(
        @PathVariable deviceId: UUID,
    ): DeviceSessionResponse {
        val updated =
            deviceService.touchDevice(currentMahinSubject(), deviceId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "device_not_found")
        return updated
    }
}
