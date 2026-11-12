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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.example.astrohealix.ui.theme.BgSurfaceHover
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
            zoneVitals.isEmpty() -> VitalStatus.UNAVAILABLE
            zoneVitals.any { it.status == VitalStatus.CRITICAL } -> VitalStatus.CRITICAL
            zoneVitals.any { it.status == VitalStatus.MONITOR } -> VitalStatus.MONITOR
            zoneVitals.all { it.status == VitalStatus.UNAVAILABLE } -> VitalStatus.UNAVAILABLE
            else -> VitalStatus.NORMAL
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BgSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            .padding(if (isMini) 10.dp else 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isMini) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PHYSIOLOGICAL TELEMETRY MAP",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.05.sp
                    )

                    Text(
                        text = selectedZone.title.uppercase(),
                        color = BorderActive,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val gridLineColor = BorderSubtle
            val suitOutlineColor = TextSecondary
            val visorFillColor = BgSurfaceHover
            val activeRingColor = BorderActive
            val normalDotColor = StatusNormal
            val monitorDotColor = StatusMonitor
            val criticalDotColor = StatusCritical
            val unavailableDotColor = StatusUnavailable

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(onSelectZone) {
                        detectTapGestures { offset ->
                            val cx = size.width * 0.5f
                            val cy = size.height * 0.5f
                            val figH = size.height * 0.88f
                            val figW = minOf(size.width * 0.42f, figH * 0.52f)

                            val nodes = listOf(
                                BodyZoneId.HEAD_NEURO to Offset(cx, cy - figH * 0.38f),
                                BodyZoneId.RESPIRATORY to Offset(cx - figW * 0.24f, cy - figH * 0.18f),
                                BodyZoneId.CARDIOVASCULAR to Offset(cx + figW * 0.20f, cy - figH * 0.16f),
                                BodyZoneId.IMMUNE_RADIATION to Offset(cx - figW * 0.42f, cy - figH * 0.02f),
                                BodyZoneId.METABOLIC_RENAL to Offset(cx, cy + figH * 0.02f),
                                BodyZoneId.MUSCULOSKELETAL to Offset(cx, cy + figH * 0.26f)
                            )

                            for ((zoneId, pos) in nodes) {
                                if (hypot(offset.x - pos.x, offset.y - pos.y) <= 36.dp.toPx()) {
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
                val figW = minOf(size.width * 0.42f, figH * 0.52f)

                // Technical Crosshair Grid Lines
                drawLine(
                    color = gridLineColor.copy(alpha = 0.6f),
                    start = Offset(cx, cy - figH * 0.50f),
                    end = Offset(cx, cy + figH * 0.50f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = gridLineColor.copy(alpha = 0.5f),
                    start = Offset(cx - figW * 0.9f, cy),
                    end = Offset(cx + figW * 0.9f, cy),
                    strokeWidth = 1f
                )

                // Spacesuit Silhouette Vector Path
                val suitPath = Path().apply {
                    val topY = cy - figH * 0.48f
                    val botY = cy + figH * 0.46f

                    moveTo(cx - figW * 0.22f, topY + figH * 0.08f)
                    cubicTo(
                        cx - figW * 0.25f, topY,
                        cx + figW * 0.25f, topY,
                        cx + figW * 0.22f, topY + figH * 0.08f
                    )
                    lineTo(cx + figW * 0.45f, topY + figH * 0.15f)
                    lineTo(cx + figW * 0.56f, topY + figH * 0.36f)
                    lineTo(cx + figW * 0.44f, topY + figH * 0.38f)
                    lineTo(cx + figW * 0.34f, topY + figH * 0.24f)
                    lineTo(cx + figW * 0.28f, cy + figH * 0.06f)
                    lineTo(cx + figW * 0.30f, botY)
                    lineTo(cx + figW * 0.10f, botY)
                    lineTo(cx, cy + figH * 0.16f)
                    lineTo(cx - figW * 0.10f, botY)
                    lineTo(cx - figW * 0.30f, botY)
                    lineTo(cx - figW * 0.28f, cy + figH * 0.06f)
                    lineTo(cx - figW * 0.34f, topY + figH * 0.24f)
                    lineTo(cx - figW * 0.44f, topY + figH * 0.38f)
                    lineTo(cx - figW * 0.56f, topY + figH * 0.36f)
                    lineTo(cx - figW * 0.45f, topY + figH * 0.15f)
                    close()
                }

                drawPath(
                    path = suitPath,
                    color = suitOutlineColor.copy(alpha = 0.55f),
                    style = Stroke(width = 1.6f.dp.toPx())
                )

                // Helmet Visor
                drawOval(
                    color = visorFillColor,
                    topLeft = Offset(cx - figW * 0.14f, cy - figH * 0.44f),
                    size = Size(figW * 0.28f, figH * 0.10f)
                )
                drawOval(
                    color = activeRingColor.copy(alpha = 0.45f),
                    topLeft = Offset(cx - figW * 0.14f, cy - figH * 0.44f),
                    size = Size(figW * 0.28f, figH * 0.10f),
                    style = Stroke(width = 1.2f.dp.toPx())
                )

                // Chest Telemetry Pack
                drawRect(
                    color = gridLineColor,
                    topLeft = Offset(cx - figW * 0.16f, cy - figH * 0.24f),
                    size = Size(figW * 0.32f, figH * 0.12f),
                    style = Stroke(width = 1.dp.toPx())
                )

                val nodes = listOf(
                    Triple(BodyZoneId.HEAD_NEURO, Offset(cx, cy - figH * 0.38f), if (isMini) 10f else 15f),
                    Triple(BodyZoneId.RESPIRATORY, Offset(cx - figW * 0.24f, cy - figH * 0.18f), if (isMini) 9f else 13f),
                    Triple(BodyZoneId.CARDIOVASCULAR, Offset(cx + figW * 0.20f, cy - figH * 0.16f), if (isMini) 9f else 13f),
                    Triple(BodyZoneId.IMMUNE_RADIATION, Offset(cx - figW * 0.42f, cy - figH * 0.02f), if (isMini) 9f else 13f),
                    Triple(BodyZoneId.METABOLIC_RENAL, Offset(cx, cy + figH * 0.02f), if (isMini) 9f else 13f),
                    Triple(BodyZoneId.MUSCULOSKELETAL, Offset(cx, cy + figH * 0.26f), if (isMini) 10f else 14f)
                )

                for ((zoneId, pos, radiusDp) in nodes) {
                    val isSelected = zoneId == selectedZone
                    val zoneStatus = zoneStatuses[zoneId] ?: VitalStatus.NORMAL
                    val statusColor = when (zoneStatus) {
                        VitalStatus.CRITICAL -> criticalDotColor
                        VitalStatus.MONITOR -> monitorDotColor
                        VitalStatus.UNAVAILABLE -> unavailableDotColor
                        VitalStatus.NORMAL -> normalDotColor
                    }

                    if (isSelected) {
                        drawCircle(
                            color = activeRingColor.copy(alpha = 0.22f),
                            radius = (radiusDp + 7f).dp.toPx(),
                            center = pos
                        )
                    }

                    drawCircle(
                        color = statusColor.copy(alpha = 0.18f),
                        radius = radiusDp.dp.toPx(),
                        center = pos
                    )

                    drawCircle(
                        color = if (isSelected) activeRingColor else statusColor,
                        radius = radiusDp.dp.toPx(),
                        center = pos,
                        style = Stroke(width = if (isSelected) 2.2f.dp.toPx() else 1.5f.dp.toPx())
                    )

                    drawCircle(
                        color = statusColor,
                        radius = (radiusDp * 0.45f).dp.toPx(),
                        center = pos
                    )
                }
            }

            if (!isMini) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ZoneLegendDot(color = StatusNormal, label = "NOMINAL")
                    ZoneLegendDot(color = StatusMonitor, label = "MONITOR")
                    ZoneLegendDot(color = StatusCritical, label = "CRITICAL")
                    ZoneLegendDot(color = StatusUnavailable, label = "NO DATA")
                }
            }
        }
    }
}

@Composable
private fun ZoneLegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}
