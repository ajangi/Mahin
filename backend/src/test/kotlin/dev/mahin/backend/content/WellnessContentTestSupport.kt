package dev.mahin.backend.content

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.cms.CmsAuthService
import dev.mahin.backend.cms.CmsRole
import java.util.UUID
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

object WellnessContentTestSupport {
    data class StaffTokens(
        val editorToken: String,
        val medicalToken: String,
        val medicalDisplayName: String,
    )

    fun seedStaff(
        mockMvc: MockMvc,
        objectMapper: ObjectMapper,
        cmsAuthService: CmsAuthService,
        medicalDisplayName: String = "دکتر نمونه",
    ): StaffTokens {
        val suffix = UUID.randomUUID().toString().take(8)
        cmsAuthService.createStaff(
            email = "editor-$suffix@mahin.test",
            password = "password-12-chars",
            displayName = "Editor",
            roles = setOf(CmsRole.EDITOR),
        )
        cmsAuthService.createStaff(
            email = "medical-$suffix@mahin.test",
            password = "password-12-chars",
            displayName = medicalDisplayName,
            roles = setOf(CmsRole.MEDICAL_REVIEWER),
        )
        return StaffTokens(
            editorToken = login(mockMvc, objectMapper, "editor-$suffix@mahin.test"),
            medicalToken = login(mockMvc, objectMapper, "medical-$suffix@mahin.test"),
            medicalDisplayName = medicalDisplayName,
        )
    }

    fun login(
        mockMvc: MockMvc,
        objectMapper: ObjectMapper,
        email: String,
    ): String {
        val body = mapOf("email" to email, "password" to "password-12-chars")
        val response =
            mockMvc
                .post("/v1/admin/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(body)
                }.andExpect { status { isOk() } }
                .andReturn()
                .response.contentAsString
        return objectMapper.readTree(response).get("accessToken").asText()
    }

    fun createSource(
        mockMvc: MockMvc,
        objectMapper: ObjectMapper,
        editorToken: String,
    ): String {
        val body =
            mapOf(
                "citationKey" to "fixture-${UUID.randomUUID()}",
                "title" to "منبع آزمون",
            )
        val response =
            mockMvc
                .post("/v1/admin/content/sources") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer $editorToken")
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(body)
                }.andExpect { status { isOk() } }
                .andReturn()
                .response.contentAsString
        return objectMapper.readTree(response).get("id").asText()
    }

    data class WorkflowCall(
        val mockMvc: MockMvc,
        val objectMapper: ObjectMapper,
        val versionId: String,
        val action: String,
        val token: String,
        val extra: Map<String, String> = emptyMap(),
    )

    fun workflow(call: WorkflowCall) {
        call.mockMvc
            .post("/v1/admin/content/versions/${call.versionId}/workflow/${call.action}") {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${call.token}")
                contentType = MediaType.APPLICATION_JSON
                content = call.objectMapper.writeValueAsString(call.extra)
            }.andExpect { status { isOk() } }
    }

    fun publishWellness(
        mockMvc: MockMvc,
        objectMapper: ObjectMapper,
        versionId: String,
        editorToken: String,
        medicalToken: String,
    ) {
        workflow(WorkflowCall(mockMvc, objectMapper, versionId, "submit_medical_review", editorToken))
        workflow(WorkflowCall(mockMvc, objectMapper, versionId, "approve_medical", medicalToken))
        workflow(WorkflowCall(mockMvc, objectMapper, versionId, "approve_editorial", editorToken))
        workflow(WorkflowCall(mockMvc, objectMapper, versionId, "publish", editorToken))
    }
}
