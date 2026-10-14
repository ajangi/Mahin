package dev.mahin.android.golden

import dev.mahin.android.learn.ArticleUiState
import dev.mahin.android.learn.GuidedPlayerUiState
import dev.mahin.android.learn.LearnUiState
import dev.mahin.android.learn.ProgramDetailUiState
import dev.mahin.core.content.ContentArticleDto
import dev.mahin.core.content.ContentProgramDto
import dev.mahin.core.content.ProgramSessionDto
import dev.mahin.core.content.ProgramStepDto
import dev.mahin.domain.content.GuidedPlayerSnapshot

object M17bGoldenFixtures {
    fun learnMagazine(): LearnUiState = M14aGoldenFixtures.learnPopulated()

    fun article(): ArticleUiState =
        ArticleUiState(
            loading = false,
            article =
                ContentArticleDto(
                    id = "article-1",
                    slug = "synthetic-article",
                    locale = "fa-IR",
                    title = "[نمونه] مقالهٔ آموزشی",
                    summary = "خلاصهٔ آزمایشی — محتوای واقعی نیست.",
                    body = "متن نمونه برای تست طلایی.",
                    status = "published",
                    medicalRiskLevel = "general_education",
                    contentVersion = 1,
                    sourceReferences = listOf("منبع نمونه"),
                    clinicalReviewer = "بازبین نمونه",
                ),
        )

    fun programDetail(): ProgramDetailUiState =
        ProgramDetailUiState(
            loading = false,
            completionCount = 2,
            program =
                ContentProgramDto(
                    id = "program-1",
                    slug = "synthetic-kegel",
                    locale = "fa-IR",
                    title = "[نمونه] تمرین کف لگن",
                    summary = "داده نمایشی.",
                    programType = "pelvic_floor",
                    medicalRiskLevel = "general_education",
                    contentVersion = 1,
                    safetyNotice = "در صورت درد، تمرین را متوقف کنید.",
                    contraindications = "فقط برای آموزش — جایگزین درمان نیست.",
                    sessions =
                        listOf(
                            ProgramSessionDto(
                                id = "session-1",
                                title = "جلسهٔ نمونه",
                                order = 1,
                                steps =
                                    listOf(
                                        ProgramStepDto(instruction = "انقباض", durationSeconds = 5),
                                        ProgramStepDto(instruction = "استراحت", durationSeconds = 5),
                                    ),
                            ),
                        ),
                ),
        )

    fun player(): GuidedPlayerUiState =
        GuidedPlayerUiState(
            sessionTitle = "جلسهٔ نمونه",
            stepInstruction = "انقباض",
            stepIndex = 0,
            stepCount = 2,
            progress = 0.35f,
            paused = false,
            completed = false,
            snapshot = GuidedPlayerSnapshot(0, 1_750, false, false),
        )
}
