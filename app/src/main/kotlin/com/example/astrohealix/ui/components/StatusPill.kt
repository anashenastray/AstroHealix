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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val fullLabel = when (status) {
        VitalStatus.NORMAL -> "NORMAL"
        VitalStatus.MONITOR -> "MONITOR"
        VitalStatus.CRITICAL -> "CRITICAL"
        VitalStatus.UNAVAILABLE -> "NO DATA"
    }

    val compactLabel = when (status) {
        VitalStatus.NORMAL -> "NORM"
        VitalStatus.MONITOR -> "WARN"
        VitalStatus.CRITICAL -> "CRIT"
        VitalStatus.UNAVAILABLE -> "N/A"
    }

    val fgColor = when (status) {
        VitalStatus.NORMAL -> StatusNormal
        VitalStatus.MONITOR -> StatusMonitor
        VitalStatus.CRITICAL -> StatusCritical
        VitalStatus.UNAVAILABLE -> StatusUnavailable
    }

    val bgColor = when (status) {
        VitalStatus.NORMAL -> StatusNormalBg
        VitalStatus.MONITOR -> StatusMonitorBg
        VitalStatus.CRITICAL -> StatusCriticalBg
        VitalStatus.UNAVAILABLE -> StatusUnavailableBg
    }

    val icon: ImageVector = when (status) {
        VitalStatus.NORMAL -> Icons.Default.CheckCircle
        VitalStatus.MONITOR -> Icons.Default.WarningAmber
        VitalStatus.CRITICAL -> Icons.Default.Warning
        VitalStatus.UNAVAILABLE -> Icons.Default.Close
    }

    val displayLabel = customLabel ?: if (compact) compactLabel else fullLabel

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(10.dp))
            .border(1.dp, fgColor.copy(alpha = 0.75f), RoundedCornerShape(10.dp))
            .padding(
                horizontal = if (compact) 5.dp else 8.dp,
                vertical = if (compact) 2.dp else 3.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = displayLabel,
            tint = fgColor,
            modifier = Modifier.size(if (compact) 9.dp else 11.dp)
        )
        Text(
            text = displayLabel,
            color = fgColor,
            fontSize = if (compact) 9.sp else 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            letterSpacing = 0.03.sp
        )
    }
}
