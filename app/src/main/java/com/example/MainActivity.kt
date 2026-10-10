package com.example

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.CarGear
import com.example.ui.components.TeslaTopBar
import com.example.ui.screens.AdbSetupScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DrivingHudDialog
import com.example.ui.screens.LandscapeDashboardScreen
import com.example.ui.screens.NavPlannerScreen
import com.example.ui.screens.VehicleProfileScreen
import com.example.ui.theme.EvCyanPrimary
import com.example.ui.theme.EvDeepNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BydNavViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val activity = context as? Activity

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    fun toggleOrientation() {
        if (isLandscape) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

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
            // Tesla-style Top Bar with P R N D Gears + Live GPS Speed (Top Right)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B19))
            ) {
                TeslaTopBar(
                    currentSpeedKmH = uiState.currentSpeedKmH,
                    currentGear = uiState.currentGear,
                    isGpsActive = uiState.isGpsActive,
                    onGearSelected = { gear -> viewModel.setGear(gear) },
                    onSpeedClick = { showSpeedDialog = true },
                    onToggleOrientation = { toggleOrientation() },
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
        },
        bottomBar = {
            if (!isLandscape) {
                // Portrait Bottom Navigation Bar
                NavigationBar(
                    modifier = Modifier
                        .testTag("main_bottom_nav")
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = Color(0xFF0B132B),
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Speed, contentDescription = "แดชบอร์ด") },
                        label = { Text("แดชบอร์ด", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = EvCyanPrimary,
                            indicatorColor = EvCyanPrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Navigation, contentDescription = "นำทาง") },
                        label = { Text("นำทาง Maps", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = EvCyanPrimary,
                            indicatorColor = EvCyanPrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Wifi, contentDescription = "ADB / OBD2") },
                        label = { Text("เชื่อมต่อ", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = EvCyanPrimary,
                            indicatorColor = EvCyanPrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "รถ BYD") },
                        label = { Text("รุ่นรถ BYD", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = EvCyanPrimary,
                            indicatorColor = EvCyanPrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(EvDeepNavy)
        ) {
            // In Landscape Mode: Left NavigationRail
            if (isLandscape) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = Color(0xFF0B132B),
                    contentColor = Color.White
                ) {
                    NavigationRailItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Speed, contentDescription = "แดชบอร์ด") },
                        label = { Text("หน้าจอหลัก", fontSize = 10.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = EvCyanPrimary,
                            indicatorColor = EvCyanPrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )

                    NavigationRailItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Navigation, contentDescription = "นำทาง") },
                        label = { Text("นำทาง", fontSize = 10.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = EvCyanPrimary,
                            indicatorColor = EvCyanPrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )

                    NavigationRailItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Wifi, contentDescription = "เชื่อมต่อ") },
                        label = { Text("เชื่อมต่อ", fontSize = 10.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = EvCyanPrimary,
                            indicatorColor = EvCyanPrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )

                    NavigationRailItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "รถ BYD") },
                        label = { Text("รุ่นรถ", fontSize = 10.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363D),
                            selectedTextColor = EvCyanPrimary,
                            indicatorColor = EvCyanPrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )
                }
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> {
                        if (isLandscape) {
                            LandscapeDashboardScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onNavigateToTab = { selectedTab = it }
                            )
                        } else {
                            DashboardScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onNavigateToTab = { selectedTab = it }
                            )
                        }
                    }
                    1 -> NavPlannerScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    2 -> AdbSetupScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    3 -> VehicleProfileScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }

    // Fullscreen Driving HUD Mode Dialog
    if (uiState.isHudModeOpen) {
        DrivingHudDialog(
            viewModel = viewModel,
            uiState = uiState,
            onDismiss = { viewModel.setHudMode(false) }
        )
    }

    // Manual Speed & Gear Testing Dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = {
                Text("ทดสอบปรับความเร็ว GPS & เกียร์รถยนต์", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                        text = "เลือกเกียร์: P (จอด) | R (ถอย) | N (ว่าง) | D (ขับขี่)",
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
