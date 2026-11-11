package com.example.astrohealix.ui.screens

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.astrohealix.model.BodyZoneId
import com.example.astrohealix.model.RoutineTask
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.ui.components.AlertBanner
import com.example.astrohealix.ui.components.BodyMapHUD
import com.example.astrohealix.ui.components.HealthCard
import com.example.astrohealix.ui.components.MissionHeader
import com.example.astrohealix.ui.theme.BgApp
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
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
    onCycleEnvironment: () -> Unit,
    onOpenSimulator: () -> Unit,
    onOpenQuickLog: (VitalId) -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToBodyMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeAlertIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgApp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Sleek Top Mission Bar
        MissionHeader(
            astronaut = state.astronaut,
            environment = state.environment,
            isOffline = state.isOffline,
            pendingSyncCount = state.pendingSyncQueue.size,
            lastSyncedAt = state.lastSyncedAt,
            onCycleEnvironment = onCycleEnvironment,
            onOpenSimulator = onOpenSimulator
        )

        // 2. Active Alert Ribbon (Compact, non-intrusive, pager-supported)
        if (state.alerts.isNotEmpty()) {
            val safeIndex = activeAlertIndex.coerceIn(0, state.alerts.size - 1)
            val currentAlert = state.alerts[safeIndex]
            AlertBanner(
                alert = currentAlert,
                onAcknowledge = onAcknowledgeAlert,
                onEscalate = onEscalateAlert,
                currentIndex = safeIndex + 1,
                totalCount = state.alerts.size,
                onNextAlert = {
                    activeAlertIndex = (safeIndex + 1) % state.alerts.size
                },
                onPrevAlert = {
                    activeAlertIndex = if (safeIndex - 1 < 0) state.alerts.size - 1 else safeIndex - 1
                }
            )
        }

        // 3. Main Split Console Layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- LEFT / CENTER REGION: 3-COLUMN VITAL TELEMETRY GRID (63% width) ---
            Column(
                modifier = Modifier
                    .weight(1.35f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Section Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VITAL TELEMETRY OVERVIEW",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Auto-Prioritized: Critical & Monitor first",
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // 3-Column Telemetry Grid (Instant visibility of all vitals!)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.vitals) { vitalCard ->
                        HealthCard(
                            snapshot = vitalCard,
                            onTapCard = onTapVital
                        )
                    }
                }
            }

            // --- RIGHT REGION: READINESS COCKPIT & MINI BODY MAP (37% width) ---
            Column(
                modifier = Modifier
                    .weight(0.75f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Crew Readiness & Routine Checklist
                CockpitReadinessCard(
                    tasks = state.routine,
                    readinessScore = state.readinessScore,
                    streakDays = state.astronaut.streakDays,
                    onToggleTask = onToggleRoutine
                )

                // Interactive Mini Body Map Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                        .clickable { onNavigateToBodyMap() }
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ANATOMICAL SYSTEMS HUD",
                                color = BorderActive,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "EXPAND >",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        BodyMapHUD(
                            vitals = state.vitals,
                            selectedZone = BodyZoneId.CARDIOVASCULAR,
                            onSelectZone = { onNavigateToBodyMap() },
                            heightDp = 180,
                            isMini = true
                        )
                    }
                }

                // Quick Action Strip
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onOpenQuickLog(VitalId.HYDRATION_ML) },
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = BgSurface,
                                contentColor = BorderActive
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderActive),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 40.dp)
                        ) {
                            Icon(Icons.Default.LocalDrink, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("+ Hydration", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onOpenQuickLog(VitalId.MOOD_INDEX) },
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = BgSurface,
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 40.dp)
                        ) {
                            Icon(Icons.Default.Mood, contentDescription = null, tint = BorderActive, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("+ Mood", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    OutlinedButton(
                        onClick = onNavigateToLogs,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BgSurface,
                            contentColor = TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 38.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Export Flight Surgeon Telemetry Spec", fontSize = 10.sp, fontFamily = FontFamily.SansSerif)
                    }
                }
            }
        }
    }
}

@Composable
private fun CockpitReadinessCard(
    tasks: List<RoutineTask>,
    readinessScore: Int,
    streakDays: Int,
    onToggleTask: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(BgSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Header: Title & Readiness Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MISSION ROUTINE & READINESS",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Streak: $streakDays d",
                        color = StatusNormal,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$readinessScore/100",
                        color = BorderActive,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Checklist tasks
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                tasks.forEach { task ->
                    val isDone = task.isCompleted
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isDone) BgApp else BgSurface)
                            .border(1.dp, if (isDone) StatusNormal.copy(alpha = 0.5f) else BorderSubtle, RoundedCornerShape(4.dp))
                            .clickable { onToggleTask(task.id) }
                            .padding(horizontal = 7.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = task.title,
                                tint = if (isDone) StatusNormal else TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = task.title,
                                color = if (isDone) TextPrimary else TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = if (isDone) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }

                        Text(
                            text = task.cadence,
                            color = TextSecondary.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
