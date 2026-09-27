package dev.mahin.backend.cms

enum class CmsRole {
    SUPER_ADMIN,
    MEDICAL_REVIEWER,
    EDITOR,
    SUPPORT,
    ANALYST,
    ;

    fun authority(): String = "ROLE_CMS_$name"
}
