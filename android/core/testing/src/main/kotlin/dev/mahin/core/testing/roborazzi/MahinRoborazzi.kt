package dev.mahin.core.testing.roborazzi

import com.github.takahirom.roborazzi.RoborazziOptions

/**
 * Shared Roborazzi compare settings for V2 full-screen goldens.
 *
 * **Tolerance:** 2% pixel change (`changeThreshold = 0.02f`). Re-record locally with
 * `./gradlew :app:recordRoborazziDebug` (and `:core:designsystem:recordRoborazziDebug`).
 */
object MahinRoborazzi {
    val options: RoborazziOptions =
        RoborazziOptions(
            compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.02f),
        )
}
