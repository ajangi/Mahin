package dev.mahin.core.sync

import com.google.common.truth.Truth.assertThat
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SyncOutboxTest {
    @Test
    fun enqueueIsIdempotentKeyAwareAtTheModelLayer() =
        runBlocking {
            val outbox = InMemorySyncOutbox()
            val mutation =
                SyncMutation(
                    id = UUID.randomUUID(),
                    entityType = "app_meta",
                    entityId = UUID.randomUUID(),
                    idempotencyKey = "meta-1",
                    createdAtEpochMs = 1L,
                )
            outbox.enqueue(mutation)
            assertThat(outbox.pending()).hasSize(1)
            assertThat(outbox.pending().first().idempotencyKey).isEqualTo("meta-1")
        }
}
