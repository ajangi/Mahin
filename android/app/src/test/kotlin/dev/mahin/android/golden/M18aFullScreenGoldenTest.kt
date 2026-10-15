package dev.mahin.android.golden

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.mahin.android.pregnancy.PregnancyAppointmentsActions
import dev.mahin.android.pregnancy.PregnancyAppointmentsFormState
import dev.mahin.android.pregnancy.PregnancyChecklistScreenContent
import dev.mahin.android.pregnancy.PregnancyPlanScreenContent
import dev.mahin.android.pregnancy.PregnancyToolsScreenContent
import dev.mahin.android.pregnancy.PregnancyWeekByWeekScreenContent
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.media.illustration.BundledMahinIllustrationCatalog
import dev.mahin.core.model.PregnancyAppointmentType
import dev.mahin.core.testing.ViewModelStoreClearingRule
import dev.mahin.core.testing.roborazzi.captureMahinFullScreenGolden
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "fa-rIR-w411dp-h891dp-xxhdpi")
class M18aFullScreenGoldenTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Test fun pregnancy_week_view_light_scale10() = captureWeek(false, 1f)

    @Test fun pregnancy_week_view_dark_scale10() = captureWeek(true, 1f)

    @Test fun pregnancy_week_view_light_scale13() = captureWeek(false, 1.3f)

    @Test fun pregnancy_week_view_dark_scale13() = captureWeek(true, 1.3f)

    @Test fun pregnancy_plan_timeline_light_scale10() = capturePlan(false, 1f)

    @Test fun pregnancy_plan_timeline_light_scale13() = capturePlan(false, 1.3f)

    @Test fun pregnancy_plan_timeline_dark_scale10() = capturePlan(true, 1f)

    @Test fun pregnancy_plan_timeline_dark_scale13() = capturePlan(true, 1.3f)

    @Test fun pregnancy_tools_light_scale10() = captureTools(false, 1f)

    @Test fun pregnancy_tools_light_scale13() = captureTools(false, 1.3f)

    @Test fun pregnancy_tools_dark_scale10() = captureTools(true, 1f)

    @Test fun pregnancy_tools_dark_scale13() = captureTools(true, 1.3f)

    @Test fun pregnancy_checklist_light_scale10() = captureChecklist(false, 1f)

    @Test fun pregnancy_checklist_light_scale13() = captureChecklist(false, 1.3f)

    @Test fun pregnancy_checklist_dark_scale10() = captureChecklist(true, 1f)

    @Test fun pregnancy_checklist_dark_scale13() = captureChecklist(true, 1.3f)

    private fun captureWeek(
        dark: Boolean,
        scale: Float,
    ) {
        androidComposeRule.captureMahinFullScreenGolden(dark, scale) {
            PregnancyWeekByWeekScreenContent(
                state = M18aGoldenFixtures.weekByWeek(),
                illustrationSource = BundledMahinIllustrationCatalog,
                onWeekSelected = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun capturePlan(
        dark: Boolean,
        scale: Float,
    ) {
        androidComposeRule.captureMahinFullScreenGolden(dark, scale) {
            PregnancyPlanScreenContent(
                hasActivePregnancy = true,
                isLoading = false,
                timeline = M18aGoldenFixtures.planTimeline(),
                onAddCarePlanAsAppointment = {},
                formState =
                    PregnancyAppointmentsFormState(
                        appointments = emptyList(),
                        newAppointmentTitle = "",
                        newAppointmentType = PregnancyAppointmentType.CLINICIAN_VISIT,
                        newAppointmentJalali = PersianCivilDateConverter.toJalali(LocalDate.of(2025, 6, 1)),
                    ),
                formActions =
                    PregnancyAppointmentsActions(
                        onNewAppointmentTitleChange = {},
                        onNewAppointmentTypeSelected = {},
                        onNewAppointmentDateSelected = {},
                        onAddAppointment = {},
                    ),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun captureTools(
        dark: Boolean,
        scale: Float,
    ) {
        androidComposeRule.captureMahinFullScreenGolden(dark, scale) {
            PregnancyToolsScreenContent(
                referenceDate = M18aGoldenFixtures.toolsReferenceDate(),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun captureChecklist(
        dark: Boolean,
        scale: Float,
    ) {
        androidComposeRule.captureMahinFullScreenGolden(dark, scale) {
            PregnancyChecklistScreenContent(
                state = M18aGoldenFixtures.checklist(),
                onToggle = { _, _ -> },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
