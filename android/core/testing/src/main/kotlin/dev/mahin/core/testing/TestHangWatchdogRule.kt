package dev.mahin.core.testing

import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Daemon watchdog: if the test body is still running after [timeoutMs], dumps all thread stacks to
 * stderr and interrupts the test thread so the run fails fast instead of hanging until Gradle's
 * task timeout.
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
