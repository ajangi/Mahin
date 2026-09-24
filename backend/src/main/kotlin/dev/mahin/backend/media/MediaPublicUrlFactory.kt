package dev.mahin.backend.media

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MediaPublicUrlFactory(
    @Value("\${mahin.media.public-base-url}") private val publicBaseUrl: String,
) {
    fun urlFor(storageKey: String): String {
        require(storageKey.isNotBlank()) { "storageKey is required" }
        require(!storageKey.contains("://")) { "storageKey must not be an absolute URL" }
        require(!storageKey.contains("?")) { "storageKey must not include query parameters" }
        require(!storageKey.contains("user", ignoreCase = true)) {
            "storageKey must not include user identifiers"
        }
        return publicBaseUrl.trimEnd('/') + "/" + storageKey.trimStart('/')
    }
}

interface ObjectStorageGateway {
    fun exists(storageKey: String): Boolean
}

class LocalObjectStorageGateway : ObjectStorageGateway {
    override fun exists(storageKey: String): Boolean = storageKey.isNotBlank()
}
