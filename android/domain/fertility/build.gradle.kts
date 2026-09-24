plugins {
    alias(libs.plugins.mahin.jvm.library)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:datetime"))
    implementation(project(":domain:cycle"))
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
