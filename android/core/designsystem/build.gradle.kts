plugins {
    alias(libs.plugins.mahin.android.library)
    alias(libs.plugins.mahin.android.compose)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "dev.mahin.core.designsystem"
}

roborazzi {
    outputDir.set(file("src/test/screenshots"))
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:datetime"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material.icons)
    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.compose.ui.test.manifest)
}
