package dev.mahin.core.database

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MahinDatabaseBootstrapTest {
    @Test
    fun schemaVersionStartsAtOneAndExports() {
        assertThat(MahinDatabase.VERSION).isEqualTo(3)
        assertThat(MahinDatabase.EXPORTS_SCHEMA).isTrue()
        assertThat(DatabaseEncryptionMode.UNENCRYPTED_BOOTSTRAP).isNotNull()
    }
}
