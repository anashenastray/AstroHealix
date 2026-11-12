package com.example.astrohealix.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.model.DataQuality
import com.example.astrohealix.model.DataSource
import com.example.astrohealix.model.VitalCardSnapshot
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusCriticalBg
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusMonitorBg
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.StatusUnavailable
import com.example.astrohealix.ui.theme.StatusUnavailableBg
import com.example.astrohealix.ui.theme.TextDisabled
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun HealthCard(
    snapshot: VitalCardSnapshot,
    onTapCard: (VitalId) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnavailable = snapshot.status == VitalStatus.UNAVAILABLE
    val statusColor = when (snapshot.status) {
        VitalStatus.NORMAL -> StatusNormal
        VitalStatus.MONITOR -> StatusMonitor
        VitalStatus.CRITICAL -> StatusCritical
        VitalStatus.UNAVAILABLE -> StatusUnavailable
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "healthCardScale"
    )

    val cardShape = RoundedCornerShape(8.dp)
    val iconVector = getVitalIcon(snapshot.id)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 128.dp)
            .scale(pressScale)
            .alpha(if (isUnavailable) 0.6f else 1f)
            .clip(cardShape)
            .background(BgSurface, cardShape)
            .border(1.dp, BorderSubtle, cardShape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ) { onTapCard(snapshot.id) }
    ) {
        // 4px solid status color left border accent
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(4.dp)
                .matchParentSize()
                .background(statusColor)
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            val isWide = maxWidth >= 230.dp

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header + Top-right StatusPill
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = snapshot.title,
                                    tint = if (isUnavailable) TextDisabled else statusColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = snapshot.title,
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            CardMetadataBadges(snapshot = snapshot)
                        }

                        StatusPill(status = snapshot.status)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = snapshot.title,
                                tint = if (isUnavailable) TextDisabled else statusColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                StatusPill(
                                    status = snapshot.status,
                                    compact = true
                                )
                            }
                        }

                        Text(
                            text = snapshot.title,
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        CardMetadataBadges(snapshot = snapshot)
                    }
                }

                // Value Display
                if (isUnavailable || snapshot.value == null) {
                    Text(
                        text = "---",
                        color = TextDisabled,
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp,
                        maxLines = 1
                    )
                } else {
                    val metricFontSize = if (snapshot.displayValue.length > 6) 24.sp else 32.sp
                    val metricLineHeight = if (snapshot.displayValue.length > 6) 28.sp else 36.sp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = snapshot.displayValue,
                            color = TextPrimary,
                            fontSize = metricFontSize,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            lineHeight = metricLineHeight,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (snapshot.unit.isNotBlank()) {
                            Text(
                                text = snapshot.unit,
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }

                // Footer: Trend Delta + Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = snapshot.deltaFromBaseline,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (snapshot.isPendingSync) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Pending Sync",
                                tint = BorderActive,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = snapshot.lastUpdated,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
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
private fun CardMetadataBadges(snapshot: VitalCardSnapshot) {
    val isSelfReported = snapshot.id == VitalId.MOOD_INDEX ||
        (snapshot.id == VitalId.SLEEP_HOURS && snapshot.source == DataSource.MANUAL)
    val hasQualityWarning = snapshot.qualityStatus != DataQuality.VALID

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TinyTagBadge(
                label = "[${snapshot.source.name}]",
                textColor = TextSecondary,
                bgColor = BgSurfaceHover,
                borderColor = BorderSubtle
            )
            if (isSelfReported) {
                TinyTagBadge(
                    label = "[SELF-REPORTED]",
                    textColor = BorderActive,
                    bgColor = BgSurfaceHover,
                    borderColor = BorderActive.copy(alpha = 0.5f)
                )
            }
        }

        if (hasQualityWarning) {
            val (qColor, qBg) = when (snapshot.qualityStatus) {
                DataQuality.INVALID -> StatusCritical to StatusCriticalBg
                DataQuality.INCOMPLETE -> StatusUnavailable to StatusUnavailableBg
                DataQuality.OUTDATED, DataQuality.UNVERIFIED -> StatusMonitor to StatusMonitorBg
                DataQuality.VALID -> TextSecondary to BgSurfaceHover
            }
            TinyTagBadge(
                label = "[${snapshot.qualityStatus.name}]",
                textColor = qColor,
                bgColor = qBg,
                borderColor = qColor
            )
        }
    }
}

@Composable
private fun TinyTagBadge(
    label: String,
    textColor: Color,
    bgColor: Color,
    borderColor: Color
) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(bgColor, shape)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

fun getVitalIcon(vitalId: VitalId): ImageVector = when (vitalId) {
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
