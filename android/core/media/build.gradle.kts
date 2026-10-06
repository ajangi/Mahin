plugins {
    alias(libs.plugins.mahin.android.library)
    alias(libs.plugins.mahin.android.compose)
    alias(libs.plugins.mahin.android.hilt)
}

android {
    namespace = "dev.mahin.core.media"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:config"))
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(project(":core:testing"))
}
