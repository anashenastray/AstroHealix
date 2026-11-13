package com.example.astrohealix.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.astrohealix.data.AstroRepository
import com.example.astrohealix.model.CatalogData
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.ui.components.ExamGuideSheet
import com.example.astrohealix.ui.components.QuickLogDialog
import com.example.astrohealix.ui.components.SimulatorDialog
import com.example.astrohealix.ui.screens.AlertsScreen
import com.example.astrohealix.ui.screens.BodyMapScreen
import com.example.astrohealix.ui.screens.DashboardScreen
import com.example.astrohealix.ui.screens.LogsScreen
import com.example.astrohealix.ui.screens.MoreScreen
import com.example.astrohealix.ui.screens.ProfileScreen
import com.example.astrohealix.ui.screens.ReportsScreen
import com.example.astrohealix.ui.screens.SecondaryTopBar
import com.example.astrohealix.ui.screens.TrendsScreen
import com.example.astrohealix.ui.screens.VitalsScreen
import com.example.astrohealix.ui.screens.WellbeingScreen
import com.example.astrohealix.ui.theme.AstroHealixTheme
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.CriticalSurfaceGlow
import com.example.astrohealix.ui.theme.OnColoredButton
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

enum class BottomTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    VITALS("Vitals", Icons.Default.MonitorHeart),
    WELLBEING("Wellbeing", Icons.Default.SelfImprovement),
    TRENDS("Trends", Icons.AutoMirrored.Filled.ShowChart),
    MORE("More", Icons.Default.MoreHoriz)
}

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

    var activeTab by remember { mutableStateOf(BottomTab.DASHBOARD) }
    var secondaryDestination by remember { mutableStateOf<SecondaryDestination?>(null) }

    var selectedVitalForGuide by remember { mutableStateOf<VitalId?>(null) }
    var selectedVitalForTrends by remember { mutableStateOf(VitalId.HEART_RATE) }
    var showQuickLogDialog by remember { mutableStateOf(false) }
    var quickLogInitialVital by remember { mutableStateOf(VitalId.HYDRATION_ML) }
    var showSimulatorDialog by remember { mutableStateOf(false) }
    var showEmergencyTriageDialog by remember { mutableStateOf(false) }

    AstroHealixTheme(themeMode = uiState.themeMode) {
        val borderColor = BorderSubtle

        Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .safeDrawingPadding(),
        containerColor = BgPrimary,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = BgSurface,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier
                    .heightIn(min = 64.dp)
                    .drawBehind {
                        drawLine(
                            color = borderColor,
                            start = Offset.Zero,
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                BottomTab.entries.forEach { tab ->
                    val isSelected = activeTab == tab && secondaryDestination == null
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            activeTab = tab
                            secondaryDestination = null
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BorderActive,
                            selectedTextColor = BorderActive,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgPrimary)
        ) {
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
                            title = "BODY MAP",
                            onBack = { secondaryDestination = null }
                        )
                        BodyMapScreen(
                            state = uiState,
                            onOpenExamGuide = { selectedVitalForGuide = it },
                            onLogMetric = { vitalId ->
                                quickLogInitialVital = vitalId
                                showQuickLogDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    SecondaryDestination.SIM_CONTROLS, null -> when (targetTab) {
                        BottomTab.DASHBOARD -> DashboardScreen(
                            state = uiState,
                            onTapVital = { selectedVitalForGuide = it },
                            onAcknowledgeAlert = { repository.acknowledgeAlert(it) },
                            onEscalateAlert = { repository.escalateAlert(it) },
                            onToggleRoutine = { repository.toggleRoutineTask(it) },
                            onOpenQuickLog = { vitalId ->
                                quickLogInitialVital = vitalId
                                showQuickLogDialog = true
                            },
                            onOpenEmergencyTriage = { showEmergencyTriageDialog = true },
                            onOpenAlerts = {
                                activeTab = BottomTab.MORE
                                secondaryDestination = SecondaryDestination.ALERTS
                            }
                        )

                        BottomTab.VITALS -> VitalsScreen(
                            state = uiState,
                            onTapVital = { selectedVitalForGuide = it },
                            onViewTrends = { vitalId ->
                                selectedVitalForTrends = vitalId
                                activeTab = BottomTab.TRENDS
                                secondaryDestination = null
                            },
                            onLogEntry = { vitalId ->
                                quickLogInitialVital = vitalId
                                showQuickLogDialog = true
                            }
                        )

                        BottomTab.WELLBEING -> WellbeingScreen(
                            state = uiState,
                            onLogWellbeingCheckIn = { mood, stress, fatigue, notes ->
                                repository.logWellbeingCheckIn(mood, stress, fatigue, notes)
                            },
                            onRequestConfidentialSupport = { category, note ->
                                repository.requestConfidentialSupport(category, note)
                            },
                            onTapVital = { selectedVitalForGuide = it }
                        )

                        BottomTab.TRENDS -> TrendsScreen(
                            state = uiState,
                            initialVitalId = selectedVitalForTrends,
                            getHistory = { repository.getTelemetryHistory(it) }
                        )

                        BottomTab.MORE -> MoreScreen(
                            unacknowledgedAlertCount = uiState.alerts.count { !it.isAcknowledged },
                            pendingSyncCount = uiState.pendingSyncQueue.size,
                            scenarioTitle = uiState.scenario.title,
                            reportedEmergencyCount = uiState.emergencyReportedSymptoms.size,
                            themeMode = uiState.themeMode,
                            onSelectThemeMode = { repository.setThemeMode(it) },
                            onNavigate = { dest ->
                                if (dest == SecondaryDestination.SIM_CONTROLS) {
                                    showSimulatorDialog = true
                                } else {
                                    secondaryDestination = dest
                                }
                            },
                            onOpenEmergencyTriage = { showEmergencyTriageDialog = true }
                        )
                    }
                }
            }

            // First-Time Setup / Onboarding Check Banner if pending
            if (!uiState.hasCompletedOnboarding) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderActive, RoundedCornerShape(8.dp))
                        .padding(16.dp)
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Verify baseline thresholds & crew access.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(
                            onClick = { repository.completeFirstTimeSetup() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BorderActive,
                                contentColor = BgPrimary
                            ),
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Verify", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Log Modal
            if (showQuickLogDialog) {
                QuickLogDialog(
                    initialVitalId = quickLogInitialVital,
                    onDismiss = { showQuickLogDialog = false },
                    onSave = { vitalId, value, secondaryValue, isAdditive, note ->
                        repository.logManualVital(vitalId, value, secondaryValue, isAdditive, note)
                    }
                )
            }

            // Simulator Controls Modal
            if (showSimulatorDialog) {
                SimulatorDialog(
                    activeScenario = uiState.scenario,
                    isOffline = uiState.isOffline,
                    onSelectScenario = { repository.setScenario(it) },
                    onToggleOffline = { repository.toggleOfflineLink() },
                    onDismiss = { showSimulatorDialog = false }
                )
            }

            // Astronaut Exam Guide Sheet
            selectedVitalForGuide?.let { vitalId ->
                ExamGuideSheet(
                    vitalId = vitalId,
                    snapshot = uiState.vitals.find { it.id == vitalId },
                    onDismiss = { selectedVitalForGuide = null },
                    onLogNow = {
                        quickLogInitialVital = it
                        showQuickLogDialog = true
                    },
                    onViewTrends = {
                        selectedVitalForTrends = it
                        activeTab = BottomTab.TRENDS
                        secondaryDestination = null
                    }
                )
            }

            // Emergency Symptom Triage Dialog
            if (showEmergencyTriageDialog) {
                EmergencyTriageDialog(
                    existingSymptoms = uiState.emergencyReportedSymptoms,
                    onDismiss = { showEmergencyTriageDialog = false },
                    onSubmit = { symptoms, note ->
                        repository.reportEmergencySymptoms(symptoms, note)
                        showEmergencyTriageDialog = false
                    }
                )
            }
        }
    }
    }
}

