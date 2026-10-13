package dev.mahin.backend.content

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException

class ContentTypePayloadValidatorTest {
    private val mapper = ObjectMapper()

    @Test
    fun acceptsPersianPlaceholderDailyTip() {
        val json =
            mapper.writeValueAsString(
                DailyTipPayload(
                    placeholder = true,
                    targeting =
                        DailyTipTargeting(
                            modes = listOf("cycle"),
                            cyclePhases = listOf("luteal"),
                        ),
                ),
            )
        assertDoesNotThrow {
            ContentTypePayloadValidator.validateForSave(ContentTypes.DAILY_TIP, json, mapper)
        }
    }

    @Test
    fun rejectsProgramWithoutSessions() {
        val json =
            mapper.writeValueAsString(
                ProgramPayload(
                    placeholder = true,
                    programType = "breathing",
                    sessions = emptyList(),
                ),
            )
        assertThrows(ResponseStatusException::class.java) {
            ContentTypePayloadValidator.validateForSave(ContentTypes.PROGRAM, json, mapper)
        }
    }

    @Test
    fun requiresSafetyFieldsWhenPublishingNonPlaceholderProgram() {
        val json =
            mapper.writeValueAsString(
                ProgramPayload(
                    placeholder = false,
                    programType = "pelvic_floor",
                    sessions =
                        listOf(
                            ProgramSessionPayload(
                                id = "s1",
                                title = "جلسهٔ نمونه",
                                order = 1,
                                steps =
                                    listOf(
                                        ProgramStepPayload(instruction = "نمونه — غیرپزشکی"),
                                    ),
                            ),
                        ),
                ),
            )
        assertThrows(ResponseStatusException::class.java) {
            ContentTypePayloadValidator.validateForPublish(ContentTypes.PROGRAM, json, mapper)
        }
    }
}
