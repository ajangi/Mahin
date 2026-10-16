package dev.mahin.android.pregnancy

import com.google.common.truth.Truth.assertThat
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import org.junit.Test

class M18bNoHardcodedBpThresholdsTest {
    @Test
    fun productionPregnancySources_doNotEmbedBpThresholdLiterals() {
        val androidRoot = Paths.get(System.getProperty("user.dir"))
        val roots =
            listOf(
                androidRoot.resolve("../domain/pregnancy/src/main/kotlin"),
                androidRoot.resolve("src/main/kotlin/dev/mahin/android/pregnancy"),
            )
        val forbidden = Regex("""\b(140|90|160|110)\b""")
        val offenders = mutableListOf<String>()
        roots.forEach { root ->
            if (!Files.exists(root)) return@forEach
            Files.walk(root).filter { it.isRegularFile() && it.toString().endsWith(".kt") }.forEach { file ->
                val text = file.toFile().readText()
                if (file.fileName.toString() == "PregnancyBpFlagEvaluator.kt") return@forEach
                if (forbidden.containsMatchIn(text)) {
                    offenders.add(root.relativize(file).toString())
                }
            }
        }
        assertThat(offenders).isEmpty()
    }
}
