plugins {
    alias(libs.plugins.mahin.jvm.library)
}

dependencies {
    implementation(project(":core:common"))
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
