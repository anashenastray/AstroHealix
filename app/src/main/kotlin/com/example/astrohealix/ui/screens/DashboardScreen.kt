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
import com.example.astrohealix.ui.components.HeroHealthCard
import com.example.astrohealix.ui.components.MissionHeader
import com.example.astrohealix.ui.theme.BgApp
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.GlassCardBorderBrush
import com.example.astrohealix.ui.theme.GlassCardFillBrush
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary
import com.example.astrohealix.ui.theme.TextTertiary
import androidx.compose.ui.graphics.Color
import com.example.astrohealix.model.VitalStatus

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
    var cardCountMode by remember { mutableIntStateOf(3) }
    var selectedBatchIndex by remember { mutableIntStateOf(0) }

    val batchTitles = listOf(
        "PRIORITY FOCUS",
        "CARDIO & O2",
        "METABOLIC & SLEEP",
        "HABITAT & WORKLOAD"
    )

    // Dynamically recomputed displayed cards based on active batch & cardCountMode (2 or 3)
    val displayedCards = remember(state.vitals, cardCountMode, selectedBatchIndex) {
        val batchVitals = when (selectedBatchIndex) {
            0 -> {
                val urgent = state.vitals.filter { it.status == VitalStatus.CRITICAL || it.status == VitalStatus.MONITOR }
                val normal = state.vitals.filterNot { it.status == VitalStatus.CRITICAL || it.status == VitalStatus.MONITOR }
                urgent + normal
            }
            1 -> {
                val cardioIds = listOf(VitalId.HEART_RATE, VitalId.BLOOD_PRESSURE, VitalId.SPO2)
                state.vitals.filter { it.id in cardioIds }
            }
            2 -> {
                val metaIds = listOf(VitalId.BODY_TEMP, VitalId.BLOOD_GLUCOSE, VitalId.SLEEP_HOURS)
                state.vitals.filter { it.id in metaIds }
            }
            else -> {
                val workloadIds = listOf(VitalId.HYDRATION_ML, VitalId.BONE_EXERCISE_MIN, VitalId.RADIATION_MSV)
                state.vitals.filter { it.id in workloadIds }
            }
        }
        batchVitals.take(cardCountMode)
    }

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
            // --- LEFT / CENTER REGION: FOCUSED 2 OR 3 HERO CARDS (63% width) ---
            Column(
                modifier = Modifier
                    .weight(1.35f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Section Title & Controls Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "FOCUSED VITAL TELEMETRY",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "• NASA HRP Continuous Telemetry",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Card Count Selector (3 Cards vs 2 Cards) + Next/Prev
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 3 CARDS / 2 CARDS Toggle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x22FFFFFF))
                                .border(1.dp, Color(0x45FFFFFF), RoundedCornerShape(6.dp))
                        ) {
                            listOf(3, 2).forEach { count ->
                                val isSel = cardCountMode == count
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) Color(0x55FFFFFF) else Color.Transparent)
                                        .clickable { cardCountMode = count }
                                        .padding(horizontal = 9.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "$count CARDS",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) TextPrimary else TextSecondary
                                    )
                                }
                            }
                        }

                        // Pagination: < PREV | NEXT >
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x25FFFFFF))
                                    .border(1.dp, Color(0x50FFFFFF), RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedBatchIndex = if (selectedBatchIndex > 0) selectedBatchIndex - 1 else batchTitles.size - 1
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text("<", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }

                            Text(
                                text = "${selectedBatchIndex + 1}/${batchTitles.size}",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x25FFFFFF))
                                    .border(1.dp, Color(0x50FFFFFF), RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedBatchIndex = if (selectedBatchIndex < batchTitles.size - 1) selectedBatchIndex + 1 else 0
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(">", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                // Batch Category Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    batchTitles.forEachIndexed { idx, title ->
                        val isSelected = selectedBatchIndex == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0x45FFFFFF) else Color(0x1CFFFFFF))
                                .border(1.dp, if (isSelected) Color(0x90FFFFFF) else Color(0x30FFFFFF), RoundedCornerShape(8.dp))
                                .clickable { selectedBatchIndex = idx }
                                .padding(vertical = 7.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // 2 OR 3 HERO CARDS DISPLAYED SIDE-BY-SIDE
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    displayedCards.forEach { card ->
                        HeroHealthCard(
                            snapshot = card,
                            onTapCard = onTapVital,
                            onOpenQuickLog = onOpenQuickLog,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
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
                        .clip(RoundedCornerShape(8.dp))
                        .background(GlassCardFillBrush)
                        .border(1.dp, GlassCardBorderBrush, RoundedCornerShape(8.dp))
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
                                color = TextPrimary,
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
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0x18FFFFFF),
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x35FFFFFF)),
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
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0x18FFFFFF),
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x35FFFFFF)),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 40.dp)
                        ) {
                            Icon(Icons.Default.Mood, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("+ Mood", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    OutlinedButton(
                        onClick = onNavigateToLogs,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x12FFFFFF),
                            contentColor = TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF)),
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
            .clip(RoundedCornerShape(8.dp))
            .background(GlassCardFillBrush)
            .border(1.dp, GlassCardBorderBrush, RoundedCornerShape(8.dp))
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
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$readinessScore/100",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Checklist tasks (Apple Frosted Glass Tiles)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                tasks.forEach { task ->
                    val isDone = task.isCompleted
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDone) Color(0x22FFFFFF) else Color(0x0EFFFFFF))
                            .border(1.dp, if (isDone) Color(0x45FFFFFF) else Color(0x15FFFFFF), RoundedCornerShape(6.dp))
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
                                tint = if (isDone) TextPrimary else TextTertiary,
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
                            color = TextTertiary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
