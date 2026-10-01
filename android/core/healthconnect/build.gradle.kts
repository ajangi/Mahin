plugins {
    alias(libs.plugins.mahin.android.library)
    alias(libs.plugins.mahin.android.hilt)
}

android {
    namespace = "dev.mahin.core.healthconnect"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:config"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:model"))
    implementation(project(":core:security"))
    implementation(project(":domain:healthconnect"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.health.connect.client)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}
