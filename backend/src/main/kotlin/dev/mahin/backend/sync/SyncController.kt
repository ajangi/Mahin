package dev.mahin.backend.sync

import dev.mahin.backend.api.currentMahinSubject
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/sync")
@Validated
class SyncController(
    private val syncService: SyncService,
) {
    @PostMapping("/mutations")
    fun pushMutations(
        @Valid @RequestBody request: SyncMutationRequest,
    ): SyncMutationResponse = syncService.applyMutations(currentMahinSubject(), request)

    @GetMapping("/changes")
    fun pullChanges(
        @RequestParam(defaultValue = "0") @Min(0) afterRevision: Long,
        @RequestParam(defaultValue = "100") @Min(1) @Max(200) limit: Int,
    ): SyncChangesResponse = syncService.pullChanges(currentMahinSubject(), afterRevision, limit)
}
