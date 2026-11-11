package com.example.astrohealix.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.model.AstronautProfile
import com.example.astrohealix.model.MissionEnvironment
import com.example.astrohealix.ui.theme.GlassCardBorderBrush
import com.example.astrohealix.ui.theme.GlassCardFillBrush
import com.example.astrohealix.ui.theme.TextPrimary
import com.example.astrohealix.ui.theme.TextSecondary
import com.example.astrohealix.ui.theme.TextTertiary

@Composable
fun MissionHeader(
    astronaut: AstronautProfile,
    environment: MissionEnvironment,
    isOffline: Boolean,
    pendingSyncCount: Int,
    lastSyncedAt: String,
    onCycleEnvironment: () -> Unit,
    onOpenSimulator: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(GlassCardFillBrush)
            .border(1.dp, GlassCardBorderBrush, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Group: Brand + Astro Identification + Vital Cabin Telemetry (Monochrome Starkness)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // VITA-SYS Monochrome Badge
                Text(
                    text = "VITA-SYS",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .background(TextTertiary, RoundedCornerShape(1.dp))
                )

                Text(
                    text = "${astronaut.id} (${astronaut.name})",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                Text(
                    text = "• DAY ${astronaut.missionDay}",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )

                // Battery Indicator (Frosted White)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = "Battery",
                        tint = TextPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${astronaut.batteryPercent}%",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Text(
                    text = "O2 ${astronaut.habitatO2Percent}% • CO2 ${astronaut.cabinCo2MmHg} mmHg",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Group: Environment + Sync Pill + Simulator Button (Frosted Monochrome Glass)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Environment Switcher Chip (Frosted Glass)
                Row(
                    modifier = Modifier
                        .background(Color(0x14FFFFFF), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                        .clickable { onCycleEnvironment() }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Environment",
                        tint = TextPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${environment.shortName} (${environment.gravityLabel})",
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "• ${environment.commDelayLabel}",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Sync Status Pill (Frosted Glass)
                if (isOffline) {
                    Row(
                        modifier = Modifier
                            .background(Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(10.dp))
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline Cache",
                            tint = TextPrimary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (pendingSyncCount > 0) "OFFLINE ($pendingSyncCount)" else "OFFLINE",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .background(Color(0x18FFFFFF), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(10.dp))
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Synced",
                            tint = TextPrimary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "COMM LINK SYNCED",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Simulator Controls Trigger (Frosted Glass Button)
                Row(
                    modifier = Modifier
                        .background(Color(0x18FFFFFF), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(8.dp))
                        .clickable { onOpenSimulator() }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Sim Controls",
                        tint = TextPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "SIM CONTROLS",
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
