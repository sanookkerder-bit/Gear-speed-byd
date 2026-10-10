package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArrivalSafetyStatus
import com.example.ui.components.BatteryGauge
import com.example.ui.components.CockpitHeader
import com.example.ui.theme.EvBatteryAmber
import com.example.ui.theme.EvBatteryGreen
import com.example.ui.theme.EvBatteryRed
import com.example.ui.theme.EvCyanPrimary
import com.example.ui.theme.EvElectricBlue
import com.example.viewmodel.BydNavUiState
import com.example.viewmodel.BydNavViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: BydNavViewModel,
    uiState: BydNavUiState,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showManualSlider by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            CockpitHeader(
                carModel = uiState.carModel,
                adbStatus = uiState.adbStatus
            )
        }

        // Center Gauge Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("battery_gauge_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ระดับแบตเตอรี่รถยนต์ (SoC)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Quick ADB Sync button
                        FilledTonalButton(
                            onClick = { viewModel.refreshBatteryNow() },
                            modifier = Modifier.testTag("refresh_battery_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync",
                                modifier = Modifier.size(16.dp),
                                tint = EvCyanPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ซิงค์ ADB", fontSize = 12.sp, color = EvCyanPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gauge
                    BatteryGauge(
                        socPercent = uiState.batteryData.socPercent,
                        estimatedRangeKm = uiState.batteryData.estimatedRangeKm,
                        isCharging = uiState.batteryData.isCharging,
                        sizeDp = 210
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val timeStr = remember(uiState.batteryData.lastSyncTimestamp) {
                        SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            .format(Date(uiState.batteryData.lastSyncTimestamp))
                    }
                    Text(
                        text = "อัปเดตล่าสุด: $timeStr (ผ่าน ADB Wireless)",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        // Active Destination & Predicted Arrival Battery Card
        item {
            uiState.routeCalculation?.let { route ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("destination_preview_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (route.safetyStatus) {
                            ArrivalSafetyStatus.SAFE -> Color(0xFF0D251E)
                            ArrivalSafetyStatus.MODERATE -> Color(0xFF14243B)
                            ArrivalSafetyStatus.LOW_WARNING -> Color(0xFF2E2207)
                            ArrivalSafetyStatus.CRITICAL_UNREACHABLE -> Color(0xFF330C0C)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "Destination",
                                    tint = EvCyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "จุดหมาย: ${route.destination.nameThai}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "${String.format("%.1f", route.distanceKm)} กม.",
                                fontWeight = FontWeight.Bold,
                                color = EvCyanPrimary,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Predicted Arrival Battery Badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F172A))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "แบตเตอรี่คงเหลือปลายทาง",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    val arrivalColor = when (route.safetyStatus) {
                                        ArrivalSafetyStatus.SAFE -> EvBatteryGreen
                                        ArrivalSafetyStatus.MODERATE -> EvCyanPrimary
                                        ArrivalSafetyStatus.LOW_WARNING -> EvBatteryAmber
                                        ArrivalSafetyStatus.CRITICAL_UNREACHABLE -> EvBatteryRed
                                    }
                                    Text(
                                        text = "${String.format("%.1f", route.arrivalSocPercent)}%",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = arrivalColor
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(-${String.format("%.1f", route.consumedSocPercent)}%)",
                                        fontSize = 13.sp,
                                        color = Color(0xFF94A3B8),
                                        modifier = Modifier.padding(bottom = 3.dp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = route.safetyStatus.labelThai,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when (route.safetyStatus) {
                                        ArrivalSafetyStatus.SAFE -> EvBatteryGreen
                                        ArrivalSafetyStatus.MODERATE -> EvCyanPrimary
                                        ArrivalSafetyStatus.LOW_WARNING -> EvBatteryAmber
                                        ArrivalSafetyStatus.CRITICAL_UNREACHABLE -> EvBatteryRed
                                    }
                                )
                                Text(
                                    text = "เหลือระยะ ${String.format("%.0f", route.remainingRangeAtDestinationKm)} กม.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Launch Google Maps Button
                        Button(
                            onClick = {
                                viewModel.launchGoogleMapsNavigation(context, route.destination)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("launch_google_maps_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EvCyanPrimary,
                                contentColor = Color(0xFF00363D)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Google Maps",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "นำทางด้วย Google Maps ตอนนี้",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Real-Time Telemetry Grid
        item {
            Text(
                text = "ข้อมูลระบบไฟฟ้า & แบตเตอรี่ Blade Battery",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryTile(
                    title = "แรงดันไฟฟ้า (Pack)",
                    value = "${String.format("%.1f", uiState.batteryData.voltageVolts)} V",
                    subtitle = "High Voltage DC",
                    icon = Icons.Default.ElectricBolt,
                    iconColor = EvCyanPrimary,
                    modifier = Modifier.weight(1f)
                )

                TelemetryTile(
                    title = "อุณหภูมิแพ็คแบต",
                    value = "${String.format("%.1f", uiState.batteryData.temperatureCelsius)} °C",
                    subtitle = "Blade Cell คุมความร้อน",
                    icon = Icons.Default.Thermostat,
                    iconColor = if (uiState.batteryData.temperatureCelsius > 38f) EvBatteryRed else EvBatteryGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryTile(
                    title = "สุขภาพแบตเตอรี่ (SoH)",
                    value = "${uiState.batteryData.stateOfHealthPercent}%",
                    subtitle = "ประสิทธิภาพสมบูรณ์",
                    icon = Icons.Default.Favorite,
                    iconColor = EvBatteryGreen,
                    modifier = Modifier.weight(1f)
                )

                TelemetryTile(
                    title = "สถานะการทำงาน",
                    value = if (uiState.batteryData.isCharging) "กำลังชาร์จไฟ" else "ขับขี่ (EV)",
                    subtitle = uiState.batteryData.chargeStatus,
                    icon = Icons.Default.Speed,
                    iconColor = if (uiState.batteryData.isCharging) EvBatteryGreen else EvElectricBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Controls & Simulation Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ระบบควบคุมด่วน & ทดสอบจำลองการขับขี่",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "จำลองการขับขี่จริง (Drive Drain)",
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (uiState.isSimulatingDrive) "กำลังจำลองแบตเตอรี่ลดลงขณะวิ่ง..." else "กดเพื่อทดสอบแบตเตอรี่ลดลงแบบเรียลไทม์",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        FilledTonalButton(
                            onClick = { viewModel.toggleDriveSimulation() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (uiState.isSimulatingDrive) EvBatteryRed.copy(alpha = 0.2f) else EvCyanPrimary.copy(alpha = 0.2f),
                                contentColor = if (uiState.isSimulatingDrive) EvBatteryRed else EvCyanPrimary
                            )
                        ) {
                            Icon(
                                imageVector = if (uiState.isSimulatingDrive) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Simulate",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (uiState.isSimulatingDrive) "หยุดจำลอง" else "จำลองวิ่ง")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Manual bench slider toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ปรับตั้งค่าระดับแบตเตอรี่ด้วยตนเอง (Bench Test)",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        OutlinedButton(
                            onClick = { showManualSlider = !showManualSlider },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Adjust", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showManualSlider) "ซ่อน" else "ปรับ SoC", fontSize = 11.sp)
                        }
                    }

                    if (showManualSlider) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "เลือกระดับแบตเตอรี่: ${uiState.batteryData.socPercent}%",
                            fontSize = 12.sp,
                            color = EvCyanPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = uiState.batteryData.socPercent.toFloat(),
                            onValueChange = { viewModel.setBatteryLevelManually(it.toInt()) },
                            valueRange = 1f..100f,
                            steps = 99,
                            colors = SliderDefaults.colors(
                                thumbColor = EvCyanPrimary,
                                activeTrackColor = EvCyanPrimary
                            )
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TelemetryTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = Color(0xFF94A3B8))
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
