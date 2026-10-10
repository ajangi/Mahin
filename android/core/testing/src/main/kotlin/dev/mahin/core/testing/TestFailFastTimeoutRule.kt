package dev.mahin.core.testing

import java.util.concurrent.TimeUnit
import org.junit.rules.TestRule
import org.junit.rules.Timeout
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Per-test wall-clock bound (default 60s) that prints a full thread dump before propagating
 * [org.junit.runners.model.TestTimedOutException].
 *
 * Do not use with Robolectric tests that call [org.robolectric.shadows.ShadowLooper.idleMainLooper]:
 * JUnit's timeout runs the test on a worker thread, which breaks main-looper control. Prefer
 * [TestHangWatchdogRule] for those tests.
 */
class TestFailFastTimeoutRule(
    private val timeoutMs: Long = 60_000L,
) : TestRule {
    private val junitTimeout =
        Timeout
            .builder()
            .withTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .withLookingForStuckThread(true)
            .build()

    override fun apply(
        base: Statement,
        description: Description,
    ): Statement {
        val timed = junitTimeout.apply(base, description)
        return object : Statement() {
            override fun evaluate() {
                try {
                    timed.evaluate()
                } catch (t: Throwable) {
                    TestHangWatchdogRule.dumpAllStackTraces(description.displayName)
                    throw t
                }
            }
        }
    }
}
