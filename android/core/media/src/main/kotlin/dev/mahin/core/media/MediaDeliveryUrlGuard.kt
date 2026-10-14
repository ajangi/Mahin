package dev.mahin.core.media

import android.util.Log

/**
 * Validates public media URLs. Unsafe URLs must never crash the app; callers fail safe to the neutral frame.
 */
object MediaDeliveryUrlGuard {
    private const val TAG = "MediaDeliveryUrlGuard"

    private val forbiddenIdentifier =
        Regex("""(?i)(?:^|[/?&#])(?:userId|userid|user_id|guestUserId|localUserId)(?:=|/|$)""")

    fun isSafePublicMediaUrl(url: String): Boolean {
        if (url.isBlank()) return false
        if ('?' in url || '#' in url) return false
        if (forbiddenIdentifier.containsMatchIn(url)) return false
        return true
    }

    fun logUnsafeUrl(reason: String) {
        Log.w(TAG, "Blocked media URL: $reason")
    }
}
