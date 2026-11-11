package com.example.astrohealix.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.model.HealthAlert
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.theme.BgApp
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BgSurfaceElevated
import com.example.astrohealix.ui.theme.BorderActive
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.CriticalSurfaceGlow
import com.example.astrohealix.ui.theme.MonitorSurfaceGlow
import com.example.astrohealix.ui.theme.StatusCritical
import com.example.astrohealix.ui.theme.StatusMonitor
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun AlertBanner(
    alert: HealthAlert,
    onAcknowledge: (String) -> Unit,
    onEscalate: (String) -> Unit,
    modifier: Modifier = Modifier,
    currentIndex: Int = 1,
    totalCount: Int = 1,
    onNextAlert: (() -> Unit)? = null,
    onPrevAlert: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val borderColor = if (alert.severity == VitalStatus.CRITICAL) StatusCritical else StatusMonitor
    val cardBg = if (alert.severity == VitalStatus.CRITICAL) CriticalSurfaceGlow else MonitorSurfaceGlow

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardBg)
            .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
    ) {
        // Left 4dp solid status accent bar
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(4.dp)
                .matchParentSize()
                .background(borderColor)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 8.dp, end = 10.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Main Compact Strip Row (Always Visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Severity Icon + Badge + Title + Rule
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = if (alert.severity == VitalStatus.CRITICAL) Icons.Default.Warning else Icons.Default.NotificationsActive,
                        contentDescription = "Alert",
                        tint = borderColor,
                        modifier = Modifier.size(15.dp)
                    )

                    // Severity tag
                    Text(
                        text = if (alert.severity == VitalStatus.CRITICAL) "[CRITICAL ALERT]" else "[MONITOR ALERT]",
                        color = borderColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )

                    Text(
                        text = alert.title,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1,
                        softWrap = false
                    )

                    Text(
                        text = "• ${alert.ruleApplied}",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Multi-alert Pager + Quick Ack Button + Expand Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Pager counter if multiple alerts
                    if (totalCount > 1) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .background(BgApp, RoundedCornerShape(4.dp))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "<",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onPrevAlert?.invoke() }
                                    .padding(horizontal = 3.dp)
                            )
                            Text(
                                text = "$currentIndex / $totalCount",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ">",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onNextAlert?.invoke() }
                                    .padding(horizontal = 3.dp)
                            )
                        }
                    }

                    // Quick Acknowledge Status / Button
                    if (alert.isAcknowledged) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .background(BgApp, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = StatusNormal, modifier = Modifier.size(12.dp))
                            Text("ACKNOWLEDGED", color = StatusNormal, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onAcknowledge(alert.id) },
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = BgSurfaceElevated,
                                contentColor = BorderActive
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderActive),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 28.dp)
                        ) {
                            Text("Acknowledge", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Details Toggle button
                    Row(
                        modifier = Modifier
                            .background(BgApp, RoundedCornerShape(4.dp))
                            .clickable { expanded = !expanded }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (expanded) "Hide" else "Protocol",
                            color = BorderActive,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = BorderActive,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Expandable Protocol & Surgeon Escalation Drawer
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Trigger Event: ${alert.triggerEvent}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(BgApp)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "CLINICAL PROTOCOL: ${alert.guidance}",
                            color = BorderActive,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detected: ${alert.triggeredAt}",
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        if (alert.isEscalated) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = StatusMonitor, modifier = Modifier.size(12.dp))
                                Text("ESCALATED TO FLIGHT SURGEON", color = StatusMonitor, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onEscalate(alert.id) },
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = BgApp,
                                    contentColor = borderColor
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 28.dp)
                            ) {
                                Text("Escalate to Flight Surgeon", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
