package com.example.astrohealix.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.ui.theme.AuraCardPure
import com.example.astrohealix.ui.theme.AuraElectricBlue
import com.example.astrohealix.ui.theme.AuraInkPrimary
import com.example.astrohealix.ui.theme.AuraInkSecondary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.LocalVitaColors

enum class DockTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    TRENDS("Vitals", Icons.Default.ShowChart),
    BODY_SCAN("Body Scan", Icons.Default.AccessibilityNew),
    PROTOCOLS("Protocols", Icons.Default.FactCheck),
    AI_COACH("AI Coach", Icons.Default.AutoAwesome)
}

/**
 * Ultra-Smooth Floating Glass Pill Navigation Bar taken directly from Aura Health design scheme.
 * Features animated tab expansion, dynamic icon tinting, and dedicated quick log launcher.
 */
@Composable
fun FloatingDockBar(
    current: DockTab,
    onSelect: (DockTab) -> Unit,
    onQuickLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalVitaColors.current.isDark
    val dockContainerColor = if (isDark) BgSurface else AuraCardPure
    val dockBorderColor = if (isDark) BorderSubtle else Color(0xFFE2E8F0)
    val activePillBg = if (isDark) Color(0xFF2A354D) else AuraInkPrimary
    val inactiveIconTint = if (isDark) Color(0xFF8E9BAE) else AuraInkSecondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = dockContainerColor,
            shape = RoundedCornerShape(50),
            shadowElevation = 10.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, dockBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockTab.entries.forEach { item ->
                    val sel = current == item
                    val pillBg by animateColorAsState(
                        if (sel) activePillBg else Color.Transparent,
                        label = "DockPillBg"
                    )
                    val iconTint by animateColorAsState(
                        if (sel) Color.White else inactiveIconTint,
                        label = "DockIconTint"
                    )

                    Surface(
                        onClick = { onSelect(item) },
                        color = pillBg,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                tint = iconTint,
                                modifier = Modifier.size(19.dp)
                            )
                            AnimatedVisibility(visible = sel) {
                                Row {
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = item.title,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick + Log Pill Button
                FilledIconButton(
                    onClick = onQuickLog,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = AuraElectricBlue),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Quick Log Vital",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
