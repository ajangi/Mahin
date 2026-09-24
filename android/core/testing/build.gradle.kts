plugins {
    alias(libs.plugins.mahin.android.library)
}

android {
    namespace = "dev.mahin.core.testing"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:model"))
    api(libs.junit)
    api(libs.truth)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
