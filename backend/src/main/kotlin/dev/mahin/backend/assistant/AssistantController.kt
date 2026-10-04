package dev.mahin.backend.assistant

import dev.mahin.backend.api.currentRegisteredUser
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/assistant")
@Validated
class AssistantController(
    private val assistantService: AssistantService,
) {
    @GetMapping("/consent")
    fun getConsent(): AssistantConsentResponse {
        val user = currentRegisteredUser()
        return assistantService.getConsent(user.userId)
    }

    @PutMapping("/consent")
    fun updateConsent(
        @Valid @RequestBody request: UpdateAssistantConsentRequest,
    ): AssistantConsentResponse {
        val user = currentRegisteredUser()
        return assistantService.updateConsent(user.userId, request)
    }

    @PostMapping("/ask")
    fun ask(
        @Valid @RequestBody request: AssistantAskRequest,
    ): AssistantAskResponse {
        val user = currentRegisteredUser()
        return assistantService.ask(user.userId, request)
    }
}