@Composable
private fun EmergencyTriageDialog(
    existingSymptoms: List<String>,
    onDismiss: () -> Unit,
    onSubmit: (symptoms: List<String>, note: String?) -> Unit
) {
    val selectedSymptoms = remember {
        mutableStateListOf<String>().apply { addAll(existingSymptoms) }
    }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CriticalSurfaceGlow)
                .border(1.dp, StatusCritical, RoundedCornerShape(12.dp))
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Emergency Triage",
                    tint = StatusCritical,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Emergency Symptom Triage",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Select any acute red-flag symptoms present. Submitting escalates immediately to Flight Surgeon & Crew Medical Officer.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            CatalogData.emergencySymptomsCatalog.forEach { symptom ->
                val checked = symptom in selectedSymptoms
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (checked) StatusCritical.copy(alpha = 0.16f) else BgSurface)
                        .border(
                            1.dp,
                            if (checked) StatusCritical else BorderSubtle,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (checked) selectedSymptoms.remove(symptom) else selectedSymptoms.add(symptom)
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (checked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = symptom,
                        tint = if (checked) StatusCritical else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = symptom,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Symptom Context / Onset Note (Optional)", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = BgSurfaceHover,
                    unfocusedContainerColor = BgSurfaceHover,
                    focusedBorderColor = StatusCritical,
                    unfocusedBorderColor = BorderSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        onSubmit(selectedSymptoms.toList(), note.trim().ifEmpty { null })
                    },
                    enabled = selectedSymptoms.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusCritical,
                        contentColor = OnColoredButton
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Escalate Now", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
