import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            configureQuality()
            configureJvm17()
            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = mahinJavaVersion
                targetCompatibility = mahinJavaVersion
            }
        }
    }
}
