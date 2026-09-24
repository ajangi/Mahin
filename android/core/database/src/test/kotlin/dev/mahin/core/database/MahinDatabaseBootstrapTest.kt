package dev.mahin.core.database

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MahinDatabaseBootstrapTest {
    @Test
    fun schemaVersionStartsAtOne() {
        val annotation = MahinDatabase::class.java.getAnnotation(androidx.room.Database::class.java)
        assertThat(annotation).isNotNull()
        assertThat(annotation!!.version).isEqualTo(1)
        assertThat(annotation.exportSchema).isTrue()
    }
}
