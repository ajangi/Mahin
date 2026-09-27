package dev.mahin.domain.content

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
