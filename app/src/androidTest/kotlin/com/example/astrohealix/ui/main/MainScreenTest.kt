package com.example.astrohealix.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.astrohealix.model.NON_DIAGNOSTIC_DISCLAIMER
import com.example.astrohealix.ui.AstroHealixApp
import com.example.astrohealix.ui.theme.AstroHealixTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [AstroHealixApp] VITA-SYS navigation and core screen flows. */
class MainScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        composeTestRule.setContent {
            AstroHealixTheme {
                AstroHealixApp()
            }
        }
    }

    @Test
    fun dashboard_rendersMissionHeaderDemoDataAndBottomTabs() {
        composeTestRule.onNodeWithText("Cmdr. J. Vance").assertIsDisplayed()
        composeTestRule.onNodeWithText("DAY 47").assertIsDisplayed()
        composeTestRule.onNodeWithText("DEMO DATA").assertIsDisplayed()
        composeTestRule.onNodeWithText("TELEMETRY").assertIsDisplayed()
        composeTestRule.onNodeWithText("Emergency Symptom Triage").assertIsDisplayed()

        composeTestRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vitals").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wellbeing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Trends").assertIsDisplayed()
        composeTestRule.onNodeWithText("More").assertIsDisplayed()

        composeTestRule.onNode(hasScrollAction())
            .performScrollToNode(hasText(NON_DIAGNOSTIC_DISCLAIMER))
        composeTestRule.onNodeWithText(NON_DIAGNOSTIC_DISCLAIMER).assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_switchesBetweenPrimaryTabs() {
        // Vitals Tab
        composeTestRule.onNodeWithText("Vitals").performClick()
        composeTestRule.onNodeWithText("VITALS BY CATEGORY").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cardiovascular").assertIsDisplayed()

        // Wellbeing Tab
        composeTestRule.onNodeWithText("Wellbeing").performClick()
        composeTestRule.onNodeWithText("PSYCHOLOGICAL WELLBEING").assertIsDisplayed()
        composeTestRule.onNodeWithText("This information is treated as private and sensitive.").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("[SELF-REPORTED]").onFirst().assertIsDisplayed()
        composeTestRule.onNode(hasScrollAction())
            .performScrollToNode(hasText("Request Confidential Support"))
        composeTestRule.onNodeWithText("Request Confidential Support").assertIsDisplayed()

        // Trends Tab
        composeTestRule.onNodeWithText("Trends").performClick()
        composeTestRule.onNodeWithText("TELEMETRY TRENDS").assertIsDisplayed()
        composeTestRule.onNode(
            androidx.compose.ui.test.SemanticsMatcher.keyIsDefined(
                androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange
            )
        ).performScrollToNode(hasText("RULE & QUALITY VALIDATION"))
        composeTestRule.onNodeWithText("RULE & QUALITY VALIDATION").assertIsDisplayed()

        // More Tab
        composeTestRule.onNodeWithText("More").performClick()
        composeTestRule.onNodeWithText("MORE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alerts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reports").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile & Baselines").assertIsDisplayed()
        composeTestRule.onNodeWithText("Logs").assertIsDisplayed()
        composeTestRule.onNodeWithText("Body Map").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sim Controls").assertIsDisplayed()

        // Return to Dashboard Tab
        composeTestRule.onNodeWithText("Dashboard").performClick()
        composeTestRule.onNodeWithText("TELEMETRY").assertIsDisplayed()
    }

    @Test
    fun moreTab_opensProfileReportsAndSimulatorDialog() {
        composeTestRule.onNodeWithText("More").performClick()

        // Profile & Baselines
        composeTestRule.onNodeWithText("Profile & Baselines").performClick()
        composeTestRule.onNodeWithText("PROFILE & BASELINES").assertIsDisplayed()
        composeTestRule.onNodeWithText("#A-8842").assertIsDisplayed()
        composeTestRule.onNode(
            androidx.compose.ui.test.SemanticsMatcher.keyIsDefined(
                androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange
            )
        ).performScrollToNode(hasText("PERSONAL BASELINE EDITOR"))
        composeTestRule.onNodeWithText("PERSONAL BASELINE EDITOR").assertIsDisplayed()
        composeTestRule.onNodeWithText("Back").performClick()

        // Reports
        composeTestRule.onNodeWithText("Reports").performClick()
        composeTestRule.onNodeWithText("HEALTH REPORTS").assertIsDisplayed()
        composeTestRule.onNodeWithText("24h").assertIsDisplayed()
        composeTestRule.onNodeWithText("7d").assertIsDisplayed()
        composeTestRule.onNodeWithText("30d").assertIsDisplayed()
        composeTestRule.onNodeWithText("Generate Health Report").assertIsDisplayed()
        composeTestRule.onNodeWithText("Export CSV").assertIsDisplayed()
        composeTestRule.onNodeWithText("Back").performClick()

        // Sim Controls -> verifies VITA-SYS SIM & [DEMO DATA]
        composeTestRule.onNodeWithText("Sim Controls").performClick()
        composeTestRule.onNodeWithText("VITA-SYS SIM").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("[DEMO DATA]").assertCountEquals(1)
        composeTestRule.onNodeWithText("Close Controls").performClick()
    }
}
