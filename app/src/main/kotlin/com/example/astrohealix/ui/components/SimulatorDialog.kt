package com.example.astrohealix.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.astrohealix.model.SimulationScenario
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusMonitorBg
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun SimulatorDialog(
    activeScenario: SimulationScenario,
    isOffline: Boolean,
    onSelectScenario: (SimulationScenario) -> Unit,
    onToggleOffline: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(BgSurface)
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MISSION TELEMETRY SIMULATOR",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "VITA-SYS SIM",
                        color = BorderActive,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Prominent [DEMO DATA] Banner Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusMonitorBg)
                        .border(1.dp, StatusMonitor, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BgPrimary)
                            .border(1.dp, StatusMonitor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "[DEMO DATA]",
                            color = StatusMonitor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.05.sp
                        )
                    }
                    Text(
                        text = "Simulated spaceflight telemetry stream for validation and crew protocol drills. Not live flight sensor hardware.",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Text(
                    text = "Select a scenario to inject spaceflight telemetry conditions, sensor dropouts, link blackouts, or threshold alerts:",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    lineHeight = 18.sp
                )

                // Scenario Cards
                SimulationScenario.entries.forEach { scenario ->
                    val isSelected = scenario == activeScenario
                    val badgeColor = when (scenario.badge) {
                        "CRITICAL" -> StatusCritical
                        "ANOMALY", "STEP 2 DEMO", "BEHAVIORAL" -> StatusMonitor
                        "DEFAULT" -> StatusNormal
                        else -> BorderActive
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BgSurfaceHover else BgPrimary)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) BorderActive else BorderSubtle,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onSelectScenario(scenario)
                                onDismiss()
                            }
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = scenario.title,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "[${scenario.badge}]",
                                color = if (isSelected) BorderActive else badgeColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = scenario.description,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                // Offline Link Toggle
                OutlinedButton(
                    onClick = {
                        onToggleOffline()
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isOffline) StatusMonitorBg else BgSurfaceHover,
                        contentColor = if (isOffline) StatusMonitor else TextPrimary
                    ),
                    border = BorderStroke(1.dp, if (isOffline) StatusMonitor else BorderActive),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(
                        text = if (isOffline) "Restore Satellite Link (Sync Queue)" else "Simulate Link Interruption (Offline Cache)",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp
                    )
                }

                // Close Button
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BgPrimary,
                        contentColor = TextSecondary
                    ),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(
                        text = "Close Controls",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
