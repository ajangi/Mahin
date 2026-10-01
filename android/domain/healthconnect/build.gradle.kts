plugins {
    alias(libs.plugins.mahin.jvm.library)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
