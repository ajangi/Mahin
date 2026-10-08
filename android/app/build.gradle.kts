import java.time.Duration
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

plugins {
    alias(libs.plugins.mahin.android.application)
    alias(libs.plugins.mahin.android.compose)
    alias(libs.plugins.mahin.android.hilt)
    alias(libs.plugins.roborazzi)
}

roborazzi {
    outputDir.set(file("src/test/screenshots"))
}

android {
    namespace = "dev.mahin.android"
    defaultConfig {
        // Non-production working identity. Production applicationId is NOT locked in M0.
        // See docs/adr/0003-android-application-id.md
        applicationId = "dev.mahin.android"
        versionCode =
            (project.findProperty("mahin.versionCode") as String?)?.toIntOrNull()
                ?: 1_100_001
        versionName =
            project.findProperty("mahin.versionName") as String?
                ?: "1.0.0-rc1"
    }
    signingConfigs {
        create("release") {
            val keystorePath =
                providers
                    .gradleProperty("mahin.release.keystorePath")
                    .orElse(providers.environmentVariable("MAHIN_RELEASE_KEYSTORE_PATH"))
                    .orNull
                    ?.trim()
            if (!keystorePath.isNullOrEmpty()) {
                val keystoreFile = rootProject.file(keystorePath)
                check(keystoreFile.isFile) {
                    "Release keystore not found at ${keystoreFile.absolutePath} " +
                        "(mahin.release.keystorePath / MAHIN_RELEASE_KEYSTORE_PATH). " +
                        "Unset the property for unsigned CI builds."
                }
                storeFile = keystoreFile
                storePassword =
                    providers
                        .gradleProperty("mahin.release.storePassword")
                        .orElse(providers.environmentVariable("MAHIN_RELEASE_STORE_PASSWORD"))
                        .orNull
                keyAlias =
                    providers
                        .gradleProperty("mahin.release.keyAlias")
                        .orElse(providers.environmentVariable("MAHIN_RELEASE_KEY_ALIAS"))
                        .orNull
                keyPassword =
                    providers
                        .gradleProperty("mahin.release.keyPassword")
                        .orElse(providers.environmentVariable("MAHIN_RELEASE_KEY_PASSWORD"))
                        .orNull
            }
        }
    }
    buildTypes {
        getByName("release") {
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile?.exists() == true) {
                signingConfig = releaseSigning
            }
        }
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
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
    implementation(project(":core:healthconnect"))
    implementation(project(":core:assistant"))
    implementation(project(":domain:cycle"))
    implementation(project(":domain:fertility"))
    implementation(project(":domain:pregnancy"))
    implementation(project(":domain:content"))
    implementation(project(":domain:account"))
    implementation(project(":domain:subscription"))
    implementation(project(":domain:reminders"))
    implementation(project(":domain:healthconnect"))
    implementation(project(":domain:assistant"))

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
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
}

tasks.withType<Test>().configureEach {
    // Robolectric + Compose screenshot tests deadlock when forked in parallel on CI.
    maxParallelForks = 1
    timeout.set(Duration.ofMinutes(30))
    testLogging {
        events("started", "failed")
    }
    if (name == "testReleaseUnitTest") {
        filter {
            excludeTestsMatching("dev.mahin.android.demo.CalendarDemoScreenScrollTest")
            excludeTestsMatching("dev.mahin.android.cycle.HistoryScreenScrollTest")
            excludeTestsMatching("dev.mahin.android.cycle.PriorityScreensA11yTest")
            excludeTestsMatching("dev.mahin.android.cycle.M13PriorityScreensScreenshotTest")
            excludeTestsMatching("dev.mahin.android.golden.M14aFullScreenGoldenTest")
            excludeTestsMatching("dev.mahin.android.golden.M14bIconSheetGoldenTest")
            excludeTestsMatching("dev.mahin.android.golden.M14cShellGoldenTest")
            excludeTestsMatching("dev.mahin.android.golden.M15FullScreenGoldenTest")
            excludeTestsMatching("dev.mahin.android.shell.MahinShellNavigationTest")
            excludeTestsMatching("dev.mahin.android.shell.ShellModeTransitionTest")
            excludeTestsMatching("dev.mahin.android.settings.SettingsScreenContentTest")
            excludeTestsMatching("dev.mahin.android.cycle.TodayScreenContentTest")
            excludeTestsMatching("dev.mahin.android.shell.MahinBottomNavigationBarA11yTest")
            excludeTestsMatching("dev.mahin.android.pregnancy.PregnancyPlanScreenTest")
            excludeTestsMatching("dev.mahin.android.ttc.TtcInsightsScreenScrollTest")
            excludeTestsMatching("dev.mahin.android.pregnancy.PregnancyHubScreenScrollTest")
            excludeTestsMatching("dev.mahin.android.pregnancy.PregnancyStartSheetScrollTest")
            excludeTestsMatching("dev.mahin.android.assistant.AssistantSettingsScreenTest")
        }
    }
}

