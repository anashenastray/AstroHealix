package com.example.astrohealix.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.model.BodyZoneId
import com.example.astrohealix.model.VitalCardSnapshot
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.StatusUnavailable
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary
import kotlin.math.hypot

@Composable
fun BodyMapHUD(
    vitals: List<VitalCardSnapshot>,
    selectedZone: BodyZoneId,
    onSelectZone: (BodyZoneId) -> Unit,
    modifier: Modifier = Modifier,
    heightDp: Int = 300,
    isMini: Boolean = false
) {
    val zoneStatuses = BodyZoneId.entries.associateWith { zone ->
        val zoneVitals = vitals.filter { it.bodyZone == zone }
        when {
            zoneVitals.any { it.status == VitalStatus.CRITICAL } -> VitalStatus.CRITICAL
            zoneVitals.any { it.status == VitalStatus.MONITOR } -> VitalStatus.MONITOR
            zoneVitals.any { it.status == VitalStatus.UNAVAILABLE } -> VitalStatus.UNAVAILABLE
            else -> VitalStatus.NORMAL
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(BgSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
            .padding(if (isMini) 8.dp else 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header: Render only in full mode to prevent collision
            if (!isMini) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ASTRONAUT PHYSIOLOGICAL MAP",
                        color = BorderActive,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "TARGET: ${selectedZone.title.uppercase()}",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val cx = size.width * 0.5f
                            val cy = size.height * 0.5f
                            val figH = size.height * 0.88f
                            val figW = minOf(size.width * 0.40f, figH * 0.50f)

                            val nodes = listOf(
                                BodyZoneId.HEAD_NEURO to Offset(cx, cy - figH * 0.38f),
                                BodyZoneId.RESPIRATORY to Offset(cx - figW * 0.24f, cy - figH * 0.18f),
                                BodyZoneId.CARDIOVASCULAR to Offset(cx + figW * 0.20f, cy - figH * 0.16f),
                                BodyZoneId.IMMUNE_RADIATION to Offset(cx - figW * 0.42f, cy - figH * 0.02f),
                                BodyZoneId.METABOLIC_RENAL to Offset(cx, cy + figH * 0.02f),
                                BodyZoneId.MUSCULOSKELETAL to Offset(cx, cy + figH * 0.26f)
                            )

                            for ((zoneId, pos) in nodes) {
                                if (hypot(offset.x - pos.x, offset.y - pos.y) <= 32.dp.toPx()) {
                                    onSelectZone(zoneId)
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
            ) {
                val cx = size.width * 0.5f
                val cy = size.height * 0.5f
                val figH = size.height * 0.88f
                val figW = minOf(size.width * 0.40f, figH * 0.50f)

                // Background Technical Crosshair Lines
                drawLine(
                    color = BorderSubtle.copy(alpha = 0.4f),
                    start = Offset(cx, cy - figH * 0.50f),
                    end = Offset(cx, cy + figH * 0.50f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = BorderSubtle.copy(alpha = 0.3f),
                    start = Offset(cx - figW * 0.9f, cy),
                    end = Offset(cx + figW * 0.9f, cy),
                    strokeWidth = 1f
                )

                // High-Tech Spacesuit Vector Path
                val suitPath = Path().apply {
                    val topY = cy - figH * 0.48f
                    val botY = cy + figH * 0.46f

                    // Helmet Arc
                    moveTo(cx - figW * 0.22f, topY + figH * 0.08f)
                    cubicTo(
                        cx - figW * 0.25f, topY,
                        cx + figW * 0.25f, topY,
                        cx + figW * 0.22f, topY + figH * 0.08f
                    )
                    // Neck & Right Shoulder
                    lineTo(cx + figW * 0.45f, topY + figH * 0.15f)
                    // Right Upper Arm
                    lineTo(cx + figW * 0.56f, topY + figH * 0.36f)
                    // Right Forearm & Glove
                    lineTo(cx + figW * 0.44f, topY + figH * 0.38f)
                    lineTo(cx + figW * 0.34f, topY + figH * 0.24f)
                    // Right Flank
                    lineTo(cx + figW * 0.28f, cy + figH * 0.06f)
                    // Right Leg & Boot
                    lineTo(cx + figW * 0.30f, botY)
                    lineTo(cx + figW * 0.10f, botY)
                    // Inseam
                    lineTo(cx, cy + figH * 0.16f)
                    // Left Leg & Boot
                    lineTo(cx - figW * 0.10f, botY)
                    lineTo(cx - figW * 0.30f, botY)
                    // Left Flank
                    lineTo(cx - figW * 0.28f, cy + figH * 0.06f)
                    // Left Arm & Glove
                    lineTo(cx - figW * 0.34f, topY + figH * 0.24f)
                    lineTo(cx - figW * 0.44f, topY + figH * 0.38f)
                    lineTo(cx - figW * 0.56f, topY + figH * 0.36f)
                    // Left Shoulder
                    lineTo(cx - figW * 0.45f, topY + figH * 0.15f)
                    close()
                }

                // Draw Spacesuit Outline
                drawPath(
                    path = suitPath,
                    color = BorderSubtle.copy(alpha = 0.85f),
                    style = Stroke(width = 1.6f.dp.toPx())
                )

                // Helmet Visor (OLED Cyan Glow)
                drawOval(
                    color = BorderActive.copy(alpha = 0.25f),
                    topLeft = Offset(cx - figW * 0.14f, cy - figH * 0.44f),
                    size = Size(figW * 0.28f, figH * 0.10f)
                )
                drawOval(
                    color = BorderActive,
                    topLeft = Offset(cx - figW * 0.14f, cy - figH * 0.44f),
                    size = Size(figW * 0.28f, figH * 0.10f),
                    style = Stroke(width = 1.2f.dp.toPx())
                )

                // Chest Display Unit outline
                drawRect(
                    color = BorderSubtle,
                    topLeft = Offset(cx - figW * 0.16f, cy - figH * 0.24f),
                    size = Size(figW * 0.32f, figH * 0.12f),
                    style = Stroke(width = 1f.dp.toPx())
                )

                // Anatomical Nodes
                val nodes = listOf(
                    Triple(BodyZoneId.HEAD_NEURO, Offset(cx, cy - figH * 0.38f), if (isMini) 12f else 17f),
                    Triple(BodyZoneId.RESPIRATORY, Offset(cx - figW * 0.24f, cy - figH * 0.18f), if (isMini) 11f else 15f),
                    Triple(BodyZoneId.CARDIOVASCULAR, Offset(cx + figW * 0.20f, cy - figH * 0.16f), if (isMini) 11f else 15f),
                    Triple(BodyZoneId.IMMUNE_RADIATION, Offset(cx - figW * 0.42f, cy - figH * 0.02f), if (isMini) 11f else 14f),
                    Triple(BodyZoneId.METABOLIC_RENAL, Offset(cx, cy + figH * 0.02f), if (isMini) 11f else 15f),
                    Triple(BodyZoneId.MUSCULOSKELETAL, Offset(cx, cy + figH * 0.26f), if (isMini) 12f else 16f)
                )

                for ((zoneId, pos, radiusDp) in nodes) {
                    val isSelected = zoneId == selectedZone
                    val status = zoneStatuses[zoneId] ?: VitalStatus.NORMAL
                    val statusColor = when (status) {
                        VitalStatus.NORMAL -> StatusNormal
                        VitalStatus.MONITOR -> StatusMonitor
                        VitalStatus.CRITICAL -> StatusCritical
                        VitalStatus.UNAVAILABLE -> StatusUnavailable
                    }

                    // Outer halo when selected
                    if (isSelected) {
                        drawCircle(
                            color = BorderActive.copy(alpha = 0.35f),
                            radius = (radiusDp + 7f).dp.toPx(),
                            center = pos
                        )
                    }

                    // Status Ring
                    drawCircle(
                        color = statusColor,
                        radius = radiusDp.dp.toPx(),
                        center = pos,
                        style = Stroke(width = if (isSelected) 2.2f.dp.toPx() else 1.5f.dp.toPx())
                    )

                    // Core point
                    drawCircle(
                        color = if (isSelected) BorderActive else statusColor.copy(alpha = 0.5f),
                        radius = (radiusDp * 0.42f).dp.toPx(),
                        center = pos
                    )
                }
            }

            if (!isMini) {
                Text(
                    text = "Tap any anatomical sensor node to inspect space physiology, NASA hazards & guided exams",
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
