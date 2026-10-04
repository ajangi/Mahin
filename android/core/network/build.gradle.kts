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

tasks.register("verifyReleaseMahinApiBaseUrlHttps") {
    group = "verification"
    description = "Fails if release MAHIN_API_BASE_URL BuildConfig is not HTTPS."
    dependsOn("generateReleaseBuildConfig")
    doLast {
        val buildConfigDir =
            layout.buildDirectory
                .dir("generated/source/buildConfig/release")
                .get()
                .asFile
        val buildConfigFile =
            buildConfigDir
                .walkTopDown()
                .firstOrNull { it.isFile && it.name == "BuildConfig.java" }
                ?: error("Release BuildConfig not found under ${buildConfigDir.path}")
        val content = buildConfigFile.readText()
        val match =
            Regex("""MAHIN_API_BASE_URL\s*=\s*"([^"]+)"""")
                .find(content)
                ?: error("MAHIN_API_BASE_URL not found in ${buildConfigFile.path}")
        val url = match.groupValues[1]
        check(url.startsWith("https://")) {
            "Release MAHIN_API_BASE_URL must use HTTPS (was $url). Override mahin.api.baseUrl.release for staging only."
        }
    }
}
