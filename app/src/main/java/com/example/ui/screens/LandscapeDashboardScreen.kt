package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArrivalSafetyStatus
import com.example.model.DrivingMode
import com.example.ui.components.BatteryGauge
import com.example.ui.theme.EvBatteryAmber
import com.example.ui.theme.EvBatteryGreen
import com.example.ui.theme.EvBatteryRed
import com.example.ui.theme.EvCyanPrimary
import com.example.ui.theme.EvElectricBlue
import com.example.viewmodel.BydNavUiState
import com.example.viewmodel.BydNavViewModel

/**
 * Dedicated Widescreen / Landscape Cockpit Screen (แนวนอน)
 * Perfectly optimized for car dashboard tablet mount & BYD DiLink landscape screen.
 */
@Composable
fun LandscapeDashboardScreen(
    viewModel: BydNavViewModel,
    uiState: BydNavUiState,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val route = uiState.routeCalculation

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // LEFT PANE (Instrument Cluster & Battery Telemetry) - 45% width
        Card(
            modifier = Modifier
                .weight(0.95f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header with Car model and Sync button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = uiState.carModel.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${uiState.carModel.batteryCapacityKwh} kWh Blade Battery",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }

                        FilledTonalButton(
                            onClick = { viewModel.refreshBatteryNow() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync", modifier = Modifier.size(14.dp), tint = EvCyanPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ซิงค์", fontSize = 11.sp, color = EvCyanPrimary)
                        }
                    }
                }

                // Battery Gauge (Compact in Landscape)
                item {
                    BatteryGauge(
                        socPercent = uiState.batteryData.socPercent,
                        estimatedRangeKm = uiState.batteryData.estimatedRangeKm,
                        isCharging = uiState.batteryData.isCharging,
                        sizeDp = 175
                    )
                }

                // Telemetry Quick Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TelemetryCompactChip(
                            title = "แรงดัน Pack",
                            value = "${String.format("%.1f", uiState.batteryData.voltageVolts)} V",
                            icon = Icons.Default.ElectricBolt,
                            iconColor = EvCyanPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryCompactChip(
                            title = "อุณหภูมิ Blade",
                            value = "${String.format("%.1f", uiState.batteryData.temperatureCelsius)} °C",
                            icon = Icons.Default.Thermostat,
                            iconColor = EvBatteryGreen,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryCompactChip(
                            title = "สุขภาพ SoH",
                            value = "${uiState.batteryData.stateOfHealthPercent}%",
                            icon = Icons.Default.Favorite,
                            iconColor = EvBatteryGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // RIGHT PANE (Navigation, Arrival Battery Remaining & Google Maps Launch) - 55% width
        Card(
            modifier = Modifier
                .weight(1.15f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ระบบนำทาง & ประเมินแบตเตอรี่ปลายทาง",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        FilledTonalButton(
                            onClick = { onNavigateToTab(1) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("เปลี่ยนจุดหมาย", fontSize = 11.sp, color = EvCyanPrimary)
                        }
                    }
                }

                // Primary Arrival Battery Card
                item {
                    if (route != null) {
                        val arrivalColor = when (route.safetyStatus) {
                            ArrivalSafetyStatus.SAFE -> EvBatteryGreen
                            ArrivalSafetyStatus.MODERATE -> EvCyanPrimary
                            ArrivalSafetyStatus.LOW_WARNING -> EvBatteryAmber
                            ArrivalSafetyStatus.CRITICAL_UNREACHABLE -> EvBatteryRed
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Navigation,
                                            contentDescription = null,
                                            tint = arrivalColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = route.destination.nameThai,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "${String.format("%.1f", route.distanceKm)} กม.",
                                        fontWeight = FontWeight.Bold,
                                        color = EvCyanPrimary,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = "แบตเตอรี่คงเหลือ ณ จุดหมาย",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = "${String.format("%.1f", route.arrivalSocPercent)}%",
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = arrivalColor
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(-${String.format("%.1f", route.consumedSocPercent)}%)",
                                                fontSize = 12.sp,
                                                color = Color(0xFF94A3B8),
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = route.safetyStatus.labelThai,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = arrivalColor
                                        )
                                        Text(
                                            text = "ใช้ไฟ ~${String.format("%.1f", route.consumedKwh)} kWh",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { (route.arrivalSocPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = arrivalColor,
                                    trackColor = Color(0xFF334155)
                                )
                            }
                        }
                    }
                }

                // CTA Button: Launch Google Maps
                item {
                    if (route != null) {
                        Button(
                            onClick = {
                                viewModel.launchGoogleMapsNavigation(context, route.destination)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("landscape_launch_maps_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EvCyanPrimary,
                                contentColor = Color(0xFF00363D)
                            )
                        ) {
                            Icon(Icons.Default.Map, contentDescription = "Maps", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "เริ่มนำทางด้วย Google Maps ตอนนี้",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Driving Mode Quick Switcher
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DrivingMode.entries.forEach { mode ->
                            val isSelected = uiState.drivingMode == mode
                            FilledTonalButton(
                                onClick = { viewModel.setDrivingMode(mode) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) EvCyanPrimary else Color(0xFF1E293B),
                                    contentColor = if (isSelected) Color(0xFF00363D) else Color(0xFF94A3B8)
                                )
                            ) {
                                Text(mode.name, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryCompactChip(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E293B))
            .padding(8.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = title, fontSize = 9.sp, color = Color(0xFF94A3B8))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
