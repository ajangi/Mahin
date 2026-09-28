plugins {
    alias(libs.plugins.mahin.android.library)
    alias(libs.plugins.mahin.android.hilt)
}

android {
    namespace = "dev.mahin.core.security"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:datastore"))
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
