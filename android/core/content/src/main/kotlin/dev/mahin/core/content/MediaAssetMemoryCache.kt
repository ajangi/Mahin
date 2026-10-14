package dev.mahin.core.content

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaAssetMemoryCache
    @Inject
    constructor() {
        private val byId = ConcurrentHashMap<String, MediaAssetDto>()

        fun put(asset: MediaAssetDto) {
            byId[asset.id] = asset
        }

        fun get(assetId: String): MediaAssetDto? = byId[assetId]
    }
