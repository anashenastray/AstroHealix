package com.example.astrohealix.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.data.AstroRepository
import com.example.astrohealix.model.CatalogData
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.ui.components.DockTab
import com.example.astrohealix.ui.components.ExamGuideSheet
import com.example.astrohealix.ui.components.FloatingDockBar
import com.example.astrohealix.ui.components.QuickLogDialog
import com.example.astrohealix.ui.components.SimulatorDialog
import com.example.astrohealix.ui.screens.AiCoachScreen
import com.example.astrohealix.ui.screens.AlertsScreen
import com.example.astrohealix.ui.screens.BodyMapScreen
import com.example.astrohealix.ui.screens.DashboardScreen
import com.example.astrohealix.ui.screens.LogsScreen
import com.example.astrohealix.ui.screens.MoreScreen
import com.example.astrohealix.ui.screens.ProfileScreen
import com.example.astrohealix.ui.screens.ProtocolsScreen
import com.example.astrohealix.ui.screens.ReportsScreen
import com.example.astrohealix.ui.screens.SecondaryTopBar
import com.example.astrohealix.ui.screens.TrendsScreen
import com.example.astrohealix.ui.theme.AstroHealixTheme
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

enum class SecondaryDestination {
    ALERTS,
    REPORTS,
    PROFILE,
    LOGS,
    BODY_MAP,
    SIM_CONTROLS
}

