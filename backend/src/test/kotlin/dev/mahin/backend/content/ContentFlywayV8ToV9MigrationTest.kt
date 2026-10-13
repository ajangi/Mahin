package dev.mahin.backend.content

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootTest
class ContentFlywayV8ToV9MigrationTest(
    @Autowired private val jdbcTemplate: JdbcTemplate,
) {
    @Test
    fun preV9EnvelopeVersionRowSurvivesV9MigrationWithNullPayload() {
        val title =
            jdbcTemplate.queryForObject(
                """
                SELECT title FROM content_version
                WHERE id = '22222222-2222-2222-2222-222222222223'
                """.trimIndent(),
                String::class.java,
            )
        assertNotNull(title)
        val payload =
            jdbcTemplate.queryForObject(
                """
                SELECT type_payload_json FROM content_version
                WHERE id = '22222222-2222-2222-2222-222222222223'
                """.trimIndent(),
                String::class.java,
            )
        assertNull(payload)
    }
}
