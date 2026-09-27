package dev.mahin.backend.content

/**
 * Normalizes Persian/Arabic character variants for consistent search matching.
 */
object PersianSearchNormalizer {
    fun normalize(input: String): String =
        input
            .trim()
            .replace('\u200c', ' ')
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('ة', 'ه')
            .lowercase()
}
