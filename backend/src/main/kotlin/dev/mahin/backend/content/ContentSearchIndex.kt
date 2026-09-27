package dev.mahin.backend.content

object ContentSearchIndex {
    fun build(
        title: String,
        summary: String?,
    ): String = PersianSearchNormalizer.normalize(listOfNotNull(title, summary).joinToString(" "))
}
