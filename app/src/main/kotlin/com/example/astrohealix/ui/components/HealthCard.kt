package com.example.astrohealix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.model.VitalCardSnapshot
import com.example.astrohealix.model.VitalId
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.CriticalSurfaceGlow
import com.example.astrohealix.ui.theme.GlassCardBorderBrush
import com.example.astrohealix.ui.theme.GlassCardFillBrush
import com.example.astrohealix.ui.theme.MonitorSurfaceGlow
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.StatusUnavailable
import com.example.astrohealix.ui.theme.TextDisabled
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun HealthCard(
    snapshot: VitalCardSnapshot,
    onTapCard: (VitalId) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (snapshot.status) {
        VitalStatus.NORMAL -> StatusNormal
        VitalStatus.MONITOR -> StatusMonitor
        VitalStatus.CRITICAL -> StatusCritical
        VitalStatus.UNAVAILABLE -> StatusUnavailable
    }

    val cardBg = when (snapshot.status) {
        VitalStatus.CRITICAL -> CriticalSurfaceGlow
        VitalStatus.MONITOR -> MonitorSurfaceGlow
        else -> BgSurface
    }

    val iconVector: ImageVector = when (snapshot.id) {
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

    val cardFillBrush: Brush = when (snapshot.status) {
        VitalStatus.CRITICAL -> Brush.verticalGradient(listOf(CriticalSurfaceGlow, Color(0x221A0A10)))
        VitalStatus.MONITOR -> Brush.verticalGradient(listOf(MonitorSurfaceGlow, Color(0x221A1408)))
        else -> GlassCardFillBrush
    }

    val cardBorderBrush: Brush = when (snapshot.status) {
        VitalStatus.CRITICAL -> SolidColor(StatusCritical.copy(alpha = 0.6f))
        VitalStatus.MONITOR -> SolidColor(StatusMonitor.copy(alpha = 0.5f))
        else -> GlassCardBorderBrush
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 112.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(cardFillBrush)
            .border(1.dp, cardBorderBrush, RoundedCornerShape(8.dp))
            .clickable { onTapCard(snapshot.id) }
    ) {
        // Left 3.5dp solid status stripe
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(3.5.dp)
                .matchParentSize()
                .background(statusColor)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 8.dp, end = 10.dp, bottom = 8.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = snapshot.title,
                        tint = if (snapshot.status == VitalStatus.UNAVAILABLE) TextDisabled else BorderActive,
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

            // Metric Value Display (with plenty of room so numbers never clip)
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

            // Footer: Baseline Delta + Sync Status / Source
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = snapshot.deltaFromBaseline,
                    color = if (snapshot.status == VitalStatus.CRITICAL) StatusCritical
                    else if (snapshot.status == VitalStatus.MONITOR) StatusMonitor
                    else TextSecondary,
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
                            tint = StatusMonitor,
                            modifier = Modifier.size(10.dp)
                        )
                    }

                    Text(
                        text = if (snapshot.status == VitalStatus.UNAVAILABLE) "[OFFLINE]" else snapshot.lastUpdated,
                        color = TextSecondary.copy(alpha = 0.6f),
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
