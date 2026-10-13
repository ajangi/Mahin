package dev.mahin.backend.content

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootTest
class ContentMigrationSchemaTest(
    @Autowired private val jdbcTemplate: JdbcTemplate,
) {
    @Test
    fun flywayAddsTypePayloadColumn() {
        val count =
            jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE UPPER(TABLE_NAME) = 'CONTENT_VERSION'
                  AND UPPER(COLUMN_NAME) = 'TYPE_PAYLOAD_JSON'
                """.trimIndent(),
                Int::class.java,
            ) ?: 0
        assertTrue(count >= 1, "expected type_payload_json on content_version")
    }
}
