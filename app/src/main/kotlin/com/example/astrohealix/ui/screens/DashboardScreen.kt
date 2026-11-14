package com.example.astrohealix.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.data.AstroUiState
import com.example.astrohealix.model.HealthAlert
import com.example.astrohealix.model.RoutineTask
import com.example.astrohealix.model.VitalCardSnapshot
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.components.StatusPill
import com.example.astrohealix.ui.theme.AuraCardPure
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.LocalVitaColors
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.StatusUnavailable
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary
import com.example.astrohealix.ui.theme.getAuraVitalColor

private enum class DashboardFilter(val label: String) {
    ALL("All Vitals"),
    ATTENTION("Attention"),
    CARDIO("Cardio"),
    METABOLIC("Metabolic")
}

/**
 * Minimal Spaceflight Dashboard taken directly from Vita SYS design scheme.
 * Minimalist, high contrast, clean typography, with dedicated Flight Surgeon sharing.
 */
@Composable
fun DashboardScreen(
    state: AstroUiState,
    onSelectVital: (VitalId) -> Unit,
    onQuickLog: (VitalId) -> Unit,
    onAcknowledgeAlert: (String) -> Unit,
    onOpenSimControls: () -> Unit,
    onToggleTask: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalVitaColors.current.isDark
    val surfaceColor = if (isDark) BgSurface else AuraCardPure

    var selectedFilter by remember { mutableStateOf(DashboardFilter.ALL) }

    val displayedVitals = remember(state.vitals, selectedFilter) {
        when (selectedFilter) {
            DashboardFilter.ALL -> state.vitals
            DashboardFilter.ATTENTION -> state.vitals.filter {
                it.status == VitalStatus.CRITICAL || it.status == VitalStatus.MONITOR || it.status == VitalStatus.UNAVAILABLE
            }
            DashboardFilter.CARDIO -> state.vitals.filter {
                it.id in setOf(VitalId.HEART_RATE, VitalId.BLOOD_PRESSURE, VitalId.SPO2, VitalId.RESPIRATORY_RATE)
            }
            DashboardFilter.METABOLIC -> state.vitals.filter {
                it.id in setOf(VitalId.BLOOD_GLUCOSE, VitalId.HYDRATION_ML, VitalId.BODY_TEMP, VitalId.RADIATION_MSV, VitalId.SLEEP_HOURS)
            }
        }.sortedBy {
            when (it.status) {
                VitalStatus.CRITICAL -> 0
                VitalStatus.MONITOR -> 1
                VitalStatus.UNAVAILABLE -> 2
                VitalStatus.NORMAL -> 3
            }
        }
    }

    val activeAlerts = remember(state.alerts) {
        state.alerts.sortedBy { it.isAcknowledged }
    }

    val reportPayload = remember(state.vitals, state.astronaut, state.environment) {
        buildString {
            appendLine("VITA-SYS TELEMETRY | ${state.astronaut.name} | Sol ${state.astronaut.missionDay}")
            appendLine("ENVIRONMENT: ${state.environment.title} (${state.environment.gravityLabel})")
            appendLine("READINESS: ${state.readinessScore}% • CO2: ${state.astronaut.cabinCo2MmHg} mmHg")
            state.vitals.forEach { v ->
                appendLine(" • ${v.shortLabel}: ${v.displayValue} ${v.unit} [${v.status.name}]")
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Minimal Vita SYS Header (Readiness, Sol, Gas metrics)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = surfaceColor,
                shadowElevation = 2.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "VITA-SYS • ${state.astronaut.name}",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Sol ${state.astronaut.missionDay} • ${state.environment.shortName} • ${state.environment.commDelayLabel}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Readiness Score Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(BorderActive.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "READINESS",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${state.readinessScore}%",
                                color = BorderActive,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Gas Metrics Mini Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) Color(0xFF141A28) else Color(0xFFF1F5F9))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "O2: ${state.astronaut.habitatO2Percent}%",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "pCO2: ${state.astronaut.cabinCo2MmHg} mmHg",
                            color = if (state.astronaut.cabinCo2MmHg > 3.0f) StatusCritical else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (state.astronaut.cabinCo2MmHg > 3.0f) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Scenario: ${state.scenario.title.take(18)}...",
                            color = BorderActive,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    HorizontalDivider(color = BorderSubtle)
                }
            }
        }

        // 2. Flight Surgeon Share & Simulator Quick Action Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share Vitals Button to Flight Surgeon (Explicitly Requested)
                Button(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "CDME Flight Surgeon Telemetry - Sol ${state.astronaut.missionDay}")
                            putExtra(Intent.EXTRA_TEXT, reportPayload)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Vitals to Flight Surgeon"))
                        Toast.makeText(context, "Vitals sent to Flight Surgeon queue", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BorderActive),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share to Surgeon",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }

                // Simulator Scenario Trigger
                OutlinedButton(
                    onClick = onOpenSimControls,
                    modifier = Modifier.height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Simulate",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Simulate",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 3. Active Emergency Alerts Banner
        if (activeAlerts.any { !it.isAcknowledged }) {
            val alert = activeAlerts.first { !it.isAcknowledged }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (alert.severity == VitalStatus.CRITICAL) StatusCritical.copy(alpha = 0.12f)
                            else StatusMonitor.copy(alpha = 0.12f)
                        )
                        .border(
                            1.dp,
                            if (alert.severity == VitalStatus.CRITICAL) StatusCritical else StatusMonitor,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (alert.severity == VitalStatus.CRITICAL) StatusCritical else StatusMonitor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = alert.title,
                                    color = if (alert.severity == VitalStatus.CRITICAL) StatusCritical else StatusMonitor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                            StatusPill(status = alert.severity, compact = true)
                        }

                        Text(
                            text = alert.guidance,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { onAcknowledgeAlert(alert.id) },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (alert.severity == VitalStatus.CRITICAL) StatusCritical else StatusMonitor
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Acknowledge", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. Minimalist Filter Tabs (All, Attention, Cardio, Metabolic)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DashboardFilter.entries) { filter ->
                    val isSel = selectedFilter == filter
                    Surface(
                        onClick = { selectedFilter = filter },
                        shape = RoundedCornerShape(50),
                        color = if (isSel) BorderActive else surfaceColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) BorderActive else BorderSubtle),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Text(
                                text = filter.label,
                                color = if (isSel) Color.White else TextPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 5. Vita SYS Minimal Vital Metric Cards (with Aura Colors)
        items(displayedVitals) { vital ->
            val auraColor = remember(vital.title) { getAuraVitalColor(vital.title) }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .clickable { onSelectVital(vital.id) },
                color = surfaceColor,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left Colored Accent Stripe from Aura Health
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(36.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(auraColor)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = vital.shortLabel,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = vital.displayValue,
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = vital.unit,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        StatusPill(status = vital.status, compact = true)
                        Text(
                            text = vital.deltaFromBaseline,
                            color = TextSecondary,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 6. Mission Daily Tasks Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(surfaceColor)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SOL 142 HEALTH PROTOCOL TASKS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${state.routine.count { it.isCompleted }}/${state.routine.size} Done",
                        color = StatusNormal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                state.routine.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleTask(task.id) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleTask(task.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = BorderActive,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = task.title,
                                color = if (task.isCompleted) TextSecondary else TextPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${task.targetLabel} • ${task.cadence}",
                                color = TextSecondary,
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
