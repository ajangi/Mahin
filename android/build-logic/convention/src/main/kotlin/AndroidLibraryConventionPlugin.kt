import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.android")
            configureQuality()
            configureJvm17()
            extensions.configure<LibraryExtension> {
                compileSdk = MAHIN_COMPILE_SDK
                defaultConfig {
                    minSdk = MAHIN_MIN_SDK
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    consumerProguardFiles("consumer-rules.pro")
                }
                compileOptions {
                    sourceCompatibility = mahinJavaVersion
                    targetCompatibility = mahinJavaVersion
                    isCoreLibraryDesugaringEnabled = true
                }
                lint {
                    abortOnError = true
                    warningsAsErrors = false
                    disable += setOf("GradleDependency", "AndroidGradlePluginVersion")
                }
                testOptions {
                    unitTests.isIncludeAndroidResources = true
                    unitTests.isReturnDefaultValues = true
                }
            }
            dependencies {
                add("coreLibraryDesugaring", libs.findLibrary("desugar-jdk-libs").get())
            }
        }
    }
}
