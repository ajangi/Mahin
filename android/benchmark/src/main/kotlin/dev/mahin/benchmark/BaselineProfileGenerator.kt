package dev.mahin.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun generateStartupAndToday() {
        baselineRule.collect(TARGET_PACKAGE) {
            pressHome()
            startActivityAndWait()
        }
    }
}

private const val TARGET_PACKAGE = "dev.mahin.android"
