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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary
import com.example.astrohealix.ui.theme.TextTertiary

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
        VitalStatus.MONITOR -> "MONITOR"
        VitalStatus.CRITICAL -> "CRIT"
        VitalStatus.UNAVAILABLE -> "N/A"
    }

    // Apple Frosted Glass Monochrome Styling (Zero neon color noise)
    val (fgColor, bgColor, borderColor, icon: ImageVector) = when (status) {
        VitalStatus.NORMAL -> Quad(
            TextPrimary,
            Color(0x14FFFFFF),
            Color(0x28FFFFFF),
            Icons.Default.CheckCircle
        )
        VitalStatus.MONITOR -> Quad(
            TextPrimary,
            Color(0x22FFFFFF),
            Color(0x40FFFFFF),
            Icons.Default.WarningAmber
        )
        VitalStatus.CRITICAL -> Quad(
            TextPrimary,
            Color(0x35FFFFFF),
            Color(0x70FFFFFF),
            Icons.Default.Warning
        )
        VitalStatus.UNAVAILABLE -> Quad(
            TextTertiary,
            Color(0x0CFFFFFF),
            Color(0x18FFFFFF),
            Icons.Default.Close
        )
    }

    val displayLabel = customLabel ?: if (compact) compactLabel else fullLabel

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(
                horizontal = if (compact) 6.dp else 8.dp,
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

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
