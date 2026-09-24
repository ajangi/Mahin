package dev.mahin.backend.api

data class ErrorResponse(
    val code: String,
    val message: String,
    val requestId: String,
)

data class HealthResponse(
    val status: String,
)

data class MetaResponse(
    val apiVersion: String,
    val environment: String,
    val product: String = "mahin",
)
