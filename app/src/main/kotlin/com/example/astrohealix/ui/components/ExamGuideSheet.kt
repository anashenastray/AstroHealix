package com.example.astrohealix.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.astrohealix.model.CatalogData
import com.example.astrohealix.model.DataQuality
import com.example.astrohealix.model.NON_DIAGNOSTIC_DISCLAIMER
import com.example.astrohealix.model.VitalCardSnapshot
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusMonitorBg
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.TextDisabled
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun ExamGuideSheet(
    vitalId: VitalId,
    onDismiss: () -> Unit,
    onLogNow: (VitalId) -> Unit,
    onViewTrends: (VitalId) -> Unit,
    snapshot: VitalCardSnapshot? = null
) {
    ExamGuideSheetImpl(
        vitalId = vitalId,
        snapshot = snapshot,
        onDismiss = onDismiss,
        onLogReading = onLogNow,
        onViewTrends = onViewTrends
    )
}

@Composable
fun ExamGuideSheet(
    vitalId: VitalId,
    snapshot: VitalCardSnapshot?,
    onDismiss: () -> Unit,
    onLogReading: (VitalId) -> Unit,
    onViewTrends: (VitalId) -> Unit
) {
    ExamGuideSheetImpl(
        vitalId = vitalId,
        snapshot = snapshot,
        onDismiss = onDismiss,
        onLogReading = onLogReading,
        onViewTrends = onViewTrends
    )
}

@Composable
private fun ExamGuideSheetImpl(
    vitalId: VitalId,
    snapshot: VitalCardSnapshot?,
    onDismiss: () -> Unit,
    onLogReading: (VitalId) -> Unit,
    onViewTrends: (VitalId) -> Unit
) {
    val def = CatalogData.VITAL_DEFINITIONS[vitalId] ?: return
    val guide = def.guide
    val baseline = snapshot?.baseline ?: def.baseline

    fun formatBound(v: Float): String {
        return if (def.precision > 0) "%.${def.precision}f".format(v) else v.toInt().toString()
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .widthIn(max = 560.dp)
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
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
                            text = "ASTRONAUT EXAM GUIDE",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.05.sp
                        )
                        Text(
                            text = "${def.title} (${def.unit})",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (snapshot != null) {
                            StatusPill(status = snapshot.status, compact = true)
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                // Current Telemetry Snapshot Strip (if provided)
                if (snapshot != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgPrimary)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (snapshot.status == VitalStatus.UNAVAILABLE) "---" else "${snapshot.displayValue} ${snapshot.unit}",
                                    color = if (snapshot.status == VitalStatus.UNAVAILABLE) TextDisabled else TextPrimary,
                                    fontSize = 22.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "[${snapshot.source.name}] • ${snapshot.lastUpdated}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (snapshot.qualityStatus != DataQuality.VALID) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StatusMonitorBg)
                                        .border(1.dp, StatusMonitor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "QUALITY FLAG: ${snapshot.qualityStatus.name}",
                                        color = StatusMonitor,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = snapshot.reason,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }
                }

                // Personal Baseline Range Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgPrimary)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Personal Nominal Baseline:",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = "${formatBound(baseline.nominalMin)}–${formatBound(baseline.nominalMax)} ${def.unit}",
                                color = StatusNormal,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (baseline.monitorLow != null || baseline.monitorHigh != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Monitor Thresholds:",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                                val monLabel = listOfNotNull(
                                    baseline.monitorLow?.let { "< ${formatBound(it)}" },
                                    baseline.monitorHigh?.let { "> ${formatBound(it)}" }
                                ).joinToString(" / ")
                                Text(
                                    text = "$monLabel ${def.unit}",
                                    color = StatusMonitor,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (baseline.criticalLow != null || baseline.criticalHigh != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Critical Thresholds:",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                                val critLabel = listOfNotNull(
                                    baseline.criticalLow?.let { "<= ${formatBound(it)}" },
                                    baseline.criticalHigh?.let { ">= ${formatBound(it)}" }
                                ).joinToString(" / ")
                                Text(
                                    text = "$critLabel ${def.unit}",
                                    color = StatusCritical,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                GuideSection(
                    title = "WHY THIS EXAM MATTERS IN SPACE",
                    content = guide.whyExamMatters
                )

                GuideSection(
                    title = "HOW TO PERFORM MEASUREMENT",
                    content = guide.howToMeasure
                )

                GuideSection(
                    title = "NASA HRP HAZARDS & PHYSIOLOGICAL IMPACT",
                    content = guide.spaceHazardEffects
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "SELF-CARE PROTOCOL ACTIONS",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.05.sp
                    )
                    guide.selfCareSteps.forEachIndexed { i, step ->
                        Text(
                            text = "${i + 1}. $step",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                GuideSection(
                    title = "CRITERIA FOR FLIGHT SURGEON ESCALATION",
                    content = guide.escalateCriteria
                )

                // Non-diagnostic Decision Support Note
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgPrimary)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = NON_DIAGNOSTIC_DISCLAIMER,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 48dp Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onViewTrends(vitalId)
                        },
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
                        Text(
                            text = "View Trends",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onLogReading(vitalId)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BorderActive,
                            contentColor = BgPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text(
                            text = "Log Reading",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.05.sp
        )
        Text(
            text = content,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}
