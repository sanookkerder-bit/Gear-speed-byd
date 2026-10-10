package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdbConnectionStatus
import com.example.model.WirelessProtocol
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
fun AdbSetupScreen(
    viewModel: BydNavViewModel,
    uiState: BydNavUiState,
    modifier: Modifier = Modifier
) {
    var showGuide by remember { mutableStateOf(false) }
    var customCommandText by remember { mutableStateOf("dumpsys battery") }

    val isConnected = uiState.adbStatus is AdbConnectionStatus.Connected
    val isConnecting = uiState.adbStatus is AdbConnectionStatus.Connecting

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "เชื่อมต่อไร้สายอ่านข้อมูลแบตเตอรี่ BYD",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "รองรับทั้ง ADB Wireless (Android Debug Bridge) และ อะแดปเตอร์ OBD2 ไร้สาย",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }

        // Protocol Selector Tabs: ADB Wireless vs OBD2 Wi-Fi vs OBD2 Bluetooth
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E293B))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ProtocolSelectTab(
                            title = "ADB ไร้สาย",
                            subtitle = "DiLink (5555)",
                            icon = Icons.Default.Wifi,
                            isSelected = uiState.activeProtocol == WirelessProtocol.ADB_WIFI,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.updateProtocol(WirelessProtocol.ADB_WIFI)
                        }

                        ProtocolSelectTab(
                            title = "OBD2 Wi-Fi",
                            subtitle = "ELM327",
                            icon = Icons.Default.DirectionsCar,
                            isSelected = uiState.activeProtocol == WirelessProtocol.OBD2_WIFI,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.updateProtocol(WirelessProtocol.OBD2_WIFI)
                        }

                        ProtocolSelectTab(
                            title = "OBD2 Bluetooth",
                            subtitle = "BT อะแดปเตอร์",
                            icon = Icons.Default.Bluetooth,
                            isSelected = uiState.activeProtocol == WirelessProtocol.OBD2_BLUETOOTH,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.updateProtocol(WirelessProtocol.OBD2_BLUETOOTH)
                        }
                    }
                }
            }
        }

        // Connection Control Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("connection_control_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Status Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isConnected -> EvBatteryGreen.copy(alpha = 0.2f)
                                            isConnecting -> EvBatteryAmber.copy(alpha = 0.2f)
                                            else -> Color(0xFF334155).copy(alpha = 0.4f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Wifi,
                                    contentDescription = "Status",
                                    tint = when {
                                        isConnected -> EvBatteryGreen
                                        isConnecting -> EvBatteryAmber
                                        else -> Color(0xFF94A3B8)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = when {
                                        isConnected -> "เชื่อมต่อสำเร็จ (${uiState.activeProtocol.titleThai})"
                                        isConnecting -> "กำลังเชื่อมต่อ..."
                                        else -> "พร้อมเชื่อมต่อ ${uiState.activeProtocol.titleThai}"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isConnected) "ดึงระดับแบตเตอรี่เรียลไทม์ต่อเนื่อง" else uiState.activeProtocol.subtitleThai,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        if (isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = EvCyanPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic inputs based on protocol
                    if (uiState.activeProtocol == WirelessProtocol.ADB_WIFI) {
                        // ADB Wireless IP & Port
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.adbConfig.ipAddress,
                                onValueChange = { viewModel.updateAdbIp(it) },
                                label = { Text("IP Address ของ DiLink", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(2f)
                                    .testTag("input_adb_ip"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EvCyanPrimary,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                            )

                            OutlinedTextField(
                                value = uiState.adbConfig.port.toString(),
                                onValueChange = { it.toIntOrNull()?.let { p -> viewModel.updateAdbPort(p) } },
                                label = { Text("Port", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_adb_port"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EvCyanPrimary,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickIpChip("Hotspot 192.168.43.1") {
                                viewModel.updateAdbIp("192.168.43.1")
                                viewModel.updateAdbPort(5555)
                            }
                            QuickIpChip("Wi-Fi 192.168.1.100") {
                                viewModel.updateAdbIp("192.168.1.100")
                                viewModel.updateAdbPort(5555)
                            }
                        }
                    } else if (uiState.activeProtocol == WirelessProtocol.OBD2_WIFI) {
                        // OBD2 Wi-Fi IP & Port
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.obd2Config.wifiIp,
                                onValueChange = { viewModel.updateObdConfig(uiState.obd2Config.copy(wifiIp = it)) },
                                label = { Text("IP อะแดปเตอร์ OBD2 Wi-Fi", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(2f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EvCyanPrimary,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = uiState.obd2Config.wifiPort.toString(),
                                onValueChange = { it.toIntOrNull()?.let { p -> viewModel.updateObdConfig(uiState.obd2Config.copy(wifiPort = p)) } },
                                label = { Text("Port (35000)", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EvCyanPrimary,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickIpChip("Default 192.168.0.10:35000") {
                                viewModel.updateObdConfig(uiState.obd2Config.copy(wifiIp = "192.168.0.10", wifiPort = 35000))
                            }
                            QuickIpChip("Vgate 192.168.1.5:35000") {
                                viewModel.updateObdConfig(uiState.obd2Config.copy(wifiIp = "192.168.1.5", wifiPort = 35000))
                            }
                        }
                    } else {
                        // OBD2 Bluetooth device name / address
                        OutlinedTextField(
                            value = uiState.obd2Config.bluetoothDeviceName,
                            onValueChange = { viewModel.updateObdConfig(uiState.obd2Config.copy(bluetoothDeviceName = it)) },
                            label = { Text("ชื่ออุปกรณ์บลูทูธ (เช่น OBDII / Vgate / OBDLink)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EvCyanPrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Connect / Disconnect Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isConnected) {
                            Button(
                                onClick = { viewModel.connectWirelessAdapter() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_connect_wireless"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EvCyanPrimary,
                                    contentColor = Color(0xFF00363D)
                                )
                            ) {
                                Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("เชื่อมต่อดึงแบตเตอรี่", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.disconnectAdb() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_disconnect_wireless"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EvBatteryRed)
                            ) {
                                Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ตัดการเชื่อมต่อ")
                            }

                            FilledTonalButton(
                                onClick = { viewModel.refreshBatteryNow() },
                                modifier = Modifier.testTag("btn_refresh_now"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ดึงค่าใหม่")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Demo Mode Toggle Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "โหมดจำลอง (Simulator Demo Mode)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "จำลองการอ่านแบตเตอรี่สดเพื่อทดสอบระบบนำทางทันที",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Switch(
                            checked = uiState.adbConfig.isDemoMode,
                            onCheckedChange = { viewModel.toggleDemoMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00363D),
                                checkedTrackColor = EvCyanPrimary
                            )
                        )
                    }
                }
            }
        }

        // How-to Guide Accordion
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Guide",
                                tint = EvCyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "คู่มือตั้งค่าการเชื่อมต่อรถ BYD",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = { showGuide = !showGuide },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (showGuide) "ซ่อน" else "ดูขั้นตอน", fontSize = 11.sp)
                        }
                    }

                    AnimatedVisibility(visible = showGuide) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            GuideStepItem(
                                stepNumber = 1,
                                title = "สำหรับ ADB Wireless (หน้าจอกลาง DiLink)",
                                desc = "เชื่อมต่อ Wi-Fi Hotspot ในรถ หรือวงเดียวกัน -> เปิด Wireless ADB ใน DiLink Developer Options (หรือพิมพ์โค้ด *#*#byd#*#*) -> ใส่ IP เช่น 192.168.43.1:5555"
                            )
                            GuideStepItem(
                                stepNumber = 2,
                                title = "สำหรับ OBD2 ไร้สาย (ELM327 Wi-Fi / Bluetooth)",
                                desc = "เสียบอะแดปเตอร์ OBD2 เข้ากับช่อง OBD2 ใต้พวงมาลัยรถ BYD -> เชื่อมต่อ Wi-Fi อะแดปเตอร์ (ปกติ IP 192.168.0.10 พอร์ต 35000) หรือจับคู่บลูทูธ OBDII ในการตั้งค่าเครื่อง"
                            )
                            GuideStepItem(
                                stepNumber = 3,
                                title = "การอ่านข้อมูลต่อเนื่อง",
                                desc = "เมื่อเชื่อมต่อแล้ว แอพจะอ่านค่าเปอร์เซ็นต์แบตเตอรี่ (SoC%) แรงดันไฟฟ้า และระยะทางอย่างต่อเนื่องทุก 2-5 วินาที"
                            )
                        }
                    }
                }
            }
        }

        // Live ADB / OBD2 Console Log Terminal
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070B19))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Terminal",
                                tint = EvCyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "เทอร์มินัลบันทึกคำสั่งและข้อมูลสด (Console Log)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "${uiState.consoleLogs.size} บรรทัด",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Command Execution Input Box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customCommandText,
                            onValueChange = { customCommandText = it },
                            placeholder = { Text("dumpsys battery", fontSize = 11.sp, color = Color(0xFF64748B)) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_custom_cmd"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EvCyanPrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = EvCyanPrimary,
                                unfocusedTextColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                viewModel.runCustomAdbCommand(customCommandText)
                            })
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { viewModel.runCustomAdbCommand(customCommandText) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EvCyanPrimary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF00363D))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Terminal Output Window
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF02040A))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(uiState.consoleLogs) { log ->
                                val time = remember(log.timestamp) {
                                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                                }
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                    Text(
                                        text = "[$time] ${log.command}",
                                        color = if (log.isError) EvBatteryRed else EvCyanPrimary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = log.output,
                                        color = if (log.isError) Color(0xFFFFA4A4) else Color(0xFFE2E8F0),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProtocolSelectTab(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isSelected) EvCyanPrimary else Color.Transparent,
            contentColor = if (isSelected) Color(0xFF00363D) else Color(0xFF94A3B8)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            Text(text = subtitle, fontSize = 9.sp, color = if (isSelected) Color(0xFF004F58) else Color(0xFF64748B))
        }
    }
}

@Composable
fun GuideStepItem(stepNumber: Int, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(EvCyanPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$stepNumber",
                color = Color(0xFF00363D),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = desc, fontSize = 11.sp, color = Color(0xFF94A3B8), lineHeight = 15.sp)
        }
    }
}

@Composable
fun QuickIpChip(text: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color(0xFF1E293B),
            contentColor = EvCyanPrimary
        )
    ) {
        Text(text, fontSize = 11.sp)
    }
}

