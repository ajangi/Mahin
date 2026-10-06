package dev.mahin.core.testing.roborazzi

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions

/**
 * Shared Roborazzi compare settings for V2 full-screen goldens.
 *
 * **Tolerance:** 0.5% pixel change (`changeThreshold = 0.005f`). Linux Robolectric native
 * graphics are stable between record and verify, so a tight threshold catches label/layout
 * drift on ~411×891dp-class captures without flaky CI. Re-record locally with
 * `./gradlew :app:recordRoborazziDebug` (and `:core:designsystem:recordRoborazziDebug`).
 */
@OptIn(ExperimentalRoborazziApi::class)
object MahinRoborazzi {
    val options: RoborazziOptions =
        RoborazziOptions(
            compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.005f),
        )
}
