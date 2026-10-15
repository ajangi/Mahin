package dev.mahin.backend.content

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootTest
class ContentFlywayV9ToV10MigrationTest(
    @Autowired private val jdbcTemplate: JdbcTemplate,
) {
    @Test
    fun v10UpdatesSchemaBootstrapMarker() {
        val marker =
            jdbcTemplate.queryForObject(
                """
                SELECT meta_value FROM app_meta WHERE meta_key = 'schema_bootstrap'
                """.trimIndent(),
                String::class.java,
            )
        assertEquals("m18a", marker)
    }
}
