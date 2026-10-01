plugins {
    alias(libs.plugins.mahin.android.library)
    alias(libs.plugins.mahin.android.hilt)
}

android {
    namespace = "dev.mahin.core.config"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}
