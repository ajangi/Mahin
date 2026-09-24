package dev.mahin.core.media

import dev.mahin.core.config.MediaDeliveryConfig
import dev.mahin.core.model.MediaAssetRef

data class MediaRequest(
    val url: String,
    val asset: MediaAssetRef,
    val isAuthoritative: Boolean,
)

interface MediaUrlResolver {
    fun resolve(asset: MediaAssetRef): MediaRequest?
}

class ConfiguredMediaUrlResolver(
    private val config: MediaDeliveryConfig,
) : MediaUrlResolver {
    override fun resolve(asset: MediaAssetRef): MediaRequest? {
        if (!asset.isAuthoritativeProductionContent) return null
        val url = config.urlFor(asset.storageKey)
        check(!url.contains("userId", ignoreCase = true))
        check(!url.contains("cycle"))
        check('?' !in url) { "Media URLs must not carry query parameters in M0 public delivery" }
        return MediaRequest(url = url, asset = asset, isAuthoritative = true)
    }
}

interface MediaImageLoader {
    fun canLoad(request: MediaRequest): Boolean
}

/**
 * Coil (or any other loader) is an implementation detail behind this boundary.
 * Feature UI must not hard-code CDN hosts.
 */
class BoundedCacheMediaImageLoader : MediaImageLoader {
    override fun canLoad(request: MediaRequest): Boolean = request.isAuthoritative
}

data class MediaCachePolicy(
    val maxSizeBytes: Long = 64L * 1024L * 1024L,
    val degradeWhenUnavailable: Boolean = true,
)
