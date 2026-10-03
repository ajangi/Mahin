plugins {
    alias(libs.plugins.mahin.android.library)
    alias(libs.plugins.mahin.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.mahin.core.network"
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        val releaseBase =
            project.findProperty("mahin.api.baseUrl.release") as String?
                ?: "https://api.mahin.app/"
        buildConfigField("String", "MAHIN_API_BASE_URL", quotedGradleString(releaseBase))
    }
    buildTypes {
        debug {
            val debugBase =
                project.findProperty("mahin.api.baseUrl.debug") as String?
                    ?: "http://10.0.2.2:8080/"
            buildConfigField("String", "MAHIN_API_BASE_URL", quotedGradleString(debugBase))
        }
        release {
            val releaseBase =
                project.findProperty("mahin.api.baseUrl.release") as String?
                    ?: "https://api.mahin.app/"
            buildConfigField("String", "MAHIN_API_BASE_URL", quotedGradleString(releaseBase))
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}

private fun quotedGradleString(value: String): String {
    val escaped = value.replace("\\", "\\\\").replace("\"", "\\\"")
    return "\"$escaped\""
}
