package com.example.astrohealix.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.model.PersonalBaseline
import com.example.astrohealix.model.TelemetryPoint
import com.example.astrohealix.model.VitalDefinition
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.StatusUnavailable
import com.example.astrohealix.ui.theme.TextSecondary
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

    val cardShape = RoundedCornerShape(8.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(BgSurface, cardShape)
            .border(1.dp, BorderSubtle, cardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header Row (Title & Scrubbed Tooltip)
        if (title.isNotBlank() || selectedPoint != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (title.isNotBlank()) title else "TELEMETRY TREND ($unit)",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                selectedPoint?.let { pt ->
                    val valLabel = if (pt.isDisconnected || pt.value == null) {
                        "NO DATA"
                    } else {
                        "${formatAxisValue(pt.value)} $unit"
                    }
                    Text(
                        text = "${pt.timeLabel}: $valLabel [${pt.source.name}]",
                        color = BorderActive,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Y-Axis Labels + Canvas Plot Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .defaultMinSize(minHeight = 120.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Y-axis value labels
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .padding(end = 6.dp, top = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatAxisValue(yMax),
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
                Text(
                    text = formatAxisValue(yMid),
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
                Text(
                    text = formatAxisValue(yMin),
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }

            // Chart Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(history) {
                            detectTapGestures { offset ->
                                if (history.isNotEmpty()) {
                                    val hPad = 8.dp.toPx()
                                    val plotW = (size.width - hPad * 2f).coerceAtLeast(1f)
                                    val idx = if (history.size == 1) {
                                        0
                                    } else {
                                        val stepX = plotW / (history.size - 1)
                                        ((offset.x - hPad) / stepX).roundToInt()
                                            .coerceIn(0, history.size - 1)
                                    }
                                    selectedPoint = history[idx]
                                }
                            }
                        }
                ) {
                    val hPad = 8.dp.toPx()
                    val vPad = 8.dp.toPx()
                    val plotWidth = (size.width - hPad * 2f).coerceAtLeast(1f)
                    val plotHeight = (size.height - vPad * 2f).coerceAtLeast(1f)

                    fun getX(index: Int): Float {
                        if (history.size <= 1) return hPad + plotWidth * 0.5f
                        return hPad + (index.toFloat() / (history.size - 1)) * plotWidth
                    }

                    fun getY(value: Float): Float {
                        val normalized = ((value - yMin) / yRange).coerceIn(0f, 1f)
                        return vPad + plotHeight * (1f - normalized)
                    }

                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

                    // 1. Nominal baseline semi-transparent green band
                    val nominalTopY = getY(baseline.nominalMax)
                    val nominalBottomY = getY(baseline.nominalMin)
                    drawRect(
                        color = StatusNormal.copy(alpha = 0.14f),
                        topLeft = Offset(0f, nominalTopY),
                        size = Size(size.width, (nominalBottomY - nominalTopY).coerceAtLeast(2f))
                    )

                    // Nominal dashed boundary lines
                    drawLine(
                        color = StatusNormal.copy(alpha = 0.45f),
                        start = Offset(0f, nominalTopY),
                        end = Offset(size.width, nominalTopY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                    drawLine(
                        color = StatusNormal.copy(alpha = 0.45f),
                        start = Offset(0f, nominalBottomY),
                        end = Offset(size.width, nominalBottomY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )

                    // Critical dashed boundary lines
                    baseline.criticalHigh?.let { cHigh ->
                        val cy = getY(cHigh)
                        drawLine(
                            color = StatusCritical.copy(alpha = 0.5f),
                            start = Offset(0f, cy),
                            end = Offset(size.width, cy),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashEffect
                        )
                    }
                    baseline.criticalLow?.let { cLow ->
                        val cy = getY(cLow)
                        drawLine(
                            color = StatusCritical.copy(alpha = 0.5f),
                            start = Offset(0f, cy),
                            end = Offset(size.width, cy),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashEffect
                        )
                    }

                    if (history.isEmpty()) return@Canvas

                    // 2. Gap shading spans for disconnected / null regions
                    var gapStartIdx: Int? = null
                    for (i in history.indices) {
                        val pt = history[i]
                        val isGap = pt.isDisconnected || pt.value == null
                        if (isGap) {
                            if (gapStartIdx == null) gapStartIdx = i
                        } else if (gapStartIdx != null) {
                            val gx1 = getX(gapStartIdx)
                            val gx2 = getX(i)
                            drawRect(
                                color = StatusUnavailable.copy(alpha = 0.18f),
                                topLeft = Offset(gx1, 0f),
                                size = Size((gx2 - gx1).coerceAtLeast(4.dp.toPx()), size.height)
                            )
                            gapStartIdx = null
                        }
                    }
                    if (gapStartIdx != null) {
                        val gx1 = getX(gapStartIdx)
                        drawRect(
                            color = StatusUnavailable.copy(alpha = 0.18f),
                            topLeft = Offset(gx1, 0f),
                            size = Size((size.width - gx1).coerceAtLeast(4.dp.toPx()), size.height)
                        )
                    }

                    // 3. Connected line segments + dashed bridges over gaps
                    var activePath = Path()
                    var pathOpen = false
                    var lastValidOffset: Offset? = null
                    var sawGapSinceLastValid = false

                    for (i in history.indices) {
                        val pt = history[i]
                        val v = pt.value
                        val isGap = pt.isDisconnected || v == null
                        if (isGap) {
                            if (pathOpen) {
                                drawPath(
                                    path = activePath,
                                    color = BorderActive,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                activePath = Path()
                                pathOpen = false
                            }
                            sawGapSinceLastValid = true
                        } else {
                            val px = getX(i)
                            val py = getY(v)
                            val currOffset = Offset(px, py)

                            if (sawGapSinceLastValid && lastValidOffset != null) {
                                // Dashed gap indicator across disconnected span
                                drawLine(
                                    color = StatusUnavailable.copy(alpha = 0.8f),
                                    start = lastValidOffset,
                                    end = currOffset,
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = dashEffect
                                )
                            }

                            if (!pathOpen) {
                                activePath.moveTo(px, py)
                                pathOpen = true
                            } else {
                                activePath.lineTo(px, py)
                            }

                            lastValidOffset = currOffset
                            sawGapSinceLastValid = false
                        }
                    }

                    if (pathOpen) {
                        drawPath(
                            path = activePath,
                            color = BorderActive,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    // 4. Data points (status color + hollow for estimated)
                    for (i in history.indices) {
                        val pt = history[i]
                        val v = pt.value
                        if (pt.isDisconnected || v == null) continue

                        val px = getX(i)
                        val py = getY(v)
                        val center = Offset(px, py)

                        val isCriticalPt = (baseline.criticalLow != null && v <= baseline.criticalLow) ||
                            (baseline.criticalHigh != null && v >= baseline.criticalHigh)
                        val isMonitorPt = !isCriticalPt && (
                            v < baseline.nominalMin ||
                                v > baseline.nominalMax ||
                                (baseline.monitorLow != null && v <= baseline.monitorLow) ||
                                (baseline.monitorHigh != null && v >= baseline.monitorHigh)
                            )

                        val ptColor = when {
                            isCriticalPt -> StatusCritical
                            isMonitorPt -> StatusMonitor
                            else -> StatusNormal
                        }

                        if (selectedPoint == pt) {
                            drawLine(
                                color = BorderActive.copy(alpha = 0.5f),
                                start = Offset(px, 0f),
                                end = Offset(px, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        if (isCriticalPt) {
                            drawCircle(
                                color = StatusCritical.copy(alpha = 0.25f),
                                radius = 7.dp.toPx(),
                                center = center
                            )
                        }

                        if (pt.isEstimated) {
                            // Hollow circle for estimated points
                            drawCircle(
                                color = BgSurface,
                                radius = 4.dp.toPx(),
                                center = center
                            )
                            drawCircle(
                                color = ptColor,
                                radius = 4.dp.toPx(),
                                center = center,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        } else {
                            drawCircle(
                                color = ptColor,
                                radius = 3.5.dp.toPx(),
                                center = center
                            )
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

        // Compact Legend Row
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
                ChartLegendItem(color = StatusMonitor, label = "Est.", hollow = true)
                ChartLegendItem(color = StatusUnavailable, label = "Gap")
            }

            Text(
                text = "Tap point",
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
        title = "${vital.title.uppercase()} (${vital.unit}) vs PERSONAL BASELINE"
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
