package dev.mahin.core.testing

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.rules.ExternalResource

/**
 * Clears the activity [androidx.lifecycle.ViewModelStore] after each test so compose / golden
 * tests do not retain ViewModels across cases.
 */
class ViewModelStoreClearingRule<A : ComponentActivity>(
    private val composeRule: AndroidComposeTestRule<ActivityScenarioRule<A>, A>,
) : ExternalResource() {
    override fun after() {
        runCatching {
            composeRule.activity.viewModelStore.clear()
        }
    }
}
