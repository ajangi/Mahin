package dev.mahin.android.pregnancy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.PregnancyAppointmentType
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR")
class PregnancyPlanScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun planTab_listsAppointmentAfterAdd() {
        composeRule.setContent {
            val appointments = mutableStateListOf<PregnancyAppointmentListItem>()
            var title by mutableStateOf("")
            MahinTheme {
                PregnancyPlanScreenContent(
                    hasActivePregnancy = true,
                    isLoading = false,
                    formState =
                        PregnancyAppointmentsFormState(
                            appointments = appointments,
                            newAppointmentTitle = title,
                            newAppointmentType = PregnancyAppointmentType.CLINICIAN_VISIT,
                            newAppointmentJalali = PersianCivilDateConverter.toJalali(LocalDate.of(2025, 6, 1)),
                        ),
                    formActions =
                        PregnancyAppointmentsActions(
                            onNewAppointmentTitleChange = { title = it },
                            onNewAppointmentTypeSelected = {},
                            onNewAppointmentDateSelected = {},
                            onAddAppointment = {
                                appointments.add(
                                    PregnancyAppointmentListItem(
                                        title = title.trim(),
                                        whenLabel = "۱۴۰۴/۰۳/۱۱",
                                    ),
                                )
                                title = ""
                            },
                        ),
                )
            }
        }
        composeRule.onNodeWithTag("pregnancy_plan_list").performScrollToNode(hasText("قرارها و آزمایش‌ها"))
        composeRule.onNodeWithText("عنوان").performTextInput("ویزیت ماهانه")
        composeRule.onNodeWithText("ذخیرهٔ قرار").performClick()
        composeRule.onNodeWithText("ویزیت ماهانه", substring = true).assertIsDisplayed()
    }

    @Test
    fun planTab_withoutActivePregnancy_showsEmptyState() {
        composeRule.setContent {
            MahinTheme {
                PregnancyPlanScreenContent(
                    hasActivePregnancy = false,
                    isLoading = false,
                    formState =
                        PregnancyAppointmentsFormState(
                            appointments = emptyList(),
                            newAppointmentTitle = "",
                            newAppointmentType = PregnancyAppointmentType.CLINICIAN_VISIT,
                            newAppointmentJalali = PersianCivilDateConverter.toJalali(LocalDate.now()),
                        ),
                    formActions =
                        PregnancyAppointmentsActions(
                            onNewAppointmentTitleChange = {},
                            onNewAppointmentTypeSelected = {},
                            onNewAppointmentDateSelected = {},
                            onAddAppointment = {},
                        ),
                )
            }
        }
        composeRule.onNodeWithText("برنامهٔ ویزیت‌ها").assertIsDisplayed()
        composeRule.onNodeWithText("ذخیرهٔ قرار").assertDoesNotExist()
    }
}
