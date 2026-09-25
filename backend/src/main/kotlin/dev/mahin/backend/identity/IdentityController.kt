package dev.mahin.backend.identity

import dev.mahin.backend.api.currentRegisteredUser
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/identity")
class IdentityController(
    private val identityService: IdentityService,
) {
    @PostMapping("/guest")
    fun bootstrapGuest(
        @Valid @RequestBody request: GuestBootstrapRequest,
    ): GuestBootstrapResponse = identityService.bootstrapGuest(request)

    @PostMapping("/convert-guest")
    fun convertGuest(
        @Valid @RequestBody request: ConvertGuestRequest,
    ): ConvertGuestResponse {
        val user = currentRegisteredUser()
        return identityService.convertGuest(request.localUserId, user.userId)
    }
}
