plugins {
    alias(libs.plugins.mahin.android.library)
    alias(libs.plugins.mahin.android.compose)
}

android {
    namespace = "dev.mahin.core.testing"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:model"))
    api(project(":core:designsystem"))
    api(libs.junit)
    api(libs.truth)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui.test.junit4)
    api(libs.roborazzi)
    api(libs.roborazzi.compose)
}
