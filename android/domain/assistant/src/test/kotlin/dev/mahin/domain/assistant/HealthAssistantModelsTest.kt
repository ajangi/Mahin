package dev.mahin.domain.assistant

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HealthAssistantModelsTest {
    @Test
    fun consentScopesDefaultOptOut() {
        val scopes = AssistantConsentScopes()
        assertThat(scopes.shareCycleSummary).isFalse()
        assertThat(scopes.shareSymptomTags).isFalse()
    }
}
