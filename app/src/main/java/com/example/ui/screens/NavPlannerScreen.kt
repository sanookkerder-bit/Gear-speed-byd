package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DestinationPresets
import com.example.model.ArrivalSafetyStatus
import com.example.model.DestinationCategory
import com.example.model.DrivingMode
import com.example.model.TripDestination
import com.example.ui.theme.EvBatteryAmber
import com.example.ui.theme.EvBatteryGreen
import com.example.ui.theme.EvBatteryRed
import com.example.ui.theme.EvCyanPrimary
import com.example.ui.theme.EvElectricBlue
import com.example.viewmodel.BydNavUiState
import com.example.viewmodel.BydNavViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NavPlannerScreen(
    viewModel: BydNavViewModel,
    uiState: BydNavUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategoryFilter by remember { mutableStateOf<DestinationCategory?>(null) }
    var isCustomDistanceSliderOpen by remember { mutableStateOf(false) }
    var showTurnByTurnDetails by remember { mutableStateOf(true) }

    // Search query filtering
    val filteredDestinations = remember(uiState.searchDestinationQuery, selectedCategoryFilter, uiState.destinations) {
        val q = uiState.searchDestinationQuery.trim().lowercase()
        uiState.destinations.filter { dest ->
            val matchesCategory = selectedCategoryFilter == null || dest.category == selectedCategoryFilter
            val matchesQuery = q.isEmpty() ||
                    dest.nameThai.lowercase().contains(q) ||
                    dest.nameEn.lowercase().contains(q) ||
                    (dest.chargingProvider?.lowercase()?.contains(q) == true)
            matchesCategory && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ระบบนำทาง Google Maps & คำนวณแบตเตอรี่",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "ค้นหาจุดหมาย วางแผนเส้นทาง และประเมินเปอร์เซ็นต์แบตเตอรี่คงเหลือ ณ ปลายทาง",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }

        // DESTINATION SEARCH BAR
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    OutlinedTextField(
                        value = uiState.searchDestinationQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("ค้นหาสถานที่ หรือ สถานีชาร์จ (เช่น Central, Mega, PTT, พัทยา)", fontSize = 12.sp, color = Color(0xFF64748B)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = EvCyanPrimary)
                        },
                        trailingIcon = {
                            if (uiState.searchDestinationQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("destination_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EvCyanPrimary,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (uiState.searchDestinationQuery.isNotBlank() && filteredDestinations.isEmpty()) {
                                viewModel.addCustomSearchDestination(uiState.searchDestinationQuery, 25.0)
                            }
                        })
                    )

                    // Quick add if not found
                    if (uiState.searchDestinationQuery.isNotBlank() && filteredDestinations.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FilledTonalButton(
                            onClick = {
                                viewModel.addCustomSearchDestination(uiState.searchDestinationQuery, 25.0)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("เพิ่ม \"${uiState.searchDestinationQuery}\" เป็นจุดหมายใหม่ (25 กม.)")
                        }
                    }
                }
            }
        }

        // PRIMARY HERO CARD: Destination Arrival Battery Prediction
        item {
            uiState.routeCalculation?.let { route ->
                val arrivalColor = when (route.safetyStatus) {
                    ArrivalSafetyStatus.SAFE -> EvBatteryGreen
                    ArrivalSafetyStatus.MODERATE -> EvCyanPrimary
                    ArrivalSafetyStatus.LOW_WARNING -> EvBatteryAmber
                    ArrivalSafetyStatus.CRITICAL_UNREACHABLE -> EvBatteryRed
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("arrival_battery_result_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Title Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(arrivalColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "Nav",
                                        tint = arrivalColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = route.destination.nameThai,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "ระยะทาง: ${String.format("%.1f", route.distanceKm)} กม. • เวลาประเมิน ~${(route.distanceKm * 1.1).toInt()} นาที",
                                        fontSize = 12.sp,
                                        color = EvCyanPrimary
                                    )
                                }
                            }

                            // Safety Status Pill
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(arrivalColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (route.isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = "Status",
                                    tint = arrivalColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (route.isSafe) "ถึงปลอดภัย" else "ต้องแวะชาร์จ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = arrivalColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Arrival Battery Percentage Display
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF1E293B))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ระดับแบตเตอรี่คงเหลือ ณ จุดหมาย",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${String.format("%.1f", route.arrivalSocPercent)}%",
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = arrivalColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(ใช้ไป ${String.format("%.1f", route.consumedSocPercent)}%)",
                                        fontSize = 14.sp,
                                        color = Color(0xFF94A3B8),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "แบตปัจจุบัน",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "${route.currentSoc}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ใช้ ${String.format("%.1f", route.consumedKwh)} kWh",
                                    fontSize = 11.sp,
                                    color = EvCyanPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Battery Visual Bar
                        LinearProgressIndicator(
                            progress = { (route.arrivalSocPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = arrivalColor,
                            trackColor = Color(0xFF334155)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Recommendation message
                        Text(
                            text = route.chargingRecommendation ?: "",
                            fontSize = 12.sp,
                            color = if (route.isSafe) Color(0xFFCBD5E1) else EvBatteryAmber,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Launch Google Maps Navigation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.launchGoogleMapsNavigation(context, route.destination)
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("btn_launch_google_maps"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EvCyanPrimary,
                                    contentColor = Color(0xFF00363D)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Maps",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "เริ่มนำทาง Google Maps",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            FilledTonalButton(
                                onClick = { viewModel.setHudMode(true) },
                                modifier = Modifier
                                    .weight(0.9f)
                                    .testTag("btn_open_hud_mode"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF1E293B),
                                    contentColor = EvCyanPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = "HUD",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "โหมด HUD", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // IN-APP TURN-BY-TURN ROUTE PLAN DETAILS
        item {
            uiState.routeCalculation?.let { route ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Directions,
                                    contentDescription = "Directions",
                                    tint = EvCyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "แผนการเดินทาง & ทิศทางแบบเลี้ยวต่อเลี้ยว",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = { showTurnByTurnDetails = !showTurnByTurnDetails },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (showTurnByTurnDetails) "ซ่อน" else "ดูรายละเอียด", fontSize = 11.sp)
                            }
                        }

                        AnimatedVisibility(visible = showTurnByTurnDetails) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                RouteStepItem(
                                    step = 1,
                                    instruction = "เริ่มต้นการเดินทางจากตำแหน่ง GPS ปัจจุบัน",
                                    detail = "ตรวจสอบระดับแบตเตอรี่รถ BYD: ${route.currentSoc}% (${uiState.carModel.name})",
                                    distanceText = "จุดเริ่มต้น"
                                )
                                RouteStepItem(
                                    step = 2,
                                    instruction = "มุ่งหน้าเข้าสู่ถนนสายหลัก / ทางหลวงพิเศษ",
                                    detail = "โหมด ${uiState.drivingMode.titleThai} • อัตราสิ้นเปลืองเฉลี่ย ${uiState.carModel.baseConsumptionKwhPer100Km} kWh/100km",
                                    distanceText = "${String.format("%.1f", route.distanceKm * 0.4)} กม."
                                )
                                RouteStepItem(
                                    step = 3,
                                    instruction = "จุดตรวจเช็คพลังงานกลางทาง",
                                    detail = "แบตเตอรี่ประเมินเหลือ ~${String.format("%.1f", route.currentSoc - (route.consumedSocPercent * 0.5))}%",
                                    distanceText = "${String.format("%.1f", route.distanceKm * 0.5)} กม."
                                )
                                RouteStepItem(
                                    step = 4,
                                    instruction = "ถึงปลายทาง: ${route.destination.nameThai}",
                                    detail = "แบตเตอรี่คงเหลือ ${String.format("%.1f", route.arrivalSocPercent)}% (เหลือระยะทางต่ออีก ${String.format("%.0f", route.remainingRangeAtDestinationKm)} กม.)",
                                    distanceText = "ถึงที่หมาย"
                                )
                            }
                        }
                    }
                }
            }
        }

        // Trip Condition Fine-Tuners
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ปรับปัจจัยการใช้พลังงาน (Driving Conditions)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Driving Mode Selector
                    Text(
                        text = "โหมดการขับขี่: ${uiState.drivingMode.titleThai}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DrivingMode.entries.forEach { mode ->
                            val isSelected = uiState.drivingMode == mode
                            FilledTonalButton(
                                onClick = { viewModel.setDrivingMode(mode) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) EvCyanPrimary else Color(0xFF1E293B),
                                    contentColor = if (isSelected) Color(0xFF00363D) else Color(0xFF94A3B8)
                                )
                            ) {
                                Text(
                                    text = mode.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // AC Toggle & Passenger Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AcUnit,
                                contentDescription = "AC",
                                tint = if (uiState.isAcOn) EvCyanPrimary else Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "เปิดแอร์ปรับอากาศ (A/C)", fontSize = 12.sp, color = Color.White)
                                Text(text = if (uiState.isAcOn) "ใช้ไฟเพิ่ม ~6%" else "ประหยัดไฟสูงสุด", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                        Switch(
                            checked = uiState.isAcOn,
                            onCheckedChange = { viewModel.toggleAc(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00363D),
                                checkedTrackColor = EvCyanPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Passenger Count Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Passengers",
                                tint = EvCyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "ผู้โดยสารและสัมภาระ", fontSize = 12.sp, color = Color.White)
                                Text(text = "${uiState.passengerCount} คน (น้ำหนักบรรทุก)", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.setPassengerCount(uiState.passengerCount - 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Minus", tint = Color.White)
                            }
                            Text(
                                text = "${uiState.passengerCount}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = EvCyanPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { viewModel.setPassengerCount(uiState.passengerCount + 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Plus", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Destination Presets & Custom Distance Slider
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "รายการจุดหมายปลายทางยอดนิยม",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                OutlinedButton(
                    onClick = { isCustomDistanceSliderOpen = !isCustomDistanceSliderOpen },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isCustomDistanceSliderOpen) "ปิดสไลเดอร์" else "สไลด์ปรับระยะทาง (กม.)", fontSize = 11.sp)
                }
            }

            AnimatedVisibility(visible = isCustomDistanceSliderOpen) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ปรับระยะทางแบบละเอียด (1 - 600 กม.)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EvCyanPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = uiState.customDistanceKm.toFloat(),
                            onValueChange = { viewModel.setCustomDistance(it.toDouble()) },
                            valueRange = 1f..600f,
                            steps = 119,
                            colors = SliderDefaults.colors(
                                thumbColor = EvCyanPrimary,
                                activeTrackColor = EvCyanPrimary
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "1 กม.", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text(
                                text = "ระยะทางที่เลือก: ${String.format("%.1f", uiState.customDistanceKm)} กม.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(text = "600 กม.", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("ทั้งหมด (${filteredDestinations.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EvCyanPrimary,
                        selectedLabelColor = Color(0xFF00363D)
                    )
                )
                DestinationCategory.entries.filter { it != DestinationCategory.CUSTOM }.forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat.labelThai, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EvCyanPrimary,
                            selectedLabelColor = Color(0xFF00363D)
                        )
                    )
                }
            }
        }

        // Destination Items List
        items(filteredDestinations) { dest ->
            val isSelected = uiState.selectedDestination?.id == dest.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectDestination(dest) }
                    .testTag("destination_card_${dest.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF00363D) else Color(0xFF0F172A)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (dest.hasDcFastCharger) EvBatteryGreen.copy(alpha = 0.2f) else Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (dest.hasDcFastCharger) Icons.Default.EvStation else Icons.Default.Navigation,
                                contentDescription = "Destination",
                                tint = if (dest.hasDcFastCharger) EvBatteryGreen else EvCyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = dest.nameThai,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = dest.chargingProvider ?: dest.nameEn,
                                fontSize = 11.sp,
                                color = if (dest.hasDcFastCharger) EvBatteryGreen else Color(0xFF94A3B8)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${dest.distanceKm} กม.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EvCyanPrimary
                        )
                        if (isSelected) {
                            Text(
                                text = "เลือกอยู่",
                                fontSize = 10.sp,
                                color = EvBatteryGreen,
                                fontWeight = FontWeight.Bold
                            )
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
fun RouteStepItem(step: Int, instruction: String, detail: String, distanceText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(EvCyanPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$step",
                color = Color(0xFF00363D),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = instruction, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = detail, fontSize = 10.sp, color = Color(0xFF94A3B8))
        }
        Text(text = distanceText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EvCyanPrimary)
    }
}
