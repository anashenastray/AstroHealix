package com.example.astrohealix.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.model.PersonalBaseline
import com.example.astrohealix.model.TelemetryPoint
import com.example.astrohealix.model.VitalDefinition
import com.example.astrohealix.ui.theme.AuraCardPure
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
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
import kotlin.math.roundToInt

@Composable
fun TrendChart(
    history: List<TelemetryPoint>,
    baseline: PersonalBaseline,
    unit: String,
    modifier: Modifier = Modifier,
    title: String = ""
) {
    var selectedPoint by remember(history) { mutableStateOf<TelemetryPoint?>(null) }

    // Smooth Progressive Loading Animation from left (Vita SYS requirement)
    val revealAnim = remember(history) { Animatable(0f) }
    LaunchedEffect(history) {
        revealAnim.snapTo(0f)
        revealAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    // Continuous Pulsing Animation for Selected Reticle (Vita SYS requirement)
    val infiniteTransition = rememberInfiniteTransition(label = "chartReticlePulse")
    val reticlePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1050, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reticleRadius"
    )

    // Distinct Vivid Curve Color from Aura Health Palette
    val auraColor = remember(title) { getAuraVitalColor(title) }

    val validValues = history.filter { !it.isDisconnected }.mapNotNull { it.value }
    val rawCandidates = buildList {
        addAll(validValues)
        add(baseline.nominalMin)
        add(baseline.nominalMax)
        baseline.monitorLow?.let { add(it) }
        baseline.monitorHigh?.let { add(it) }
        baseline.criticalLow?.let { add(it) }
        baseline.criticalHigh?.let { add(it) }
    }
    val rawMin = rawCandidates.minOrNull() ?: 0f
    val rawMax = rawCandidates.maxOrNull() ?: 100f
    val rawSpan = (rawMax - rawMin).coerceAtLeast(1f)
    val pad = (rawSpan * 0.12f).coerceAtLeast(0.5f)
    val yMin = rawMin - pad
    val yMax = rawMax + pad
    val yRange = (yMax - yMin).coerceAtLeast(1f)
    val yMid = (yMin + yMax) / 2f

    val useDecimals = yRange < 12f || unit in setOf("°C", "mSv/d", "hrs", "/10")
    fun formatAxisValue(v: Float): String =
        if (useDecimals) "%.1f".format(v) else v.roundToInt().toString()

    val cardShape = RoundedCornerShape(12.dp)
    val isDark = LocalVitaColors.current.isDark
    val cardBg = if (isDark) BgSurface else AuraCardPure

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(cardBg, cardShape)
            .border(1.dp, BorderSubtle, cardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header Row: Clean title + Active Scrub Telemetry HUD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title.ifBlank { "TELEMETRY TREND" },
                    color = auraColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "AURA SMOOTH CURVE • LEFT-TO-RIGHT REVEAL",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            AnimatedVisibility(
                visible = selectedPoint != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedPoint?.let { pt ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(BgSurfaceHover, RoundedCornerShape(4.dp))
                            .border(1.dp, auraColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = pt.timeLabel,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (pt.isDisconnected) "OFFLINE"
                            else if (pt.secondaryValue != null) "${pt.value?.roundToInt()}/${pt.secondaryValue.roundToInt()} $unit"
                            else if (useDecimals) "%.2f %s".format(pt.value ?: 0f, unit)
                            else "${pt.value?.roundToInt() ?: "--"} $unit",
                            color = auraColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Canvas Area with Left Y-Axis and progressive unroll clip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Y-Axis labels (Max, Nominal Upper, Nominal Lower, Min)
            Column(
                modifier = Modifier
                    .width(42.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatAxisValue(yMax),
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = formatAxisValue(baseline.nominalMax),
                    color = StatusNormal,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = formatAxisValue(baseline.nominalMin),
                    color = StatusNormal,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = formatAxisValue(yMin),
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Graph Canvas with Scrubber Touch Detectors
            val gridColor = BorderSubtle.copy(alpha = 0.45f)
            val nominalBandColor = StatusNormal.copy(alpha = 0.08f)
            val hoverBg = BgSurfaceHover
            val holeSurfaceColor = cardBg

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(history) {
                        detectTapGestures(
                            onTap = { offset ->
                                if (history.isNotEmpty()) {
                                    val idx = ((offset.x / size.width) * history.lastIndex)
                                        .roundToInt()
                                        .coerceIn(0, history.lastIndex)
                                    selectedPoint = history[idx]
                                }
                            }
                        )
                    }
                    .pointerInput(history) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (history.isNotEmpty()) {
                                    val idx = ((offset.x / size.width) * history.lastIndex)
                                        .roundToInt()
                                        .coerceIn(0, history.lastIndex)
                                    selectedPoint = history[idx]
                                }
                            },
                            onDrag = { change, _ ->
                                if (history.isNotEmpty()) {
                                    val idx = ((change.position.x / size.width) * history.lastIndex)
                                        .roundToInt()
                                        .coerceIn(0, history.lastIndex)
                                    selectedPoint = history[idx]
                                }
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    fun toY(v: Float): Float {
                        val norm = (v - yMin) / yRange
                        return h - (norm * h).coerceIn(0f, h)
                    }

                    // 1. Nominal Baseline Band
                    val nomTop = toY(baseline.nominalMax)
                    val nomBottom = toY(baseline.nominalMin)
                    drawRect(
                        color = nominalBandColor,
                        topLeft = Offset(0f, nomTop),
                        size = Size(w, (nomBottom - nomTop).coerceAtLeast(1f))
                    )

                    // 2. Horizontal Reference Gridlines
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val gY = (h / gridSteps) * i
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, gY),
                            end = Offset(w, gY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // 3. Progressive horizontal clip for smooth drawing unroll animation from left
                    clipRect(
                        left = 0f,
                        top = 0f,
                        right = w * revealAnim.value,
                        bottom = h
                    ) {
                        if (history.isNotEmpty()) {
                            val pts = history.mapIndexed { idx, pt ->
                                val x = if (history.size <= 1) w / 2f
                                else (idx.toFloat() / history.lastIndex) * w
                                val y = if (pt.value != null && !pt.isDisconnected) toY(pt.value) else null
                                Triple(x, y, pt)
                            }

                            // Build contiguous segments of valid points for smooth cubic bezier curves
                            val segments = mutableListOf<List<Triple<Float, Float, TelemetryPoint>>>()
                            var currentSegment = mutableListOf<Triple<Float, Float, TelemetryPoint>>()
                            for (p in pts) {
                                if (p.second != null) {
                                    currentSegment.add(Triple(p.first, p.second!!, p.third))
                                } else {
                                    if (currentSegment.isNotEmpty()) {
                                        segments.add(currentSegment)
                                        currentSegment = mutableListOf()
                                    }
                                }
                            }
                            if (currentSegment.isNotEmpty()) {
                                segments.add(currentSegment)
                            }

                            // Draw shaded gradient fills under cubic bezier curves
                            for (seg in segments) {
                                if (seg.size >= 2) {
                                    val fillPath = Path()
                                    fillPath.moveTo(seg.first().first, h)
                                    fillPath.lineTo(seg.first().first, seg.first().second)

                                    for (i in 1 until seg.size) {
                                        val prev = seg[i - 1]
                                        val cur = seg[i]
                                        val cX = (prev.first + cur.first) / 2f
                                        fillPath.cubicTo(cX, prev.second, cX, cur.second, cur.first, cur.second)
                                    }
                                    fillPath.lineTo(seg.last().first, h)
                                    fillPath.close()

                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                auraColor.copy(alpha = 0.22f),
                                                auraColor.copy(alpha = 0.02f)
                                            ),
                                            startY = 0f,
                                            endY = h
                                        )
                                    )

                                    // Draw smooth vibrant cubic bezier curve line
                                    val strokePath = Path()
                                    strokePath.moveTo(seg.first().first, seg.first().second)
                                    for (i in 1 until seg.size) {
                                        val prev = seg[i - 1]
                                        val cur = seg[i]
                                        val cX = (prev.first + cur.first) / 2f
                                        strokePath.cubicTo(cX, prev.second, cX, cur.second, cur.first, cur.second)
                                    }

                                    drawPath(
                                        path = strokePath,
                                        color = auraColor,
                                        style = Stroke(
                                            width = 2.5.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 4. Draw Selected Scrubber Reticle with Pulsing Halo (Vita SYS)
                    selectedPoint?.let { sp ->
                        val idx = history.indexOf(sp)
                        if (idx >= 0) {
                            val x = if (history.size <= 1) w / 2f else (idx.toFloat() / history.lastIndex) * w
                            val y = if (sp.value != null && !sp.isDisconnected) toY(sp.value) else null

                            // Vertical scrubber guide line
                            drawLine(
                                color = auraColor.copy(alpha = 0.65f),
                                start = Offset(x, 0f),
                                end = Offset(x, h),
                                strokeWidth = 1.2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )

                            if (y != null) {
                                val center = Offset(x, y)
                                // Pulsing Outer Reticle Ring
                                drawCircle(
                                    color = auraColor.copy(alpha = 0.20f),
                                    radius = (10.dp.toPx() * reticlePulse),
                                    center = center
                                )
                                drawCircle(
                                    color = holeSurfaceColor,
                                    radius = 5.dp.toPx(),
                                    center = center
                                )
                                drawCircle(
                                    color = auraColor,
                                    radius = 5.dp.toPx(),
                                    center = center,
                                    style = Stroke(width = 2.5.dp.toPx())
                                )
                            }
                        }
                    }
                }

                if (history.isEmpty()) {
                    Text(
                        text = "No telemetry history recorded",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // X-axis time labels
        if (history.isNotEmpty()) {
            val tickLabels = remember(history) {
                val indices = if (history.size <= 4) {
                    history.indices.toList()
                } else {
                    listOf(
                        0,
                        history.lastIndex / 3,
                        (history.lastIndex * 2) / 3,
                        history.lastIndex
                    ).distinct()
                }
                indices.map { history[it].timeLabel }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 44.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tickLabels.forEach { label ->
                    Text(
                        text = label,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }
        }

        // Minimalist Legend Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(color = StatusNormal, label = "Nominal")
                ChartLegendItem(color = StatusMonitor, label = "Monitor")
                ChartLegendItem(color = StatusCritical, label = "Critical")
            }

            Text(
                text = "Touch to scrub",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun TrendChart(
    vital: VitalDefinition,
    points: List<TelemetryPoint>,
    modifier: Modifier = Modifier
) {
    TrendChart(
        history = points,
        baseline = vital.baseline,
        unit = vital.unit,
        modifier = modifier,
        title = "${vital.title.uppercase()} (${vital.unit})"
    )
}

@Composable
private fun ChartLegendItem(
    color: Color,
    label: String,
    hollow: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (hollow) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(BgSurface, CircleShape)
                    .border(1.5.dp, color, CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color, CircleShape)
            )
        }
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}
