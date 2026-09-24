plugins {
    alias(libs.plugins.mahin.android.library)
    alias(libs.plugins.mahin.android.compose)
}

android {
    namespace = "dev.mahin.core.designsystem"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