@Composable
fun AstroHealixApp(
    repository: AstroRepository = remember { AstroRepository() }
) {
    val uiState by repository.uiState.collectAsState()

    var activeTab by remember { mutableStateOf(DockTab.DASHBOARD) }
    var secondaryDestination by remember { mutableStateOf<SecondaryDestination?>(null) }

    var selectedVitalForGuide by remember { mutableStateOf<VitalId?>(null) }
    var selectedVitalForTrends by remember { mutableStateOf(VitalId.HEART_RATE) }
    var showQuickLogDialog by remember { mutableStateOf(false) }
    var quickLogInitialVital by remember { mutableStateOf(VitalId.HYDRATION_ML) }
    var showSimulatorDialog by remember { mutableStateOf(false) }

    AstroHealixTheme(themeMode = uiState.themeMode) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(BgPrimary)
                .safeDrawingPadding(),
            containerColor = BgPrimary,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(BgPrimary)
            ) {
                // Main Content Screen Area
                AnimatedContent(
                    targetState = secondaryDestination to activeTab,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(200, easing = LinearOutSlowInEasing)) +
                            scaleIn(initialScale = 0.985f, animationSpec = tween(200, easing = LinearOutSlowInEasing)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(160, easing = FastOutLinearInEasing))
                            )
                    },
                    label = "screenTransition",
                    modifier = Modifier.fillMaxSize()
                ) { (targetSec, targetTab) ->
                    when (targetSec) {
                        SecondaryDestination.ALERTS -> AlertsScreen(
                            state = uiState,
                            onAcknowledge = { repository.acknowledgeAlert(it) },
                            onEscalate = { repository.escalateAlert(it) },
                            onBack = { secondaryDestination = null }
                        )

                        SecondaryDestination.REPORTS -> ReportsScreen(
                            state = uiState,
                            onGenerateReport = { periodHours -> repository.exportReportObject(periodHours) },
                            onExportCsv = { repository.exportReportCsv() },
                            onBack = { secondaryDestination = null }
                        )

                        SecondaryDestination.PROFILE -> ProfileScreen(
                            state = uiState,
                            onUpdateProfile = { name, missionName, missionDay ->
                                repository.updateProfile(name, missionName, missionDay)
                            },
                            onUpdateBaseline = { vitalId, updated ->
                                repository.updateBaseline(vitalId, updated)
                            },
                            onCompleteOnboarding = { repository.completeFirstTimeSetup() },
                            onSelectThemeMode = { repository.setThemeMode(it) },
                            onBack = { secondaryDestination = null }
                        )

                        SecondaryDestination.LOGS -> Column(modifier = Modifier.fillMaxSize()) {
                            SecondaryTopBar(
                                title = "LOGS & SYNC QUEUE",
                                onBack = { secondaryDestination = null }
                            )
                            LogsScreen(
                                state = uiState,
                                onForceSync = { repository.reconcileOfflineQueue() },
                                onExportCsv = { repository.exportReportCsv() },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        SecondaryDestination.BODY_MAP -> Column(modifier = Modifier.fillMaxSize()) {
                            SecondaryTopBar(
                                title = "BODY SCAN HUD",
                                onBack = { secondaryDestination = null }
                            )
                            BodyMapScreen(
                                onInspectVital = { vitalId ->
                                    selectedVitalForTrends = vitalId
                                    activeTab = DockTab.TRENDS
                                    secondaryDestination = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        SecondaryDestination.SIM_CONTROLS, null -> when (targetTab) {
                            DockTab.DASHBOARD -> DashboardScreen(
                                state = uiState,
                                onSelectVital = { vitalId ->
                                    selectedVitalForTrends = vitalId
                                    activeTab = DockTab.TRENDS
                                },
                                onQuickLog = { vitalId ->
                                    quickLogInitialVital = vitalId
                                    showQuickLogDialog = true
                                },
                                onAcknowledgeAlert = { repository.acknowledgeAlert(it) },
                                onOpenSimControls = { showSimulatorDialog = true },
                                onToggleTask = { repository.toggleRoutineTask(it) }
                            )

                            DockTab.TRENDS -> TrendsScreen(
                                state = uiState,
                                initialVitalId = selectedVitalForTrends,
                                getHistory = { repository.getTelemetryHistory(it) }
                            )

                            DockTab.BODY_SCAN -> BodyMapScreen(
                                onInspectVital = { vitalId ->
                                    selectedVitalForTrends = vitalId
                                    activeTab = DockTab.TRENDS
                                }
                            )

                            DockTab.PROTOCOLS -> ProtocolsScreen(
                                state = uiState,
                                onTransmitToFlightSurgeon = {
                                    repository.reconcileOfflineQueue()
                                }
                            )

                            DockTab.AI_COACH -> AiCoachScreen(
                                state = uiState
                            )
                        }
                    }
                }

                // First-Time Setup / Onboarding Check Banner if pending
                if (!uiState.hasCompletedOnboarding && secondaryDestination == null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 76.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgSurface)
                            .border(1.dp, BorderActive, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Complete First-Time Setup",
                                    color = TextPrimary,
                                    fontSize = 13.5.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Text(
                                    text = "Verify baseline thresholds & crew access.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.size(8.dp))
                            Button(
                                onClick = { repository.completeFirstTimeSetup() },
                                colors = ButtonDefaults.buttonColors(containerColor = BorderActive),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Acknowledge", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Sleek Floating Dock Navigation Bar taken directly from Aura Health
                FloatingDockBar(
                    current = activeTab,
                    onSelect = { tab ->
                        activeTab = tab
                        secondaryDestination = null
                    },
                    onQuickLog = {
                        quickLogInitialVital = VitalId.HEART_RATE
                        showQuickLogDialog = true
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                // Quick Log Modal Dialog
                if (showQuickLogDialog) {
                    QuickLogDialog(
                        initialVitalId = quickLogInitialVital,
                        onDismiss = { showQuickLogDialog = false },
                        onSave = { vitalId, value, secValue, isAdditive, note ->
                            repository.logManualVital(
                                vitalId = vitalId,
                                value = value,
                                secondaryValue = secValue,
                                isAdditive = isAdditive,
                                note = note
                            )
                            showQuickLogDialog = false
                        }
                    )
                }

                // Scenario Simulator Dialog
                if (showSimulatorDialog) {
                    SimulatorDialog(
                        activeScenario = uiState.scenario,
                        isOffline = uiState.isOffline,
                        onSelectScenario = { scenario ->
                            repository.setScenario(scenario)
                        },
                        onToggleOffline = {
                            repository.toggleOfflineLink()
                        },
                        onDismiss = { showSimulatorDialog = false }
                    )
                }

                // Exam Guide Bottom Sheet
                selectedVitalForGuide?.let { vid ->
                    val snapshot = uiState.vitals.find { it.id == vid }
                    ExamGuideSheet(
                        vitalId = vid,
                        snapshot = snapshot,
                        onDismiss = { selectedVitalForGuide = null },
                        onLogNow = {
                            selectedVitalForGuide = null
                            quickLogInitialVital = vid
                            showQuickLogDialog = true
                        },
                        onViewTrends = {
                            selectedVitalForGuide = null
                            selectedVitalForTrends = vid
                            activeTab = DockTab.TRENDS
                            secondaryDestination = null
                        }
                    )
                }
            }
        }
    }
}
