package com.example.astrohealix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusCriticalBg
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusMonitorBg
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.StatusNormalBg
import com.example.astrohealix.ui.theme.StatusUnavailable
import com.example.astrohealix.ui.theme.StatusUnavailableBg

@Composable
fun StatusPill(
    status: VitalStatus,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    customLabel: String? = null
) {
    val defaultLabel = when (status) {
        VitalStatus.NORMAL -> "NORMAL"
        VitalStatus.MONITOR -> "MONITOR"
        VitalStatus.CRITICAL -> "ATTENTION REQUIRED"
        VitalStatus.UNAVAILABLE -> "DATA UNAVAILABLE"
    }

    val style = when (status) {
        VitalStatus.NORMAL -> StatusPillStyle(
            fgColor = StatusNormal,
            bgColor = StatusNormalBg,
            icon = Icons.Default.CheckCircle
        )
        VitalStatus.MONITOR -> StatusPillStyle(
            fgColor = StatusMonitor,
            bgColor = StatusMonitorBg,
            icon = Icons.Default.WarningAmber
        )
        VitalStatus.CRITICAL -> StatusPillStyle(
            fgColor = StatusCritical,
            bgColor = StatusCriticalBg,
            icon = Icons.Default.Warning
        )
        VitalStatus.UNAVAILABLE -> StatusPillStyle(
            fgColor = StatusUnavailable,
            bgColor = StatusUnavailableBg,
            icon = Icons.Default.Close
        )
    }

    val displayLabel = (customLabel ?: defaultLabel).uppercase()
    val pillShape = RoundedCornerShape(50)

    Row(
        modifier = modifier
            .clip(pillShape)
            .background(style.bgColor, pillShape)
            .border(1.dp, style.fgColor.copy(alpha = 0.35f), pillShape)
            .padding(
                horizontal = if (compact) 7.dp else 10.dp,
                vertical = if (compact) 2.dp else 4.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 5.dp)
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = displayLabel,
            tint = style.fgColor,
            modifier = Modifier.size(if (compact) 10.dp else 12.dp)
        )
        Text(
            text = displayLabel,
            color = style.fgColor,
            fontSize = if (compact) 9.5.sp else 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = if (compact) 0.03.em else 0.05.em,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private data class StatusPillStyle(
    val fgColor: Color,
    val bgColor: Color,
    val icon: ImageVector
)
