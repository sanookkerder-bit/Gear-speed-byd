package com.example.service.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.model.ArrivalSafetyStatus
import com.example.model.CarGear
import com.example.ui.components.IPhoneBatteryView
import com.example.ui.theme.EvBatteryAmber
import com.example.ui.theme.EvBatteryGreen
import com.example.ui.theme.EvBatteryRed
import com.example.ui.theme.EvCyanPrimary
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Floating Overlay Service:
 * Displays the Tesla-style Statusbar over other applications (e.g. Google Maps).
 * Shows live GPS speed (top right), car gears P R N D, and BYD battery % via ADB.
 */
class FloatingOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var overlayComposeView: ComposeView? = null
    private var windowParams: WindowManager.LayoutParams? = null

    companion object {
        private const val CHANNEL_ID = "byd_floating_overlay_channel"
        private const val NOTIFICATION_ID = 2026

        fun start(context: Context) {
            try {
                val intent = Intent(context, FloatingOverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("FloatingOverlayService", "Failed to start service", e)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, FloatingOverlayService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                android.util.Log.e("FloatingOverlayService", "Failed to stop service", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

            startForegroundNotification()
            showOverlayWindow()
            OverlayStateManager.setOverlayRunning(true)
        } catch (e: Exception) {
            android.util.Log.e("FloatingOverlayService", "Error during onCreate of FloatingOverlayService", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        return START_STICKY
    }

    private fun startForegroundNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BYD Statusbar Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "แถบลอยแสดงความเร็ว เกียร์ และแบตเตอรี่ BYD ทับแอพอื่น"
            }
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BYD Cockpit Statusbar Overlay")
            .setContentText("กำลังแสดงแถบความเร็ว เกียร์ และแบตเตอรี่ทับ Google Maps")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun showOverlayWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 20 // Margin from screen top
        }

        overlayComposeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingOverlayService)
            setViewTreeSavedStateRegistryOwner(this@FloatingOverlayService)

            setContent {
                val overlayData by OverlayStateManager.data.collectAsState()
                var isMinimized by remember { mutableStateOf(false) }

                FloatingStatusbarContent(
                    data = overlayData,
                    isMinimized = isMinimized,
                    onToggleMinimize = { isMinimized = !isMinimized },
                    onClose = { stopSelf() },
                    onDrag = { dx, dy ->
                        val lp = windowParams
                        val wm = windowManager
                        val v = overlayComposeView
                        if (lp != null && wm != null && v != null) {
                            lp.x += dx.toInt()
                            lp.y += dy.toInt()
                            try {
                                wm.updateViewLayout(v, lp)
                            } catch (_: Exception) {}
                        }
                    }
                )
            }
        }

        try {
            windowManager?.addView(overlayComposeView, windowParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        OverlayStateManager.setOverlayRunning(false)
        overlayComposeView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayComposeView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

@Composable
fun FloatingStatusbarContent(
    data: OverlayData,
    isMinimized: Boolean,
    onToggleMinimize: () -> Unit,
    onClose: () -> Unit,
    onDrag: (Float, Float) -> Unit
) {
    // 1. Live ticking clock: เวลา วันที่ เดือน ปี ซ้ายมือสุด ตัวหนังสือสีขาว
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEE d MMM yyyy", Locale("th", "TH")) }
    val timeText = timeFormatter.format(calendar.time)
    val dateText = dateFormatter.format(calendar.time)

    // 2. แบตรถและเปอร์เซ็นคงเหลือ -> เมื่อกด สลับแสดงระยะทางคงเหลือ (1% = 2.9 กม. โดยประมาณ)
    var showDistanceMode by remember { mutableStateOf(false) }
    val remainingDistanceKm = data.socPercent * 2.9

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        if (isMinimized) {
            // Compact Floating Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xF0070B19))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .align(Alignment.TopEnd)
                    .clickable { onToggleMinimize() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${data.socPercent}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${data.speedKmH} km/h",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        } else {
            // Full Cockpit Statusbar Floating Window:
            // - ซ้ายมือสุด: เวลา วันที่ เดือน ปี ตัวหนังสือสีขาว
            // - ถัดมาทางซ้ายของเกียร์: แบตรถและเปอร์เซ็น คงเหลือ (กดแล้วสลับแสดง ระยะทางคงเหลือ)
            // - ถัดมาทางซ้ายของความเร็ว: เกียร์ (P R N D)
            // - ขวามือสุด: ความเร็ว ตัวหนังสือ ใหญ่
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xF5060A17))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ซ้ายมือสุด: Drag handle + เวลา วันที่ เดือน ปี ตัวหนังสือสีขาว
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "ลากย้าย",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = timeText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(13.dp)
                            .background(Color.White.copy(alpha = 0.35f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                // กลุ่มทางขวา: แบตรถ & %คงเหลือ (กดสลับระยะทางได้) -> เกียร์ -> ขวามือสุด: ความเร็วตัวหนังสือใหญ่
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // แบตรถและเปอร์เซ็นคงเหลือ (ฟังก์ชันกดสลับแสดงระยะทางคงเหลือ)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF131D33))
                            .clickable { showDistanceMode = !showDistanceMode }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IPhoneBatteryView(
                            socPercent = data.socPercent,
                            isCharging = false,
                            showPercentageText = false
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (!showDistanceMode) {
                            Text(
                                text = "${data.socPercent}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontFamily = FontFamily.SansSerif
                            )
                        } else {
                            Text(text = "🚗", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.1f", remainingDistanceKm)} กม.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "⇄",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // เกียร์ (P R N D)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF131D33))
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(CarGear.PARK, CarGear.REVERSE, CarGear.NEUTRAL, CarGear.DRIVE).forEach { gear ->
                            val isSelected = data.gear == gear
                            Text(
                                text = gear.symbol,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Color(0xFF475569)
                            )
                        }
                    }

                    // ขวามือสุด เป็นความเร็ว ตัวหนังสือ ใหญ่
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${data.speedKmH}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 32.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "km/h",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    // ปุ่มย่อ/ปิดขนาดเล็ก
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
