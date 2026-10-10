package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ArrivalSafetyStatus
import com.example.model.CarGear
import com.example.ui.components.TeslaTopBar
import com.example.ui.theme.EvBatteryAmber
import com.example.ui.theme.EvBatteryGreen
import com.example.ui.theme.EvBatteryRed
import com.example.ui.theme.EvCyanPrimary
import com.example.viewmodel.BydNavUiState
import com.example.viewmodel.BydNavViewModel

@Composable
fun DrivingHudDialog(
    viewModel: BydNavViewModel,
    uiState: BydNavUiState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val route = uiState.routeCalculation

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF060913))
                .padding(16.dp)
                .testTag("driving_hud_fullscreen")
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP BAR: Tesla P R N D Gears + Speed (Top Right) + Exit Button
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close HUD",
                                tint = Color.White
                            )
                        }

                        TeslaTopBar(
                            currentSpeedKmH = uiState.currentSpeedKmH,
                            currentGear = uiState.currentGear,
                            isGpsActive = uiState.isGpsActive,
                            onGearSelected = { viewModel.setGear(it) },
                            onSpeedClick = { viewModel.setSpeedManually((uiState.currentSpeedKmH + 10) % 130) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // CENTER: High-Impact Destination Battery Gauge & Details
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (route != null) {
                        val arrivalColor = when (route.safetyStatus) {
                            ArrivalSafetyStatus.SAFE -> EvBatteryGreen
                            ArrivalSafetyStatus.MODERATE -> EvCyanPrimary
                            ArrivalSafetyStatus.LOW_WARNING -> EvBatteryAmber
                            ArrivalSafetyStatus.CRITICAL_UNREACHABLE -> EvBatteryRed
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "Nav",
                                        tint = EvCyanPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = route.destination.nameThai,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "แบตเตอรี่คงเหลือ ณ จุดหมาย",
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "${String.format("%.1f", route.arrivalSocPercent)}%",
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.SansSerif,
                                    color = arrivalColor
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                LinearProgressIndicator(
                                    progress = { (route.arrivalSocPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp)),
                                    color = arrivalColor,
                                    trackColor = Color(0xFF334155)
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    HudMetricItem(
                                        label = "แบตปัจจุบัน",
                                        value = "${uiState.batteryData.socPercent}%"
                                    )
                                    HudMetricItem(
                                        label = "ใช้ไป",
                                        value = "-${String.format("%.1f", route.consumedSocPercent)}%"
                                    )
                                    HudMetricItem(
                                        label = "ระยะทาง",
                                        value = "${String.format("%.1f", route.distanceKm)} กม."
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "กรุณาเลือกจุดหมายปลายทางในแท็บนำทาง",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    }
                }

                // BOTTOM ACTION: Google Maps Navigation Button
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (route != null) {
                        Button(
                            onClick = {
                                viewModel.launchGoogleMapsNavigation(context, route.destination)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("btn_hud_launch_maps"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EvCyanPrimary,
                                contentColor = Color(0xFF00363D)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Google Maps",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "เปิด Google Maps นำทางตอนนี้",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun HudMetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
