package com.example

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AdbConnectionStatus
import com.example.model.ArrivalSafetyStatus
import com.example.model.BydCarModel
import com.example.model.CarGear
import com.example.ui.components.TeslaTopBar
import com.example.ui.screens.GoogleMapsViewScreen
import com.example.ui.theme.EvBatteryGreen
import com.example.ui.theme.EvBatteryRed
import com.example.ui.theme.EvCyanPrimary
import com.example.ui.theme.EvDeepNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BydNavViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enforce landscape orientation exclusively
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BydNavApp()
            }
        }
    }
}

@Composable
fun BydNavApp(
    viewModel: BydNavViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showBatteryAdbDialog by remember { mutableStateOf(false) }

    // Runtime Permission Launcher for GPS Location Speed
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.initLocationTracker(context)
        }
    }

    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            viewModel.initLocationTracker(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(EvDeepNavy),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            // Tesla-style Statusbar across the top:
            // - Left: BYD telemetry, Live Battery %, Destination Arrival Battery %, and Overlay Toggle
            // - Right: Tesla-style P R N D Gear Selector + Live GPS Speed (Top Right Corner)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B19))
            ) {
                TeslaTopBar(
                    currentSpeedKmH = uiState.currentSpeedKmH,
                    currentGear = uiState.currentGear,
                    isGpsActive = uiState.isGpsActive,
                    socPercent = uiState.batteryData.socPercent,
                    arrivalSocPercent = uiState.routeCalculation?.arrivalSocPercent,
                    safetyStatus = uiState.routeCalculation?.safetyStatus ?: ArrivalSafetyStatus.SAFE,
                    seatbeltStatus = uiState.seatbeltStatus,
                    isCharging = uiState.batteryData.isCharging,
                    onGearSelected = { gear -> viewModel.setGear(gear) },
                    onSpeedClick = { showSpeedDialog = true },
                    onBatteryClick = { showBatteryAdbDialog = true },
                    onToggleSeatbelt = { seatIndex -> viewModel.toggleSeatbelt(seatIndex) },
                    onToggleOverlay = { viewModel.toggleFloatingOverlay(context) },
                    isOverlayActive = uiState.isOverlayActive,
                    modifier = Modifier.testTag("app_tesla_top_bar")
                )
                // Subtle horizontal glow separator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF1E293B))
                )
            }
        }
    ) { innerPadding ->
        // Direct Embedded Google Maps Fullscreen Display (Replaces old navigation & tabs completely)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(EvDeepNavy)
        ) {
            GoogleMapsViewScreen(
                viewModel = viewModel,
                uiState = uiState,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Quick Battery & Wireless ADB Config Dialog
    if (showBatteryAdbDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryAdbDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = EvCyanPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "จัดการข้อมูลแบตเตอรี่ & เชื่อมต่อ ADB รถ BYD",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Battery Level Adjustment Slider
                    Text(
                        text = "ระดับแบตเตอรี่ปัจจุบัน (SoC): ${uiState.batteryData.socPercent}%",
                        color = EvCyanPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Slider(
                        value = uiState.batteryData.socPercent.toFloat(),
                        onValueChange = { viewModel.setBatteryLevelManually(it.toInt()) },
                        valueRange = 1f..100f,
                        steps = 98,
                        colors = SliderDefaults.colors(
                            thumbColor = EvCyanPrimary,
                            activeTrackColor = EvCyanPrimary
                        )
                    )
                    Text(
                        text = "ระยะทางที่วิ่งได้โดยประมาณ: ${uiState.batteryData.estimatedRangeKm} กม. | แรงดัน ${uiState.batteryData.voltageVolts}V",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Vehicle Model Selection
                    Text(
                        text = "รุ่นรถ BYD: ${uiState.carModel.name}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(BydCarModel.ALL_MODELS) { model ->
                            val isSelected = uiState.carModel.id == model.id
                            FilledTonalButton(
                                onClick = { viewModel.updateCarModel(model) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) EvCyanPrimary else Color(0xFF1E293B),
                                    contentColor = if (isSelected) Color(0xFF00363D) else Color.White
                                )
                            ) {
                                Text(model.name, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Wireless ADB Quick Connect to BYD DiLink
                    Text(
                        text = "เชื่อมต่อ ADB ไร้สาย (Android Debug Bridge - BYD DiLink)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.adbConfig.ipAddress,
                            onValueChange = { viewModel.updateAdbIp(it) },
                            label = { Text("IP Address", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EvCyanPrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = uiState.adbConfig.port.toString(),
                            onValueChange = { it.toIntOrNull()?.let { p -> viewModel.updateAdbPort(p) } },
                            label = { Text("Port", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EvCyanPrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isConnected = uiState.adbStatus is AdbConnectionStatus.Connected
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isConnected) EvBatteryGreen else Color(0xFF64748B))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isConnected) "เชื่อมต่อสำเร็จ" else "ยังไม่เชื่อมต่อ",
                                fontSize = 11.sp,
                                color = if (isConnected) EvBatteryGreen else Color(0xFF94A3B8)
                            )
                        }

                        Button(
                            onClick = {
                                if (uiState.adbStatus is AdbConnectionStatus.Connected) {
                                    viewModel.disconnectAdb()
                                } else {
                                    viewModel.connectAdb()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.adbStatus is AdbConnectionStatus.Connected) Color(0xFFDC2626) else EvCyanPrimary,
                                contentColor = if (uiState.adbStatus is AdbConnectionStatus.Connected) Color.White else Color(0xFF00363D)
                            )
                        ) {
                            Text(
                                text = if (uiState.adbStatus is AdbConnectionStatus.Connected) "ตัดการเชื่อมต่อ" else "เชื่อมต่อ ADB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBatteryAdbDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EvCyanPrimary, contentColor = Color(0xFF00363D))
                ) {
                    Text("ตกลง", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Manual Speed & Gear Testing Dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = {
                Text(
                    text = "ทดสอบปรับความเร็ว GPS & เกียร์รถยนต์",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "ความเร็วปัจจุบัน: ${uiState.currentSpeedKmH} km/h (ดึงจาก GPS ดาวเทียมจริง)",
                        color = EvCyanPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Slider(
                        value = uiState.currentSpeedKmH.toFloat(),
                        onValueChange = { viewModel.setSpeedManually(it.toInt()) },
                        valueRange = 0f..180f,
                        steps = 35,
                        colors = SliderDefaults.colors(
                            thumbColor = EvCyanPrimary,
                            activeTrackColor = EvCyanPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "แตะ P R N D บน Statusbar เพื่อเปลี่ยนเกียร์ หรือตั้งค่าความเร็ว",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSpeedDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EvCyanPrimary, contentColor = Color(0xFF00363D))
                ) {
                    Text("ตกลง", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(18.dp)
        )
    }
}
