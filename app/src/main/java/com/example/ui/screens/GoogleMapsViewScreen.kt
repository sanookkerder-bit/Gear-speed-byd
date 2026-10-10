package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.ArrivalSafetyStatus
import com.example.model.TripDestination
import com.example.ui.theme.EvBatteryAmber
import com.example.ui.theme.EvBatteryGreen
import com.example.ui.theme.EvBatteryRed
import com.example.ui.theme.EvCyanPrimary
import com.example.viewmodel.BydNavUiState
import com.example.viewmodel.BydNavViewModel

/**
 * Embedded Live Google Maps Navigation Screen
 * Displays Google Maps directly inside the app with the BYD Arrival Battery % HUD overlay.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GoogleMapsViewScreen(
    viewModel: BydNavViewModel,
    uiState: BydNavUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoadingMap by remember { mutableStateOf(true) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val dest = uiState.selectedDestination
    val route = uiState.routeCalculation

    // Construct direct Google Maps Driving Navigation URL
    val mapsUrl = remember(dest?.id) {
        if (dest != null) {
            "https://www.google.com/maps/dir/?api=1&origin=Current+Location&destination=${dest.latitude},${dest.longitude}&travelmode=driving"
        } else {
            "https://www.google.com/maps/@13.7563,100.5018,13z"
        }
    }

    LaunchedEffect(dest?.id) {
        webViewInstance?.loadUrl(mapsUrl)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("embedded_google_maps_screen")
    ) {
        // EMBEDDED GOOGLE MAPS WEBVIEW
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        setGeolocationEnabled(true)
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        // Modern Chrome User-Agent
                        userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onGeolocationPermissionsShowPrompt(
                            origin: String?,
                            callback: GeolocationPermissions.Callback?
                        ) {
                            callback?.invoke(origin, true, false)
                        }
                    }

                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoadingMap = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoadingMap = false
                        }

                        @Suppress("DEPRECATION")
                        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                            isLoadingMap = false
                        }
                    }

                    loadUrl(mapsUrl)
                    webViewInstance = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator
        if (isLoadingMap) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xDD0F172A))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = EvCyanPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "กำลังโหลดแผนที่ Google Maps...",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }

        // TOP-LEFT HUD: BYD DESTINATION ARRIVAL BATTERY % CARD
        if (route != null) {
            val arrivalColor = when (route.safetyStatus) {
                ArrivalSafetyStatus.SAFE -> EvBatteryGreen
                ArrivalSafetyStatus.MODERATE -> EvCyanPrimary
                ArrivalSafetyStatus.LOW_WARNING -> EvBatteryAmber
                ArrivalSafetyStatus.CRITICAL_UNREACHABLE -> EvBatteryRed
            }

            Card(
                modifier = Modifier
                    .padding(top = 10.dp, start = 12.dp)
                    .align(Alignment.TopStart)
                    .width(310.dp)
                    .testTag("maps_arrival_battery_hud"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF2070C1B))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header row: Destination & Distance
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
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(arrivalColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = arrivalColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = route.destination.nameThai,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }

                        Text(
                            text = "${String.format("%.1f", route.distanceKm)} กม.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EvCyanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Arrival Battery Percentage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF141F36))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
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
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = arrivalColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(-${String.format("%.1f", route.consumedSocPercent)}%)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(bottom = 3.dp)
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
                                text = "แบตปัจจุบัน: ${route.currentSoc}%",
                                fontSize = 10.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (route.arrivalSocPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = arrivalColor,
                        trackColor = Color(0xFF334155)
                    )
                }
            }
        }

        // TOP-RIGHT ACTION BUTTONS: Launch Native Maps App & Recenter Map
        Row(
            modifier = Modifier
                .padding(top = 10.dp, end = 12.dp)
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { webViewInstance?.reload() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xDD0F172A),
                    contentColor = EvCyanPrimary
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
            }

            dest?.let { d ->
                Button(
                    onClick = {
                        viewModel.launchGoogleMapsNavigation(context, d)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EvCyanPrimary,
                        contentColor = Color(0xFF00363D)
                    )
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = "Open App", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("เปิดแอพ Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // BOTTOM BAR: QUICK DESTINATION PRESETS & SEARCH
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Search Input expansion
            AnimatedVisibility(visible = isSearchExpanded) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xF20F172A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchDestinationQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("ค้นหาสถานที่ หรือ สถานีชาร์จ (เช่น Central, Mega, PTT)", fontSize = 11.sp, color = Color(0xFF64748B)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EvCyanPrimary,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                if (uiState.searchDestinationQuery.isNotBlank()) {
                                    viewModel.addCustomSearchDestination(uiState.searchDestinationQuery, 22.0)
                                    isSearchExpanded = false
                                }
                            })
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { isSearchExpanded = false }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
            }

            // Quick Destination Chips Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF5070B18))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = EvCyanPrimary, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.destinations) { destination ->
                            val isSelected = uiState.selectedDestination?.id == destination.id
                            FilledTonalButton(
                                onClick = {
                                    viewModel.selectDestination(destination)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isSelected) EvCyanPrimary else Color(0xFF1A2640),
                                    contentColor = if (isSelected) Color(0xFF00363D) else Color.White
                                )
                            ) {
                                if (destination.hasDcFastCharger) {
                                    Icon(Icons.Default.EvStation, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = destination.nameThai,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${destination.distanceKm}k",
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color(0xFF004F58) else EvCyanPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
