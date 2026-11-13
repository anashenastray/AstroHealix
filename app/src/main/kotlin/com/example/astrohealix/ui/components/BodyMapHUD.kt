package com.example.astrohealix.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
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

    // Continuous pulsing telemetry animation
    val infiniteTransition = rememberInfiniteTransition(label = "hudPulse")
    val pulseRatio by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRatio"
    )

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
                        text = "ANATOMICAL TELEMETRY",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.05.sp
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BgSurfaceHover)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(BorderActive)
                        )
                        Text(
                            text = selectedZone.title.uppercase(),
                            color = BorderActive,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                            val figW = minOf(size.width * 0.44f, figH * 0.52f)

                            val nodes = listOf(
                                BodyZoneId.HEAD_NEURO to Offset(cx, cy - figH * 0.38f),
                                BodyZoneId.RESPIRATORY to Offset(cx - figW * 0.22f, cy - figH * 0.17f),
                                BodyZoneId.CARDIOVASCULAR to Offset(cx + figW * 0.20f, cy - figH * 0.15f),
                                BodyZoneId.IMMUNE_RADIATION to Offset(cx - figW * 0.44f, cy - figH * 0.02f),
                                BodyZoneId.METABOLIC_RENAL to Offset(cx, cy + figH * 0.04f),
                                BodyZoneId.MUSCULOSKELETAL to Offset(cx, cy + figH * 0.27f)
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
                val figW = minOf(size.width * 0.44f, figH * 0.52f)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)

                // 1. Radar Telemetry Background: Concentric Rings & Crosshairs
                drawCircle(
                    color = gridLineColor.copy(alpha = 0.35f),
                    radius = figH * 0.22f,
                    center = Offset(cx, cy - figH * 0.05f),
                    style = Stroke(width = 1f, pathEffect = dashEffect)
                )
                drawCircle(
                    color = gridLineColor.copy(alpha = 0.25f),
                    radius = figH * 0.42f,
                    center = Offset(cx, cy - figH * 0.05f),
                    style = Stroke(width = 1f, pathEffect = dashEffect)
                )

                // Central Axes
                drawLine(
                    color = gridLineColor.copy(alpha = 0.45f),
                    start = Offset(cx, cy - figH * 0.48f),
                    end = Offset(cx, cy + figH * 0.48f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = gridLineColor.copy(alpha = 0.35f),
                    start = Offset(cx - figW * 1.05f, cy),
                    end = Offset(cx + figW * 1.05f, cy),
                    strokeWidth = 1f
                )

                // 4 Corner HUD Framing Brackets [ ]
                val bracketLen = 12.dp.toPx()
                val pad = 4.dp.toPx()
                // Top-Left
                drawLine(gridLineColor, Offset(pad, pad), Offset(pad + bracketLen, pad), 1.5f)
                drawLine(gridLineColor, Offset(pad, pad), Offset(pad, pad + bracketLen), 1.5f)
                // Top-Right
                drawLine(gridLineColor, Offset(size.width - pad, pad), Offset(size.width - pad - bracketLen, pad), 1.5f)
                drawLine(gridLineColor, Offset(size.width - pad, pad), Offset(size.width - pad, pad + bracketLen), 1.5f)
                // Bottom-Left
                drawLine(gridLineColor, Offset(pad, size.height - pad), Offset(pad + bracketLen, size.height - pad), 1.5f)
                drawLine(gridLineColor, Offset(pad, size.height - pad), Offset(pad, size.height - pad - bracketLen), 1.5f)
                // Bottom-Right
                drawLine(gridLineColor, Offset(size.width - pad, size.height - pad), Offset(size.width - pad - bracketLen, size.height - pad), 1.5f)
                drawLine(gridLineColor, Offset(size.width - pad, size.height - pad), Offset(size.width - pad, size.height - pad - bracketLen), 1.5f)

                // 2. Sculpted Astronaut Anatomical Silhouette
                val topY = cy - figH * 0.48f
                val botY = cy + figH * 0.46f

                val suitPath = Path().apply {
                    // Head / Helmet Contour
                    moveTo(cx - figW * 0.20f, topY + figH * 0.08f)
                    cubicTo(
                        cx - figW * 0.24f, topY - figH * 0.01f,
                        cx + figW * 0.24f, topY - figH * 0.01f,
                        cx + figW * 0.20f, topY + figH * 0.08f
                    )
                    // Neck / Collar
                    lineTo(cx + figW * 0.22f, topY + figH * 0.11f)
                    // Right Shoulder
                    cubicTo(
                        cx + figW * 0.35f, topY + figH * 0.12f,
                        cx + figW * 0.44f, topY + figH * 0.15f,
                        cx + figW * 0.48f, topY + figH * 0.19f
                    )
                    // Right Arm & Glove
                    lineTo(cx + figW * 0.58f, topY + figH * 0.36f)
                    lineTo(cx + figW * 0.47f, topY + figH * 0.38f)
                    lineTo(cx + figW * 0.38f, topY + figH * 0.24f)
                    // Right Torso / Waist
                    lineTo(cx + figW * 0.26f, cy + figH * 0.06f)
                    // Right Hip / Leg
                    lineTo(cx + figW * 0.29f, cy + figH * 0.26f)
                    lineTo(cx + figW * 0.28f, botY)
                    lineTo(cx + figW * 0.10f, botY)
                    // Groin Arch
                    lineTo(cx, cy + figH * 0.17f)
                    // Left Leg / Hip
                    lineTo(cx - figW * 0.10f, botY)
                    lineTo(cx - figW * 0.28f, botY)
                    lineTo(cx - figW * 0.29f, cy + figH * 0.26f)
                    // Left Torso / Waist
                    lineTo(cx - figW * 0.26f, cy + figH * 0.06f)
                    // Left Arm & Glove
                    lineTo(cx - figW * 0.38f, topY + figH * 0.24f)
                    lineTo(cx - figW * 0.47f, topY + figH * 0.38f)
                    lineTo(cx - figW * 0.58f, topY + figH * 0.36f)
                    // Left Shoulder
                    cubicTo(
                        cx - figW * 0.48f, topY + figH * 0.19f,
                        cx - figW * 0.35f, topY + figH * 0.12f,
                        cx - figW * 0.22f, topY + figH * 0.11f
                    )
                    close()
                }

                // Inner holographic body gradient fill
                drawPath(
                    path = suitPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            activeRingColor.copy(alpha = 0.08f),
                            activeRingColor.copy(alpha = 0.02f)
                        ),
                        startY = topY,
                        endY = botY
                    )
                )

                // Crisp anti-aliased suit outline
                drawPath(
                    path = suitPath,
                    color = suitOutlineColor.copy(alpha = 0.45f),
                    style = Stroke(
                        width = 1.4f.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                // Helmet Visor Oval with Reflection Highlight
                val visorTop = cy - figH * 0.45f
                val visorWidth = figW * 0.28f
                val visorHeight = figH * 0.10f
                drawOval(
                    color = visorFillColor,
                    topLeft = Offset(cx - visorWidth * 0.5f, visorTop),
                    size = Size(visorWidth, visorHeight)
                )
                drawOval(
                    color = activeRingColor.copy(alpha = 0.5f),
                    topLeft = Offset(cx - visorWidth * 0.5f, visorTop),
                    size = Size(visorWidth, visorHeight),
                    style = Stroke(width = 1.2f.dp.toPx())
                )
                // Visor reflection curve
                drawLine(
                    color = activeRingColor.copy(alpha = 0.7f),
                    start = Offset(cx - visorWidth * 0.3f, visorTop + visorHeight * 0.3f),
                    end = Offset(cx + visorWidth * 0.2f, visorTop + visorHeight * 0.25f),
                    strokeWidth = 1.2f.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Chest Telemetry HUD Pack
                val chestW = figW * 0.30f
                val chestH = figH * 0.11f
                val chestTop = cy - figH * 0.24f
                drawRoundRect(
                    color = gridLineColor.copy(alpha = 0.6f),
                    topLeft = Offset(cx - chestW * 0.5f, chestTop),
                    size = Size(chestW, chestH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )

                // 3. Physiological Nodes & Pulsing Radar Hotspots
                val nodes = listOf(
                    Triple(BodyZoneId.HEAD_NEURO, Offset(cx, cy - figH * 0.38f), if (isMini) 9f else 13f),
                    Triple(BodyZoneId.RESPIRATORY, Offset(cx - figW * 0.22f, cy - figH * 0.17f), if (isMini) 8f else 12f),
                    Triple(BodyZoneId.CARDIOVASCULAR, Offset(cx + figW * 0.20f, cy - figH * 0.15f), if (isMini) 8f else 12f),
                    Triple(BodyZoneId.IMMUNE_RADIATION, Offset(cx - figW * 0.44f, cy - figH * 0.02f), if (isMini) 8f else 12f),
                    Triple(BodyZoneId.METABOLIC_RENAL, Offset(cx, cy + figH * 0.04f), if (isMini) 8f else 12f),
                    Triple(BodyZoneId.MUSCULOSKELETAL, Offset(cx, cy + figH * 0.27f), if (isMini) 9f else 13f)
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

                    val isUrgent = zoneStatus == VitalStatus.CRITICAL || zoneStatus == VitalStatus.MONITOR

                    // Animated telemetry radar ripple for active anomaly/monitor hotspots
                    if (isUrgent) {
                        val maxExpand = 14.dp.toPx()
                        val currentExpand = maxExpand * pulseRatio
                        val rippleAlpha = (1f - pulseRatio).coerceIn(0f, 0.45f)
                        drawCircle(
                            color = statusColor.copy(alpha = rippleAlpha),
                            radius = radiusDp.dp.toPx() + currentExpand,
                            center = pos,
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                    }

                    // Selected targeting reticle brackets
                    if (isSelected) {
                        val selRadius = (radiusDp + 6f).dp.toPx()
                        // Outer pulse ring
                        drawCircle(
                            color = activeRingColor.copy(alpha = 0.2f),
                            radius = selRadius + 4.dp.toPx(),
                            center = pos
                        )
                        // Precision target reticle circle
                        drawCircle(
                            color = activeRingColor,
                            radius = selRadius,
                            center = pos,
                            style = Stroke(width = 1.8.dp.toPx())
                        )
                    }

                    // Node base fill
                    drawCircle(
                        color = statusColor.copy(alpha = 0.22f),
                        radius = radiusDp.dp.toPx(),
                        center = pos
                    )

                    // Node outer ring
                    drawCircle(
                        color = if (isSelected) activeRingColor else statusColor,
                        radius = radiusDp.dp.toPx(),
                        center = pos,
                        style = Stroke(width = if (isSelected) 2.dp.toPx() else 1.4.dp.toPx())
                    )

                    // Node center core dot
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
                .size(7.dp)
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
