package dev.mahin.backend.privacy

import dev.mahin.backend.api.currentRegisteredUser
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/v1/privacy")
class PrivacyAccountController(
    private val privacyAccountService: PrivacyAccountService,
) {
    @PostMapping("/deletion-requests")
    fun requestDeletion(): DeletionRequestResponse {
        val user = currentRegisteredUser()
        return privacyAccountService.requestDeletion(user.userId)
    }

    @PostMapping("/export-jobs")
    fun requestExport(): ExportJobResponse {
        val user = currentRegisteredUser()
        return privacyAccountService.requestExport(user.userId)
    }

    @GetMapping("/export-jobs/{jobId}")
    fun getExportJob(
        @PathVariable jobId: UUID,
    ): ExportJobResponse {
        val user = currentRegisteredUser()
        return privacyAccountService.getExportJob(user.userId, jobId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "export_job_not_found")
    }
}
