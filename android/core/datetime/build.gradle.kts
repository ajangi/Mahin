plugins {
    alias(libs.plugins.mahin.jvm.library)
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.datetime)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
