package dev.mahin.core.testing

import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Daemon watchdog: if the test body is still running after [timeoutMs], dumps all thread stacks to
 * stderr and interrupts the test thread. It does **not** end tests blocked on locks — hung tests
 * may still run until the Gradle task timeout.
 *
 * **Limitation:** [Thread.interrupt] does not unblock threads stuck in
 * [java.util.concurrent.locks.ReentrantLock.lock] (e.g. Room/SQLite teardown deadlocks). The
 * watchdog is for **diagnostics** (stack dump) and marking the test thread interrupted; it does
 * not forcibly release locks.
 *
 * On CI, set env `MAHIN_TEST_VERBOSE_STREAMS=true` (see `app/build.gradle.kts`) so stderr dumps
 * are included in the test log; otherwise they still go to stderr but may be filtered from reports.
 */
class TestHangWatchdogRule(
    private val timeoutMs: Long = 60_000L,
) : TestRule {
    override fun apply(
        base: Statement,
        description: Description,
    ): Statement =
        object : Statement() {
            override fun evaluate() {
                val finished = AtomicBoolean(false)
                val testThread = Thread.currentThread()
                val watchdog =
                    Thread(
                        {
                            try {
                                Thread.sleep(timeoutMs)
                            } catch (_: InterruptedException) {
                                return@Thread
                            }
                            if (finished.get()) return@Thread
                            dumpAllStackTraces(description.displayName)
                            testThread.interrupt()
                        },
                        "test-hang-watchdog-${description.methodName}",
                    ).apply { isDaemon = true }
                watchdog.start()
                try {
                    base.evaluate()
                } finally {
                    finished.set(true)
                    watchdog.interrupt()
                }
            }
        }

    companion object {
        fun dumpAllStackTraces(testLabel: String) {
            val writer = StringWriter()
            val out = PrintWriter(writer)
            out.println("=== Test hang watchdog: $testLabel still running; thread dump ===")
            for ((thread, stack) in Thread.getAllStackTraces()) {
                out.println("--- ${thread.name} (${thread.state}) ---")
                for (element in stack) {
                    out.println("    at $element")
                }
            }
            out.flush()
            System.err.println(writer.toString())
        }
    }
}
