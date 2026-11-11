package com.example.astrohealix.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.model.VitalCardSnapshot
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.theme.GlassCardBorderBrush
import com.example.astrohealix.ui.theme.GlassCardFillBrush
import com.example.astrohealix.ui.theme.GlassElevatedBorderBrush
import com.example.astrohealix.ui.theme.GlassElevatedFillBrush
import com.example.astrohealix.ui.theme.TextDisabled
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary
import com.example.astrohealix.ui.theme.TextTertiary

@Composable
fun HealthCard(
    snapshot: VitalCardSnapshot,
    onTapCard: (VitalId) -> Unit,
    modifier: Modifier = Modifier
) {
    val iconVector: ImageVector = getVitalIcon(snapshot.id)

    // Apple Frosted Glass: Fill & Specular Border
    val cardFill: Brush = when (snapshot.status) {
        VitalStatus.CRITICAL -> GlassElevatedFillBrush
        VitalStatus.MONITOR -> GlassCardFillBrush
        else -> GlassCardFillBrush
    }

    val cardBorder: Brush = when (snapshot.status) {
        VitalStatus.CRITICAL -> GlassElevatedBorderBrush
        VitalStatus.MONITOR -> GlassCardBorderBrush
        else -> GlassCardBorderBrush
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 112.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(cardFill)
            .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
            .clickable { onTapCard(snapshot.id) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: [Icon + Short Title] ... [Status Pill]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = snapshot.title,
                        tint = if (snapshot.status == VitalStatus.UNAVAILABLE) TextDisabled else TextPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = snapshot.shortLabel,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                StatusPill(
                    status = snapshot.status,
                    compact = true
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Metric Value Display (Pure Stark White, Zero Color Tint)
            if (snapshot.status == VitalStatus.UNAVAILABLE) {
                Text(
                    text = "DATA UNAVAILABLE",
                    color = TextDisabled,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = snapshot.displayValue,
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )
                    Text(
                        text = snapshot.unit,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Footer: Baseline Delta + Sync Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = snapshot.deltaFromBaseline,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    if (snapshot.isPendingSync) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Pending Sync",
                            tint = TextSecondary,
                            modifier = Modifier.size(10.dp)
                        )
                    }

                    Text(
                        text = if (snapshot.status == VitalStatus.UNAVAILABLE) "[OFFLINE]" else snapshot.lastUpdated,
                        color = TextTertiary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

/**
 * HeroHealthCard:
 * Spacious, beautifully detailed Apple VisionOS frosted glass card
 * designed for focused 2 or 3 cards presentation.
 */
@Composable
fun HeroHealthCard(
    snapshot: VitalCardSnapshot,
    onTapCard: (VitalId) -> Unit,
    onOpenQuickLog: ((VitalId) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val iconVector: ImageVector = getVitalIcon(snapshot.id)

    val cardFill: Brush = when (snapshot.status) {
        VitalStatus.CRITICAL -> GlassElevatedFillBrush
        VitalStatus.MONITOR -> GlassCardFillBrush
        else -> GlassCardFillBrush
    }

    val cardBorder: Brush = when (snapshot.status) {
        VitalStatus.CRITICAL -> GlassElevatedBorderBrush
        VitalStatus.MONITOR -> GlassCardBorderBrush
        else -> GlassCardBorderBrush
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardFill)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP HEADER: [Icon Tile + Title + Source] ... [Status Pill]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x35FFFFFF))
                            .border(1.dp, Color(0x65FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = snapshot.title,
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = snapshot.title,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "[${snapshot.source.name}] • ${snapshot.lastUpdated}",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                StatusPill(status = snapshot.status, compact = true)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. METRIC VALUE READOUT & DELTA PILL
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (snapshot.status == VitalStatus.UNAVAILABLE) {
                    Text(
                        text = "DATA UNAVAILABLE",
                        color = TextDisabled,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Telemetry stream disconnected. Check sensor.",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 13.sp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = snapshot.displayValue,
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 34.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = snapshot.unit,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 3.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Delta capsule on its own dedicated row (never wraps into single letters!)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x35FFFFFF))
                                .border(1.dp, Color(0x65FFFFFF), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = snapshot.deltaFromBaseline,
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        if (snapshot.deltaPercent != null && snapshot.deltaPercent != 0f) {
                            Text(
                                text = if (snapshot.deltaPercent > 0) "+${snapshot.deltaPercent.toInt()}%" else "${snapshot.deltaPercent.toInt()}%",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. PERSONAL NOMINAL BASELINE GAUGE
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Baseline: ${snapshot.baseline.nominalMin.toInt()}–${snapshot.baseline.nominalMax.toInt()} ${snapshot.unit}",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = if (snapshot.status == VitalStatus.NORMAL) "NOMINAL" else "OBSERVE",
                        color = TextTertiary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Visual Range Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0x25FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight()
                            .align(Alignment.Center)
                            .background(Color(0x55FFFFFF))
                    )
                    val progress = when {
                        snapshot.value == null -> 0.5f
                        snapshot.baseline.nominalMax <= snapshot.baseline.nominalMin -> 0.5f
                        else -> {
                            val span = snapshot.baseline.nominalMax * 1.5f
                            (snapshot.value / span).coerceIn(0.05f, 0.95f)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = (progress * 180).dp.coerceAtLeast(0.dp))
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(TextPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 4. MINI LIVE TELEMETRY SPARKLINE WAVEFORM
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x18FFFFFF))
                    .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val points = listOf(
                        0.5f, 0.48f, 0.52f, 0.49f, 0.53f, 0.47f,
                        0.55f, 0.45f, 0.62f, 0.38f,
                        if (snapshot.status == VitalStatus.CRITICAL) 0.15f else if (snapshot.status == VitalStatus.MONITOR) 0.28f else 0.46f,
                        0.5f, 0.52f, 0.48f, 0.51f
                    )

                    val path = Path()
                    points.forEachIndexed { i, yRatio ->
                        val x = (i.toFloat() / (points.size - 1)) * w
                        val y = yRatio * h
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawLine(
                        color = Color(0x35FFFFFF),
                        start = Offset(0f, h * 0.5f),
                        end = Offset(w, h * 0.5f),
                        strokeWidth = 1f
                    )

                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 1.8.dp.toPx())
                    )

                    val lastX = w
                    val lastY = points.last() * h
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 5. DIRECT TOUCH ACTION BUTTONS (Frosted Glass, Compact Zero-Wrap)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { onTapCard(snapshot.id) },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0x28FFFFFF),
                        contentColor = TextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55FFFFFF)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guide", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif, maxLines = 1, softWrap = false)
                }

                OutlinedButton(
                    onClick = { onOpenQuickLog?.invoke(snapshot.id) ?: onTapCard(snapshot.id) },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0x38FFFFFF),
                        contentColor = TextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x80FFFFFF)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Log", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}

private fun getVitalIcon(id: VitalId): ImageVector = when (id) {
    VitalId.HEART_RATE -> Icons.Default.Favorite
    VitalId.BLOOD_PRESSURE -> Icons.Default.Speed
    VitalId.SPO2 -> Icons.Default.WaterDrop
    VitalId.RESPIRATORY_RATE -> Icons.Default.Waves
    VitalId.BODY_TEMP -> Icons.Default.Thermostat
    VitalId.BLOOD_GLUCOSE -> Icons.Default.WaterDrop
    VitalId.SLEEP_HOURS -> Icons.Default.NightsStay
    VitalId.HYDRATION_ML -> Icons.Default.LocalDrink
    VitalId.RADIATION_MSV -> Icons.Default.Warning
    VitalId.MOOD_INDEX -> Icons.Default.Mood
    VitalId.BONE_EXERCISE_MIN -> Icons.Default.FitnessCenter
}
