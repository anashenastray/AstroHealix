package com.example.astrohealix.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.data.AstroUiState
import com.example.astrohealix.model.BodySystemsCatalog
import com.example.astrohealix.model.ClinicalProtocol
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
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun ProtocolsScreen(
    state: AstroUiState,
    initialExpandedProtocolCode: String? = null,
    onTransmitToFlightSurgeon: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isDark = LocalVitaColors.current.isDark
    val surfaceColor = if (isDark) BgSurface else AuraCardPure

    var protocolsList by remember {
        mutableStateOf(BodySystemsCatalog.getDefaultClinicalProtocols())
    }

    var expandedProtocolCode by remember(initialExpandedProtocolCode) {
        mutableStateOf(initialExpandedProtocolCode ?: protocolsList.firstOrNull()?.code)
    }

    var showReportPreview by remember { mutableStateOf(false) }

    // Live Timer Controls
    var activeTimerCode by remember { mutableStateOf<String?>(null) }
    var remainingSeconds by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning, remainingSeconds) {
        if (isTimerRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            if (remainingSeconds == 0) {
                isTimerRunning = false
            }
        }
    }

    val reportPayload = remember(state.vitals, state.astronaut, state.environment) {
        buildString {
            appendLine("=== CDME-PRO-MAX CLINICAL TELEMETRY REPORT ===")
            appendLine("ASTRONAUT: ${state.astronaut.name} (${state.astronaut.id})")
            appendLine("MISSION: ${state.astronaut.missionName} • Sol ${state.astronaut.missionDay}")
            appendLine("ENVIRONMENT: ${state.environment.title} • ${state.environment.commDelayLabel}")
            appendLine("READINESS SCORE: ${state.readinessScore}% • STREAK: ${state.astronaut.streakDays} Sols")
            appendLine("CABIN GAS: O2 ${state.astronaut.habitatO2Percent}%, CO2 ${state.astronaut.cabinCo2MmHg} mmHg")
            appendLine("----------------------------------------------")
            appendLine("CURRENT VITALS:")
            state.vitals.forEach { v ->
                appendLine(" • ${v.shortLabel}: ${v.displayValue} ${v.unit} [${v.status.name}] (${v.deltaFromBaseline})")
            }
            appendLine("----------------------------------------------")
            val unackAlerts = state.alerts.filter { !it.isAcknowledged }
            appendLine("ACTIVE ALERTS: ${if (unackAlerts.isEmpty()) "None - Nominal" else unackAlerts.joinToString { it.title }}")
            appendLine("SAFETY INTERLOCK: DETERMINISTIC VALIDATION ACTIVE")
            appendLine("==============================================")
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
            .testTag("protocols_screen"),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header with Flight Surgeon Direct Uplink Pill
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
                                text = "Clinical Spaceflight Protocols",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "ACLS Zero-G • Hypercapnia • Emergency RAG Interlocks",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Share Vitals Button to Flight Surgeon
                        Button(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "CDME Flight Surgeon Uplink - ${state.astronaut.name}")
                                    putExtra(Intent.EXTRA_TEXT, reportPayload)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Transmit Vitals to Flight Surgeon"))
                                onTransmitToFlightSurgeon(reportPayload)
                                Toast.makeText(context, "Vitals Transmitted to Flight Surgeon Queue", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BorderActive),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Surgeon Uplink", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    HorizontalDivider(color = BorderSubtle)
                }
            }
        }

        // 2. Interactive Accordion Checklists
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                protocolsList.forEach { protocol ->
                    val isExpanded = expandedProtocolCode == protocol.code
                    val completedSteps = protocol.steps.count { it.isCompleted }
                    val totalSteps = protocol.steps.size

                    val containerBorderColor by animateColorAsState(
                        targetValue = if (isExpanded) BorderActive else BorderSubtle,
                        animationSpec = tween(200),
                        label = "protocolBorder"
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, containerBorderColor, RoundedCornerShape(12.dp))
                            .clickable {
                                expandedProtocolCode = if (isExpanded) null else protocol.code
                            },
                        color = surfaceColor,
                        shadowElevation = if (isExpanded) 4.dp else 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                                .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = protocol.code,
                                            color = BorderActive,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "• ${protocol.category}",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = protocol.title,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusPill(status = protocol.severity, compact = true)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Subtitle summary and completion badge
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Trigger: ${protocol.triggerCondition}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$completedSteps/$totalSteps done",
                                    color = if (completedSteps == totalSteps) StatusNormal else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Expanded Checklist Content
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    HorizontalDivider(color = BorderSubtle)

                                    // Countdown Timer Widget if protocol specifies one
                                    protocol.timerDurationSeconds?.let { dur ->
                                        val isThisTimerActive = activeTimerCode == protocol.code
                                        val curRemaining = if (isThisTimerActive) remainingSeconds else dur
                                        val minutes = curRemaining / 60
                                        val secs = curRemaining % 60
                                        val timeStr = "%02d:%02d".format(minutes, secs)

                                        Surface(
                                            color = if (isDark) Color(0xFF1E283E) else Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.Timer,
                                                        contentDescription = null,
                                                        tint = BorderActive,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Column {
                                                        Text(
                                                            text = protocol.timerLabel ?: "Protocol Pacer Timer",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = TextPrimary
                                                        )
                                                        Text(
                                                            text = timeStr,
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Black,
                                                            fontFamily = FontFamily.Monospace,
                                                            color = if (curRemaining <= 10 && curRemaining > 0) StatusCritical else BorderActive
                                                        )
                                                    }
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            if (activeTimerCode != protocol.code) {
                                                                activeTimerCode = protocol.code
                                                                remainingSeconds = dur
                                                                isTimerRunning = true
                                                            } else {
                                                                isTimerRunning = !isTimerRunning
                                                            }
                                                        },
                                                        shape = RoundedCornerShape(50),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(30.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isTimerRunning && activeTimerCode == protocol.code) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                            contentDescription = null,
                                                            tint = BorderActive,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(
                                                            text = if (isTimerRunning && activeTimerCode == protocol.code) "Pause" else "Start",
                                                            fontSize = 11.sp,
                                                            color = BorderActive
                                                        )
                                                    }

                                                    OutlinedButton(
                                                        onClick = {
                                                            activeTimerCode = protocol.code
                                                            remainingSeconds = dur
                                                            isTimerRunning = false
                                                        },
                                                        shape = RoundedCornerShape(50),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(30.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Refresh,
                                                            contentDescription = "Reset",
                                                            tint = TextSecondary,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Protocol Action Steps
                                    protocol.steps.forEach { step ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    // Toggle step completed
                                                    protocolsList = protocolsList.map { p ->
                                                        if (p.code == protocol.code) {
                                                            p.copy(steps = p.steps.map { s ->
                                                                if (s.index == step.index) s.copy(isCompleted = !s.isCompleted) else s
                                                            })
                                                        } else p
                                                    }
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Checkbox(
                                                checked = step.isCompleted,
                                                onCheckedChange = { chk ->
                                                    protocolsList = protocolsList.map { p ->
                                                        if (p.code == protocol.code) {
                                                            p.copy(steps = p.steps.map { s ->
                                                                if (s.index == step.index) s.copy(isCompleted = chk) else s
                                                            })
                                                        } else p
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = BorderActive,
                                                    checkmarkColor = Color.White
                                                ),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "${step.index}. ${step.instruction}",
                                                    color = if (step.isCompleted) TextSecondary else TextPrimary,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                if (step.detail.isNotBlank()) {
                                                    Text(
                                                        text = step.detail,
                                                        color = TextSecondary,
                                                        fontSize = 11.sp,
                                                        lineHeight = 15.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Telemetry Export & Flight Surgeon Transmit Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(surfaceColor)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Flight Surgeon Telemetry Packet",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Encrypted Sol 142 Telemetry Delta",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Surface(
                        onClick = { showReportPreview = !showReportPreview },
                        shape = RoundedCornerShape(6.dp),
                        color = BorderActive.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (showReportPreview) "Hide Preview" else "View Payload",
                            color = BorderActive,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = showReportPreview) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgPrimary, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = reportPayload,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(reportPayload))
                            Toast.makeText(context, "Telemetry copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BorderSubtle),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "CDME-PRO-MAX Vitals Packet")
                                putExtra(Intent.EXTRA_TEXT, reportPayload)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Transmit Vitals"))
                            Toast.makeText(context, "Queued for Houston transmission", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BorderActive),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share to Surgeon", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
