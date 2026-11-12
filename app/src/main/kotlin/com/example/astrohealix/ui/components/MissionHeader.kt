package com.example.astrohealix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.model.AstronautProfile
import com.example.astrohealix.model.MissionEnvironment
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusCriticalBg
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusMonitorBg
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun MissionHeader(
    astronaut: AstronautProfile,
    environment: MissionEnvironment,
    readinessScore: Int,
    isOffline: Boolean,
    pendingSyncCount: Int = 0,
    isDemoData: Boolean = true,
    modifier: Modifier = Modifier
) {
    val borderColor = BorderSubtle

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(BgSurface)
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        if (maxWidth >= 520.dp) {
            // Wide / tablet layout: single 56dp row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AstronautNameText(
                        name = astronaut.name,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    MissionDayBadge(missionDay = astronaut.missionDay)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EnvironmentPill(label = environment.shortName)
                    if (isDemoData) {
                        DemoDataPill()
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isOffline) {
                        OfflinePill(pendingSyncCount = pendingSyncCount)
                    }
                    ReadinessValue(readinessScore = readinessScore)
                }
            }
        } else {
            // Narrow phone layout (e.g. 360dp): compact two-row header so nothing clips
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AstronautNameText(
                            name = astronaut.name,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        MissionDayBadge(missionDay = astronaut.missionDay)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isOffline) {
                            OfflinePill(pendingSyncCount = pendingSyncCount)
                        }
                        ReadinessValue(readinessScore = readinessScore)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    EnvironmentPill(label = environment.shortName)
                    if (isDemoData) {
                        DemoDataPill()
                    }
                }
            }
        }
    }
}

@Composable
private fun AstronautNameText(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        color = TextPrimary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
private fun MissionDayBadge(missionDay: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(BgSurfaceHover)
            .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "DAY $missionDay",
            color = BorderActive,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun EnvironmentPill(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(BgSurfaceHover)
            .border(1.dp, BorderSubtle, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun DemoDataPill() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(StatusMonitorBg)
            .border(1.dp, StatusMonitor.copy(alpha = 0.6f), RoundedCornerShape(50))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "DEMO DATA",
            color = StatusMonitor,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun OfflinePill(pendingSyncCount: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(StatusCriticalBg)
            .border(1.dp, StatusCritical.copy(alpha = 0.7f), RoundedCornerShape(50))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = "Offline",
            tint = StatusCritical,
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = if (pendingSyncCount > 0) "OFFLINE ($pendingSyncCount)" else "OFFLINE",
            color = StatusCritical,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun ReadinessValue(readinessScore: Int) {
    val readinessColor = when {
        readinessScore >= 80 -> StatusNormal
        readinessScore >= 60 -> StatusMonitor
        else -> StatusCritical
    }
    Text(
        text = "$readinessScore%",
        color = readinessColor,
        fontSize = 14.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        softWrap = false
    )
}
