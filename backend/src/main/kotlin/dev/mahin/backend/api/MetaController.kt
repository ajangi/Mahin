package dev.mahin.backend.api

import dev.mahin.backend.config.MahinFeatureFlagsProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MetaController(
    @Value("\${mahin.api-version}") private val apiVersion: String,
    @Value("\${mahin.environment}") private val environment: String,
    private val featureFlags: MahinFeatureFlagsProperties,
) {
    @GetMapping("/v1/meta")
    fun meta(): MetaResponse =
        MetaResponse(
            apiVersion = apiVersion,
            environment = environment,
            featureFlags = featureFlags.asPublicMap(),
        )
}
