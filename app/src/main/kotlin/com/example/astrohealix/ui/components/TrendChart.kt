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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.model.PersonalBaseline
import com.example.astrohealix.model.TelemetryPoint
import com.example.astrohealix.model.VitalDefinition
import com.example.astrohealix.ui.theme.BgApp
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.GlassCardBorderBrush
import com.example.astrohealix.ui.theme.GlassCardFillBrush
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.StatusUnavailable
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun TrendChart(
    vital: VitalDefinition,
    points: List<TelemetryPoint>,
    modifier: Modifier = Modifier
) {
    var selectedPoint by remember { mutableStateOf<TelemetryPoint?>(null) }

    val baseline = vital.baseline
    val validValues = points.mapNotNull { it.value }
    val dataMin = validValues.minOrNull() ?: baseline.nominalMin
    val dataMax = validValues.maxOrNull() ?: baseline.nominalMax

    val yMin = minOf(dataMin, baseline.criticalLow ?: baseline.nominalMin, baseline.nominalMin - 5f)
    val yMax = maxOf(dataMax, baseline.criticalHigh ?: baseline.nominalMax, baseline.nominalMax + 10f)
    val yRange = (yMax - yMin).coerceAtLeast(10f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(GlassCardFillBrush)
            .border(1.dp, GlassCardBorderBrush, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Chart Header Row: Title & Tooltip Readout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${vital.title.uppercase()} (${vital.unit}) vs PERSONAL BASELINE",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            selectedPoint?.let { pt ->
                val vStr = pt.value?.let { if (vital.precision > 0) "%.${vital.precision}f".format(it) else it.toInt().toString() } ?: "N/A"
                Text(
                    text = "${pt.timeLabel}: $vStr ${vital.unit} [${pt.source.name}]",
                    color = BorderActive,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Main Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .defaultMinSize(minHeight = 160.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            if (points.isNotEmpty()) {
                                val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                                val idx = (offset.x / stepX).toInt().coerceIn(0, points.size - 1)
                                selectedPoint = points[idx]
                            }
                        }
                    }
            ) {
                val width = size.width
                val height = size.height

                fun getY(value: Float): Float {
                    val normalized = (value - yMin) / yRange
                    return height - (normalized * height).coerceIn(0f, height)
                }

                // 1. Shaded Personal Baseline Nominal Zone
                val baselineTopY = getY(baseline.nominalMax)
                val baselineBottomY = getY(baseline.nominalMin)
                drawRect(
                    color = StatusNormal.copy(alpha = 0.10f),
                    topLeft = Offset(0f, baselineTopY),
                    size = Size(width, (baselineBottomY - baselineTopY).coerceAtLeast(2f))
                )

                // 2. Dotted horizontal gridlines for Critical High, Nominal Max, Nominal Min, Critical Low
                val dottedEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                baseline.criticalHigh?.let { cHigh ->
                    val cy = getY(cHigh)
                    drawLine(
                        color = StatusCritical.copy(alpha = 0.5f),
                        start = Offset(0f, cy),
                        end = Offset(width, cy),
                        strokeWidth = 1f,
                        pathEffect = dottedEffect
                    )
                }

                drawLine(
                    color = BorderSubtle,
                    start = Offset(0f, baselineTopY),
                    end = Offset(width, baselineTopY),
                    strokeWidth = 1f,
                    pathEffect = dottedEffect
                )

                drawLine(
                    color = BorderSubtle,
                    start = Offset(0f, baselineBottomY),
                    end = Offset(width, baselineBottomY),
                    strokeWidth = 1f,
                    pathEffect = dottedEffect
                )

                baseline.criticalLow?.let { cLow ->
                    val cy = getY(cLow)
                    drawLine(
                        color = StatusCritical.copy(alpha = 0.5f),
                        start = Offset(0f, cy),
                        end = Offset(width, cy),
                        strokeWidth = 1f,
                        pathEffect = dottedEffect
                    )
                }

                if (points.isEmpty()) return@Canvas

                val stepX = width / (points.size - 1).coerceAtLeast(1)

                // 3. Render Disconnected Gap Spans
                var gapStartIndex: Int? = null
                for (i in points.indices) {
                    val pt = points[i]
                    if (pt.isDisconnected || pt.value == null) {
                        if (gapStartIndex == null) gapStartIndex = i
                    } else if (gapStartIndex != null) {
                        val gx1 = gapStartIndex * stepX
                        val gx2 = i * stepX
                        drawRect(
                            color = StatusUnavailable.copy(alpha = 0.25f),
                            topLeft = Offset(gx1, 0f),
                            size = Size(gx2 - gx1, height)
                        )
                        gapStartIndex = null
                    }
                }
                if (gapStartIndex != null) {
                    val gx1 = gapStartIndex * stepX
                    drawRect(
                        color = StatusUnavailable.copy(alpha = 0.25f),
                        topLeft = Offset(gx1, 0f),
                        size = Size(width - gx1, height)
                    )
                }

                // 4. Plot Telemetry Segments (Solid Cyan for Live/Synced, Dashed for Estimated, Breaking on Gap)
                var currentPath = Path()
                var pathHasPoints = false
                var isSegmentEstimated = false

                for (i in points.indices) {
                    val pt = points[i]
                    val px = i * stepX

                    if (pt.isDisconnected || pt.value == null) {
                        if (pathHasPoints) {
                            drawPath(
                                path = currentPath,
                                color = BorderActive,
                                style = Stroke(
                                    width = 2.5f.dp.toPx(),
                                    pathEffect = if (isSegmentEstimated) dottedEffect else null
                                )
                            )
                            currentPath = Path()
                            pathHasPoints = false
                        }
                    } else {
                        val py = getY(pt.value)
                        if (!pathHasPoints) {
                            currentPath.moveTo(px, py)
                            pathHasPoints = true
                            isSegmentEstimated = pt.isEstimated
                        } else {
                            currentPath.lineTo(px, py)
                        }

                        // Point circle
                        val ptColor = if (pt.anomalyRule != null) StatusCritical
                        else if (pt.isEstimated) StatusMonitor
                        else BorderActive

                        drawCircle(
                            color = ptColor,
                            radius = 3.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }

                if (pathHasPoints) {
                    drawPath(
                        path = currentPath,
                        color = BorderActive,
                        style = Stroke(
                            width = 2.5f.dp.toPx(),
                            pathEffect = if (isSegmentEstimated) dottedEffect else null
                        )
                    )
                }
            }
        }

        // Legend Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendItem(color = BorderActive, label = "Nominal Telemetry")
                LegendItem(color = StatusNormal.copy(alpha = 0.5f), label = "Personal Baseline")
                LegendItem(color = StatusUnavailable, label = "[GAP / NO DATA]")
                LegendItem(color = StatusCritical, label = "Anomaly Spike")
            }

            Text(
                text = "Tap to scrub",
                color = TextSecondary.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 2.dp)
                .background(color, RoundedCornerShape(2.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
