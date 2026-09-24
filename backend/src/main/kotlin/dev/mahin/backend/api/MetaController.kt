package dev.mahin.backend.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MetaController(
    @Value("\${mahin.api-version}") private val apiVersion: String,
    @Value("\${mahin.environment}") private val environment: String,
) {
    @GetMapping("/v1/meta")
    fun meta(): MetaResponse =
        MetaResponse(
            apiVersion = apiVersion,
            environment = environment,
        )
}
