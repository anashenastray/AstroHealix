package com.example.astrohealix.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.data.AstroUiState
import com.example.astrohealix.model.RoutineTask
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.components.AlertBanner
import com.example.astrohealix.ui.components.HealthCard
import com.example.astrohealix.ui.components.MissionHeader
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.CriticalSurfaceGlow
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    state: AstroUiState,
    onTapVital: (VitalId) -> Unit,
    onAcknowledgeAlert: (String) -> Unit,
    onEscalateAlert: (String) -> Unit,
    onToggleRoutine: (String) -> Unit,
    onOpenQuickLog: (VitalId) -> Unit,
    onOpenEmergencyTriage: () -> Unit,
    onOpenAlerts: () -> Unit,
    onShareToSurgeon: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var routineExpanded by remember { mutableStateOf(false) }

    val urgentAlerts = remember(state.alerts) {
        state.alerts
            .filter {
                !it.isAcknowledged &&
                    (it.severity == VitalStatus.CRITICAL || it.severity == VitalStatus.MONITOR)
            }
            .sortedBy { if (it.severity == VitalStatus.CRITICAL) 0 else 1 }
    }
    val displayedAlerts = remember(urgentAlerts) { urgentAlerts.take(2) }
    val vitalRows = remember(state.vitals) { state.vitals.chunked(2) }
    val completedRoutineCount = remember(state.routine) { state.routine.count { it.isCompleted } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // 1. Compact single-row Mission Header
        MissionHeader(
            astronaut = state.astronaut,
            environment = state.environment,
            readinessScore = state.readinessScore,
            isOffline = state.isOffline,
            pendingSyncCount = state.pendingSyncQueue.size,
            isDemoData = state.isDemoData
        )

        // Single LazyColumn prevents nested scrolling crash with 2-column rows
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgPrimary),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2. Active Alerts (up to 2 CRITICAL/MONITOR AlertBanners)
            if (displayedAlerts.isNotEmpty()) {
                item(key = "alerts-header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE ALERTS (${urgentAlerts.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                        if (state.alerts.size > displayedAlerts.size) {
                            TextButton(
                                onClick = onOpenAlerts,
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text(
                                    text = "View All (${state.alerts.size})",
                                    color = BorderActive,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                items(
                    items = displayedAlerts,
                    key = { "alert-${it.id}" }
                ) { alert ->
                    AlertBanner(
                        alert = alert,
                        onAcknowledge = onAcknowledgeAlert,
                        onEscalate = onEscalateAlert
                    )
                }
            }

            // Emergency Symptom Triage Quick Action
            item(key = "emergency-triage") {
                EmergencyTriageActionCard(
                    reportedCount = state.emergencyReportedSymptoms.size,
                    onClick = onOpenEmergencyTriage
                )
            }

            // 3. Vital Cards Section Header + 2-column Grid
            item(key = "vitals-section-header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TELEMETRY",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = onShareToSurgeon,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderActive),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = BgSurface,
                                contentColor = BorderActive
                            ),
                            modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share to Flight Surgeon",
                                modifier = Modifier.size(16.dp),
                                tint = BorderActive
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "Share to Surgeon",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { onOpenQuickLog(VitalId.HYDRATION_ML) },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderActive),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = BgSurface,
                                contentColor = BorderActive
                            ),
                            modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Log Vital",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "Log Vital",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            items(
                items = vitalRows,
                key = { row -> "vital-row-" + row.joinToString("-") { it.id.name } }
            ) { rowVitals ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowVitals.forEach { snapshot ->
                        HealthCard(
                            snapshot = snapshot,
                            onTapCard = onTapVital,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowVitals.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // 4. Compact Collapsible Mission Routine Checklist
            item(key = "routine-checklist") {
                CollapsibleRoutineSection(
                    tasks = state.routine,
                    completedCount = completedRoutineCount,
                    expanded = routineExpanded,
                    onToggleExpand = { routineExpanded = !routineExpanded },
                    onToggleTask = onToggleRoutine
                )
            }

            // 5. Non-diagnostic disclaimer
            item(key = "disclaimer") {
                Text(
                    text = state.disclaimer,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EmergencyTriageActionCard(
    reportedCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (reportedCount > 0) CriticalSurfaceGlow else BgSurface)
            .border(
                width = 1.dp,
                color = if (reportedCount > 0) StatusCritical else StatusCritical.copy(alpha = 0.45f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Emergency Symptom Triage",
                tint = StatusCritical,
                modifier = Modifier.size(20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Emergency Symptom Triage",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (reportedCount > 0) {
                        "$reportedCount urgent symptom(s) logged with Flight Surgeon"
                    } else {
                        "Report acute red-flag symptoms immediately"
                    },
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = "REPORT",
            color = StatusCritical,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CollapsibleRoutineSection(
    tasks: List<RoutineTask>,
    completedCount: Int,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleTask: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BgSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clickable(onClick = onToggleExpand)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Mission Routine Checklist",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$completedCount of ${tasks.size} daily tasks completed",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse routine" else "Expand routine",
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(BgSurfaceHover)
                            .border(
                                width = 1.dp,
                                color = if (task.isCompleted) StatusNormal.copy(alpha = 0.45f) else BorderSubtle,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onToggleTask(task.id) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) {
                                    Icons.Default.CheckCircle
                                } else {
                                    Icons.Default.RadioButtonUnchecked
                                },
                                contentDescription = task.title,
                                tint = if (task.isCompleted) StatusNormal else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = task.title,
                                color = if (task.isCompleted) TextPrimary else TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (task.isCompleted) FontWeight.Medium else FontWeight.Normal
                            )
                        }

                        Spacer(modifier = Modifier.size(8.dp))

                        Text(
                            text = task.cadence,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
