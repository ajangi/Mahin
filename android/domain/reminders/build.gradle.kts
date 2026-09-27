plugins {
    alias(libs.plugins.mahin.jvm.library)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":domain:cycle"))
    implementation(project(":domain:pregnancy"))
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
