package com.example.astrohealix.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.model.HealthAlert
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceHover
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.CriticalSurfaceGlow
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.StatusUnavailable
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun AlertBanner(
    alert: HealthAlert,
    onAcknowledge: (String) -> Unit,
    onEscalate: (String) -> Unit,
    modifier: Modifier = Modifier,
    currentIndex: Int = 0,
    totalCount: Int = 1,
    onNextAlert: (() -> Unit)? = null,
    onPrevAlert: (() -> Unit)? = null
) {
    val isCritical = alert.severity == VitalStatus.CRITICAL
    val accentColor = when (alert.severity) {
        VitalStatus.CRITICAL -> StatusCritical
        VitalStatus.MONITOR -> StatusMonitor
        VitalStatus.NORMAL -> StatusNormal
        VitalStatus.UNAVAILABLE -> StatusUnavailable
    }
    val containerBg = if (isCritical) CriticalSurfaceGlow else BgSurface
    val borderColor = if (isCritical && !alert.isAcknowledged) {
        StatusCritical.copy(alpha = 0.55f)
    } else {
        BorderSubtle
    }
    val cardShape = RoundedCornerShape(8.dp)
    val priorityLabel = when (alert.severity) {
        VitalStatus.CRITICAL -> "CRITICAL"
        VitalStatus.MONITOR -> "MONITOR"
        else -> alert.severity.name
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (alert.isAcknowledged) 0.65f else 1f)
            .clip(cardShape)
            .background(containerBg, cardShape)
            .drawBehind {
                drawRect(
                    color = accentColor,
                    size = Size(6.dp.toPx(), size.height)
                )
            }
            .border(1.dp, borderColor, cardShape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Icon + Priority Pill + Timestamp + Optional Pager
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = priorityLabel,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    StatusPill(
                        status = alert.severity,
                        compact = true,
                        customLabel = priorityLabel
                    )
                    Text(
                        text = alert.triggeredAt,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                if (totalCount > 1 && (onPrevAlert != null || onNextAlert != null)) {
                    val displayIndex = (currentIndex + 1).coerceIn(1, totalCount)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BgSurfaceHover)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .clickable(enabled = onPrevAlert != null) { onPrevAlert?.invoke() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous alert",
                                tint = TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "$displayIndex / $totalCount",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .clickable(enabled = onNextAlert != null) { onNextAlert?.invoke() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next alert",
                                tint = TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Alert Title (16sp Bold) — cleanly stripped of redundant status prefixes
            val cleanTitle = alert.title
                .removePrefix("MONITOR: ")
                .removePrefix("ATTENTION REQUIRED: ")
                .removePrefix("CRITICAL: ")
                .trim()

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = cleanTitle,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                )
            }

            // Body: triggerEvent + guidance
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = alert.triggerEvent,
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    fontFamily = FontFamily.SansSerif
                )

                if (alert.guidance.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(BgSurfaceHover)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = alert.guidance,
                            color = TextSecondary,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }

            // Acknowledged / Escalated status details
            if (alert.isAcknowledged) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Acknowledged",
                        tint = StatusNormal,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Acknowledged: ${alert.acknowledgedAt ?: "Recorded"}",
                        color = StatusNormal,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (alert.isEscalated) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(BgSurfaceHover)
                        .border(1.dp, BorderActive.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Escalated",
                            tint = BorderActive,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Escalated to Flight Surgeon • ${alert.escalatedAt ?: "Logged"}",
                            color = BorderActive,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!alert.escalationNote.isNullOrBlank()) {
                        Text(
                            text = alert.escalationNote,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }

            // Action Strip (48dp minimum touch targets)
            if (!alert.isAcknowledged || !alert.isEscalated) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val sideBySide = maxWidth >= 480.dp && !alert.isAcknowledged && !alert.isEscalated
                    if (sideBySide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AcknowledgeCta(
                                onClick = { onAcknowledge(alert.id) },
                                modifier = Modifier.weight(1f)
                            )
                            EscalateCta(
                                isCritical = isCritical,
                                onClick = { onEscalate(alert.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!alert.isAcknowledged) {
                                AcknowledgeCta(
                                    onClick = { onAcknowledge(alert.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (!alert.isEscalated) {
                                EscalateCta(
                                    isCritical = isCritical,
                                    onClick = { onEscalate(alert.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AcknowledgeCta(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BorderActive,
            contentColor = BgPrimary
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        modifier = modifier.defaultMinSize(minHeight = 48.dp)
    ) {
        Text(
            text = "Acknowledge & Record",
            fontSize = 13.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun EscalateCta(
    isCritical: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (isCritical) StatusCritical else BorderSubtle
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accent),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isCritical) StatusCritical else TextPrimary
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        modifier = modifier.defaultMinSize(minHeight = 48.dp)
    ) {
        Text(
            text = "Escalate to Flight Surgeon",
            fontSize = 13.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false
        )
    }
}