private val releaseApkForbiddenSubstrings =
    listOf(
        "http://10.0.2.2:8080",
        "10.0.2.2",
    )

fun org.gradle.api.Project.resolveAapt2Executable(): File {
    val buildToolsRoot = File(android.sdkDirectory, "build-tools")
    val preferred = File(buildToolsRoot, android.buildToolsVersion)
    val candidates =
        buildList {
            if (preferred.isDirectory) add(preferred)
            buildToolsRoot
                .listFiles()
                ?.filter { it.isDirectory }
                ?.sortedByDescending { it.name }
                ?.forEach { if (it != preferred) add(it) }
        }
    return candidates
        .map { File(it, "aapt2") }
        .firstOrNull { it.isFile }
        ?: error("aapt2 not found under ${buildToolsRoot.absolutePath}")
}

fun org.gradle.api.Project.dumpApkManifestText(apk: File): String {
    val aapt2 = resolveAapt2Executable()
    val process =
        ProcessBuilder(
            aapt2.absolutePath,
            "dump",
            "xmltree",
            apk.absolutePath,
            "--file",
            "AndroidManifest.xml",
        ).redirectErrorStream(true)
            .start()
    val output = process.inputStream.bufferedReader(Charsets.UTF_8).readText()
    val exit = process.waitFor()
    check(exit == 0) { "aapt2 dump failed with exit $exit for ${apk.path}" }
    return output
}

tasks.register("verifyReleaseApkNoEmulatorApiHost") {
    group = "verification"
    description =
        "Fails if the minified release APK ships dev API hosts, cleartext dev network config, or emulator base URL strings."
    dependsOn("assembleRelease", "assembleDebug")
    doLast {
        val debugDir =
            layout.buildDirectory
                .dir("outputs/apk/debug")
                .get()
                .asFile
        val debugApk =
            debugDir
                .listFiles()
                ?.firstOrNull { it.isFile && it.extension == "apk" }
        check(debugApk != null) { "Expected a debug APK under ${debugDir.path}" }
        check(dumpApkManifestText(debugApk).contains(".demo.")) {
            "Debug manifest dump smoke check failed: expected a .demo. component (verifies aapt2 manifest scan)"
        }

        val releaseDir =
            layout.buildDirectory
                .dir("outputs/apk/release")
                .get()
                .asFile
        val apk =
            releaseDir
                .listFiles()
                ?.firstOrNull { it.isFile && it.extension == "apk" }
        check(apk != null) { "Expected a release APK under ${releaseDir.path}" }
        check(!dumpApkManifestText(apk).contains(".demo.")) {
            "Release manifest contains debug demo package component"
        }
        ZipFile(apk).use { zip ->
            zip
                .entries()
                .asSequence()
                .filter { entry: ZipEntry -> !entry.isDirectory }
                .forEach { entry: ZipEntry ->
                    val text =
                        zip
                            .getInputStream(entry)
                            .bufferedReader(Charsets.ISO_8859_1)
                            .readText()
                    val needles =
                        if (
                            entry.name.endsWith(".dex") ||
                            entry.name.endsWith(".jar") ||
                            entry.name.endsWith(".kotlin_module")
                        ) {
                            listOf("http://10.0.2.2:8080")
                        } else {
                            releaseApkForbiddenSubstrings
                        }
                    needles.forEach { needle ->
                        check(!text.contains(needle)) {
                            "Release artifact ${entry.name} contains forbidden release string: $needle"
                        }
                    }
                    if (entry.name.endsWith(".dex")) {
                        check(!text.contains("dev/mahin/android/demo/")) {
                            "Release artifact ${entry.name} contains debug demo package classes"
                        }
                    }
                }
        }
    }
}
