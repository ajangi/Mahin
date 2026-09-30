plugins {
    alias(libs.plugins.mahin.android.application)
    alias(libs.plugins.mahin.android.compose)
    alias(libs.plugins.mahin.android.hilt)
}

android {
    namespace = "dev.mahin.android"
    defaultConfig {
        // Non-production working identity. Production applicationId is NOT locked in M0.
        // See docs/adr/0003-android-application-id.md
        applicationId = "dev.mahin.android"
        versionCode = 1
        versionName = "0.0.5-m4"
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:datetime"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:config"))
    implementation(project(":core:media"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:security"))
    implementation(project(":core:analytics"))
    implementation(project(":core:sync"))
    implementation(project(":core:content"))
    implementation(project(":core:notifications"))
    implementation(project(":core:billing"))
    implementation(project(":domain:cycle"))
    implementation(project(":domain:fertility"))
    implementation(project(":domain:pregnancy"))
    implementation(project(":domain:content"))
    implementation(project(":domain:account"))
    implementation(project(":domain:subscription"))
    implementation(project(":domain:reminders"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.biometric)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(project(":core:testing"))
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.room.runtime)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.withType<Test>().configureEach {
    if (name == "testReleaseUnitTest") {
        filter {
            excludeTestsMatching("dev.mahin.android.demo.CalendarDemoScreenScrollTest")
            excludeTestsMatching("dev.mahin.android.cycle.HistoryScreenScrollTest")
            excludeTestsMatching("dev.mahin.android.ttc.TtcInsightsScreenScrollTest")
            excludeTestsMatching("dev.mahin.android.pregnancy.PregnancyHubScreenScrollTest")
            excludeTestsMatching("dev.mahin.android.pregnancy.PregnancyStartSheetScrollTest")
        }
    }
}
