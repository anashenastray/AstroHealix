package com.example.astrohealix.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material3.Button
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
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.components.BodyMapHUD
import com.example.astrohealix.ui.components.StatusPill
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.CriticalSurfaceGlow
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.StatusUnavailable
import com.example.astrohealix.ui.theme.TextDisabled
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp)
    ) {
        val isWide = maxWidth >= 720.dp

        if (isWide) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BodyMapHeader()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.95f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BodyMapHUD(
                            vitals = state.vitals,
                            selectedZone = selectedZone,
                            onSelectZone = { selectedZone = it },
                            heightDp = 360
                        )
                        ZoneSelectorGrid(
                            selectedZone = selectedZone,
                            onSelectZone = { selectedZone = it }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1.05f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ZoneDetailCard(
                            zone = selectedZone,
                            zoneVitals = zoneVitals,
                            onOpenGuide = onOpenExamGuide,
                            onLogMetric = onLogMetric
                        )
                        HazardsMatrixSection(hazards = state.hazards)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BodyMapHeader()
                BodyMapHUD(
                    vitals = state.vitals,
                    selectedZone = selectedZone,
                    onSelectZone = { selectedZone = it },
                    heightDp = 280
                )
                ZoneSelectorGrid(
                    selectedZone = selectedZone,
                    onSelectZone = { selectedZone = it }
                )
                ZoneDetailCard(
                    zone = selectedZone,
                    zoneVitals = zoneVitals,
                    onOpenGuide = onOpenExamGuide,
                    onLogMetric = onLogMetric
                )
                HazardsMatrixSection(hazards = state.hazards)
            }
        }
    }
}

@Composable
private fun BodyMapHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "ANATOMICAL BODY MAP",
            color = TextPrimary,
            fontSize = 22.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.02).sp
        )
        Text(
            text = "SYSTEM TELEMETRY ZONES & NASA HRP 5 HAZARDS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.05.sp
        )
    }
}

@Composable
private fun ZoneSelectorGrid(
    selectedZone: BodyZoneId,
    onSelectZone: (BodyZoneId) -> Unit
) {
    val row1Zones = listOf(
        BodyZoneId.HEAD_NEURO to "NEURO",
        BodyZoneId.RESPIRATORY to "RESPIRATORY",
        BodyZoneId.CARDIOVASCULAR to "CARDIO"
    )
    val row2Zones = listOf(
        BodyZoneId.METABOLIC_RENAL to "METABOLIC",
        BodyZoneId.MUSCULOSKELETAL to "MUSCULO",
        BodyZoneId.IMMUNE_RADIATION to "IMMUNE/RAD"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(row1Zones, row2Zones).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { (zone, label) ->
                    val isSelected = zone == selectedZone
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BgSurfaceHover else BgSurface)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) BorderActive else BorderSubtle,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectZone(zone) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) BorderActive else TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false
                        )
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BgSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = zone.title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = zone.subtitle,
                color = TextSecondary,
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif
            )
        }

        // Recommended Space Medical Exam
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(BgPrimary)
                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Recommended Exam",
                tint = BorderActive,
                modifier = Modifier.size(18.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "RECOMMENDED SPACE EXAM / TELEMEDICINE",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.05.sp
                )
                Text(
                    text = zone.recommendedUltrasound,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Associated Zone Vitals
        if (zoneVitals.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                zoneVitals.forEach { vSnap ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgPrimary)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { onOpenGuide(vSnap.id) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = vSnap.title,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (vSnap.status == VitalStatus.UNAVAILABLE) "---" else vSnap.displayValue,
                                    color = if (vSnap.status == VitalStatus.UNAVAILABLE) TextDisabled else TextPrimary,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                if (vSnap.status != VitalStatus.UNAVAILABLE) {
                                    Text(
                                        text = vSnap.unit,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }
                        }

                        StatusPill(status = vSnap.status, compact = true)
                    }
                }
            }
        }

        // Action Buttons (48dp minimum touch targets)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onOpenGuide(firstVital) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = BgSurfaceHover,
                    contentColor = TextPrimary
                ),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Exam Guide", fontSize = 13.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onLogMetric(firstVital) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BorderActive,
                    contentColor = BgPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Reading", fontSize = 13.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HazardsMatrixSection(hazards: List<HazardScore>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "NASA HRP 5 HAZARDS EXPOSURE MATRIX",
            color = TextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.05.sp
        )

        hazards.forEach { hazard ->
            HazardCard(hazard = hazard)
        }
    }
}

@Composable
private fun HazardCard(hazard: HazardScore) {
    val statusColor = when (hazard.status) {
        VitalStatus.NORMAL -> StatusNormal
        VitalStatus.MONITOR -> StatusMonitor
        VitalStatus.CRITICAL -> StatusCritical
        VitalStatus.UNAVAILABLE -> StatusUnavailable
    }
    val cardBg = if (hazard.status == VitalStatus.CRITICAL) CriticalSurfaceGlow else BgSurface
    val cardBorder = if (hazard.status == VitalStatus.CRITICAL) StatusCritical else BorderSubtle

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(8.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(statusColor)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hazard.label.uppercase(),
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${hazard.score}/100",
                        color = statusColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    StatusPill(status = hazard.status, compact = true)
                }
            }

            Text(
                text = hazard.summary,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}
