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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.example.astrohealix.model.BodyZoneId
import com.example.astrohealix.model.HazardScore
import com.example.astrohealix.model.VitalCardSnapshot
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.ui.components.BodyMapHUD
import com.example.astrohealix.ui.components.StatusPill
import com.example.astrohealix.ui.theme.BgApp
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun BodyMapScreen(
    state: AstroUiState,
    onOpenExamGuide: (VitalId) -> Unit,
    onLogMetric: (VitalId) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedZone by remember { mutableStateOf(BodyZoneId.CARDIOVASCULAR) }
    val zoneVitals = state.vitals.filter { it.bodyZone == selectedZone }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgApp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ASTRONAUT ANATOMICAL HUD & 5 NASA HRP HAZARDS",
                color = BorderActive,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "INTERACTIVE CLINICAL DECISION SUPPORT",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Two-Column Split Layout for Tablet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- LEFT COLUMN: ANATOMICAL FIGURE (45% width) ---
            Column(
                modifier = Modifier
                    .weight(0.95f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BodyMapHUD(
                    vitals = state.vitals,
                    selectedZone = selectedZone,
                    onSelectZone = { selectedZone = it },
                    heightDp = 390
                )

                // Quick selector buttons arranged in 2 rows of 3 buttons (Clean & No text wrapping!)
                val row1Zones = listOf(
                    BodyZoneId.HEAD_NEURO to "NEURO / SLEEP",
                    BodyZoneId.RESPIRATORY to "RESPIRATORY",
                    BodyZoneId.CARDIOVASCULAR to "CARDIO"
                )
                val row2Zones = listOf(
                    BodyZoneId.METABOLIC_RENAL to "METABOLIC",
                    BodyZoneId.MUSCULOSKELETAL to "MUSCULOSKELETAL",
                    BodyZoneId.IMMUNE_RADIATION to "IMMUNE / RAD"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row1Zones.forEach { (zone, label) ->
                            val isSelected = zone == selectedZone
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) BorderActive.copy(alpha = 0.15f) else BgSurface)
                                    .border(1.dp, if (isSelected) BorderActive else BorderSubtle, RoundedCornerShape(4.dp))
                                    .clickable { selectedZone = zone }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) BorderActive else TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row2Zones.forEach { (zone, label) ->
                            val isSelected = zone == selectedZone
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) BorderActive.copy(alpha = 0.15f) else BgSurface)
                                    .border(1.dp, if (isSelected) BorderActive else BorderSubtle, RoundedCornerShape(4.dp))
                                    .clickable { selectedZone = zone }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) BorderActive else TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // --- RIGHT COLUMN: ZONE CLINICAL DETAILS & NASA 5 HAZARDS (55% width) ---
            Column(
                modifier = Modifier
                    .weight(1.05f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Selected Zone Detail Card
                ZoneDetailCard(
                    zone = selectedZone,
                    zoneVitals = zoneVitals,
                    onOpenGuide = onOpenExamGuide,
                    onLogMetric = onLogMetric
                )

                // NASA HRP 5 Hazards Exposure Matrix
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "NASA HRP 5 HAZARDS EXPOSURE MATRIX",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    state.hazards.forEach { hazard ->
                        HazardCard(hazard = hazard)
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoneDetailCard(
    zone: BodyZoneId,
    zoneVitals: List<VitalCardSnapshot>,
    onOpenGuide: (VitalId) -> Unit,
    onLogMetric: (VitalId) -> Unit
) {
    val firstVital = zoneVitals.firstOrNull()?.id ?: VitalId.HEART_RATE

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(BgSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Zone Title & Description
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = zone.title.uppercase(),
                    color = BorderActive,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = zone.subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // Recommended Space Medical Exam
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(BgApp)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Exam",
                        tint = BorderActive,
                        modifier = Modifier.size(14.dp)
                    )
                    Column {
                        Text(
                            text = "Recommended Space Exam / Telemedicine:",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = zone.recommendedUltrasound,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Associated Vitals telemetry row
            if (zoneVitals.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    zoneVitals.forEach { vSnap ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(BgApp)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = vSnap.shortLabel,
                                        color = TextSecondary,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    StatusPill(status = vSnap.status, compact = true)
                                }

                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = vSnap.displayValue,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = vSnap.unit,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(bottom = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onOpenGuide(firstVital) },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BgApp,
                        contentColor = BorderActive
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderActive),
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 38.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Exam Instructions", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onLogMetric(firstVital) },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BgApp,
                        contentColor = TextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 38.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Log Reading", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun HazardCard(hazard: HazardScore) {
    val statusColor = when (hazard.status) {
        com.example.astrohealix.model.VitalStatus.NORMAL -> StatusNormal
        com.example.astrohealix.model.VitalStatus.MONITOR -> StatusMonitor
        com.example.astrohealix.model.VitalStatus.CRITICAL -> StatusCritical
        com.example.astrohealix.model.VitalStatus.UNAVAILABLE -> com.example.astrohealix.ui.theme.StatusUnavailable
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(BgSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hazard.label.uppercase(),
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "RISK: ${hazard.score}/100",
                        color = statusColor,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    StatusPill(status = hazard.status, compact = true)
                }
            }

            Text(
                text = hazard.summary,
                color = TextSecondary,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}
