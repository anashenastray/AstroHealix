package com.example.astrohealix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.data.AstroRepository
import com.example.astrohealix.model.MissionEnvironment
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.ui.components.ExamGuideSheet
import com.example.astrohealix.ui.components.QuickLogDialog
import com.example.astrohealix.ui.components.SimulatorDialog
import com.example.astrohealix.ui.screens.BodyMapScreen
import com.example.astrohealix.ui.screens.DashboardScreen
import com.example.astrohealix.ui.screens.LogsScreen
import com.example.astrohealix.ui.screens.TrendsScreen
import com.example.astrohealix.ui.theme.BgApp
import com.example.astrohealix.ui.theme.BgAppGradientEnd
import com.example.astrohealix.ui.theme.BgAppGradientStart
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary
import com.example.astrohealix.ui.theme.VoidBlack

enum class AppNavDestination(val label: String, val shortCode: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", "DASH", Icons.Default.Dashboard),
    BODY_MAP("Body Map", "BODY", Icons.Default.AccessibilityNew),
    TRENDS("Analytics", "TREND", Icons.AutoMirrored.Filled.ShowChart),
    LOGS("Logs & Queue", "LOGS", Icons.Default.History)
}

@Composable
fun AstroHealixApp(
    repository: AstroRepository = remember { AstroRepository() }
) {
    val uiState by repository.uiState.collectAsState()

    var currentDestination by remember { mutableStateOf(AppNavDestination.DASHBOARD) }
    var selectedVitalForGuide by remember { mutableStateOf<VitalId?>(null) }
    var selectedVitalForTrends by remember { mutableStateOf(VitalId.HEART_RATE) }
    var showQuickLogDialog by remember { mutableStateOf(false) }
    var quickLogInitialVital by remember { mutableStateOf(VitalId.HYDRATION_ML) }
    var showSimulatorDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgAppGradientStart, VoidBlack, BgAppGradientEnd)))
            .safeDrawingPadding()
    ) {
        // --- AEROSPACE LEFT NAVIGATION RAIL (Void Glass) ---
        Box(
            modifier = Modifier
                .width(76.dp)
                .fillMaxHeight()
                .background(Color(0x3D080E1C))
                .border(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Brand Mark
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x2600F0FF))
                            .border(1.dp, BorderActive, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AGY",
                            color = BorderActive,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "VITA-SYS",
                        color = TextSecondary,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Middle Navigation Items
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppNavDestination.entries.forEach { destination ->
                        val isSelected = currentDestination == destination

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .defaultMinSize(minWidth = 56.dp, minHeight = 52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0x3300F0FF) else Color(0x1F141F36))
                                .border(
                                    1.dp,
                                    if (isSelected) BorderActive else Color(0x1A475D85),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { currentDestination = destination }
                                .padding(vertical = 8.dp, horizontal = 6.dp)
                        ) {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                tint = if (isSelected) BorderActive else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = destination.shortCode,
                                color = if (isSelected) BorderActive else TextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // Bottom Controls Trigger
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x2600F0FF).copy(alpha = 0.12f))
                            .border(1.dp, BorderActive.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .clickable { showSimulatorDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Sim Controls",
                            tint = BorderActive,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "SIM",
                        color = TextSecondary,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // --- MAIN WORKSTATION CANVAS ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(BgApp)
        ) {
            when (currentDestination) {
                AppNavDestination.DASHBOARD -> DashboardScreen(
                    state = uiState,
                    onTapVital = { vitalId -> selectedVitalForGuide = vitalId },
                    onAcknowledgeAlert = { alertId -> repository.acknowledgeAlert(alertId) },
                    onEscalateAlert = { alertId -> repository.escalateAlert(alertId) },
                    onToggleRoutine = { taskId -> repository.toggleRoutineTask(taskId) },
                    onCycleEnvironment = {
                        val nextEnv = when (uiState.environment) {
                            MissionEnvironment.ORBITAL_STATION -> MissionEnvironment.DEEP_SPACE_TRANSIT
                            MissionEnvironment.DEEP_SPACE_TRANSIT -> MissionEnvironment.SURFACE_BASE
                            MissionEnvironment.SURFACE_BASE -> MissionEnvironment.ROVER_EVA
                            MissionEnvironment.ROVER_EVA -> MissionEnvironment.ORBITAL_STATION
                        }
                        repository.setEnvironment(nextEnv)
                    },
                    onOpenSimulator = { showSimulatorDialog = true },
                    onOpenQuickLog = { vitalId ->
                        quickLogInitialVital = vitalId
                        showQuickLogDialog = true
                    },
                    onNavigateToLogs = { currentDestination = AppNavDestination.LOGS },
                    onNavigateToBodyMap = { currentDestination = AppNavDestination.BODY_MAP }
                )

                AppNavDestination.BODY_MAP -> BodyMapScreen(
                    state = uiState,
                    onOpenExamGuide = { vitalId -> selectedVitalForGuide = vitalId },
                    onLogMetric = { vitalId ->
                        quickLogInitialVital = vitalId
                        showQuickLogDialog = true
                    }
                )

                AppNavDestination.TRENDS -> TrendsScreen(
                    state = uiState,
                    initialVitalId = selectedVitalForTrends,
                    getHistory = { vitalId -> repository.getTelemetryHistory(vitalId) }
                )

                AppNavDestination.LOGS -> LogsScreen(
                    state = uiState,
                    onForceSync = { repository.reconcileOfflineQueue() },
                    onExportCsv = { repository.exportReportCsv() }
                )
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
                    onSelectScenario = { scenario -> repository.setScenario(scenario) },
                    onToggleOffline = { repository.toggleOfflineLink() },
                    onDismiss = { showSimulatorDialog = false }
                )
            }

            // Astronaut Exam Guide Drawer
            selectedVitalForGuide?.let { vitalId ->
                ExamGuideSheet(
                    vitalId = vitalId,
                    onDismiss = { selectedVitalForGuide = null },
                    onLogNow = {
                        quickLogInitialVital = it
                        showQuickLogDialog = true
                    },
                    onViewTrends = {
                        selectedVitalForTrends = it
                        currentDestination = AppNavDestination.TRENDS
                    }
                )
            }
        }
    }
}
