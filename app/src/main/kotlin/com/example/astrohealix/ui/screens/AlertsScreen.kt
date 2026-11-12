package com.example.astrohealix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.astrohealix.data.AstroUiState
import com.example.astrohealix.model.VitalStatus
import com.example.astrohealix.ui.components.AlertBanner
import com.example.astrohealix.ui.theme.BgPrimary
import com.example.astrohealix.ui.theme.BgSurface
import com.example.astrohealix.ui.theme.BorderSubtle
import com.example.astrohealix.ui.theme.StatusNormal
import com.example.astrohealix.ui.theme.TextSecondary

@Composable
fun AlertsScreen(
    state: AstroUiState,
    onAcknowledge: (String) -> Unit,
    onEscalate: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeAlerts = remember(state.alerts) {
        state.alerts
            .filter { !it.isAcknowledged }
            .sortedBy { if (it.severity == VitalStatus.CRITICAL) 0 else 1 }
    }
    val acknowledgedAlerts = remember(state.alerts) {
        state.alerts.filter { it.isAcknowledged }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        if (onBack != null) {
            SecondaryTopBar(title = "ALERTS", onBack = onBack)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgPrimary),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "active-alerts-header") {
                Text(
                    text = "ACTIVE ALERTS (${activeAlerts.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
            }

            if (activeAlerts.isEmpty()) {
                item(key = "no-active-alerts") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BgSurface)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusNormal,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "No unacknowledged alerts. Telemetry parameters within thresholds.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(
                    items = activeAlerts,
                    key = { "active-alert-${it.id}" }
                ) { alert ->
                    AlertBanner(
                        alert = alert,
                        onAcknowledge = onAcknowledge,
                        onEscalate = onEscalate
                    )
                }
            }

            if (acknowledgedAlerts.isNotEmpty()) {
                item(key = "acknowledged-alerts-header") {
                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = "ACKNOWLEDGED (${acknowledgedAlerts.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                    }
                }

                items(
                    items = acknowledgedAlerts,
                    key = { "ack-alert-${it.id}" }
                ) { alert ->
                    AlertBanner(
                        alert = alert,
                        onAcknowledge = onAcknowledge,
                        onEscalate = onEscalate,
                        modifier = Modifier.alpha(0.6f)
                    )
                }
            }

            item(key = "alerts-footer") {
                Text(
                    text = "Escalated alerts transmit telemetry snapshots to Flight Surgeon queue.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
