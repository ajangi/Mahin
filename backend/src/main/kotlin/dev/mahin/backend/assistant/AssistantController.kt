package dev.mahin.backend.assistant

import dev.mahin.backend.api.currentRegisteredUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/assistant")
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
        @RequestBody request: UpdateAssistantConsentRequest,
    ): AssistantConsentResponse {
        val user = currentRegisteredUser()
        return assistantService.updateConsent(user.userId, request)
    }

    @PostMapping("/ask")
    fun ask(
        @RequestBody request: AssistantAskRequest,
    ): AssistantAskResponse {
        val user = currentRegisteredUser()
        return assistantService.ask(user.userId, request)
    }
}
