plugins {
    alias(libs.plugins.mahin.android.library)
    alias(libs.plugins.mahin.android.hilt)
    alias(libs.plugins.mahin.android.room)
}

android {
    namespace = "dev.mahin.core.database"
    sourceSets {
        getByName("test") {
            assets.srcDir("$projectDir/schemas")
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:datetime"))
    implementation(project(":core:security"))
    implementation(project(":domain:cycle"))
    implementation(project(":domain:pregnancy"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.sqlite)
    implementation(libs.sqlcipher.android)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.core)
}
