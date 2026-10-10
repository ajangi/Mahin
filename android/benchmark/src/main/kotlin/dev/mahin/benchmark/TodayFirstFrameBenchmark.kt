package dev.mahin.benchmark

import android.os.SystemClock
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodayFirstFrameBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun todayTabFirstFrames() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.DEFAULT,
            startupMode = StartupMode.WARM,
            iterations = 3,
        ) {
            pressHome()
            startActivityAndWait()
            waitForTodayHeroOrFirstDayCta()
        }
    }
}

private fun MacrobenchmarkScope.waitForTodayHeroOrFirstDayCta() {
    val deadline = SystemClock.uptimeMillis() + 10_000
    while (SystemClock.uptimeMillis() < deadline) {
        if (device.hasObject(By.text("ثبت اولین پریود"))) return
        if (device.hasObject(By.descContains("چرخه"))) return
        device.waitForIdle()
    }
    device.wait(Until.hasObject(By.pkg(TARGET_PACKAGE)), 1_000)
}

private const val TARGET_PACKAGE = "dev.mahin.android"
