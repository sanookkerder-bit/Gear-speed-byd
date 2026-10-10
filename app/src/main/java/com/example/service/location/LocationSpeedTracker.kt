package com.example.service.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class LocationSpeedTracker(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentSpeedKmH = MutableStateFlow(0)
    val currentSpeedKmH: StateFlow<Int> = _currentSpeedKmH.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _isGpsActive = MutableStateFlow(false)
    val isGpsActive: StateFlow<Boolean> = _isGpsActive.asStateFlow()

    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun startTracking(onLocationUpdate: ((Location) -> Unit)? = null) {
        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000L // 1 second update interval for live speed
            ).apply {
                setMinUpdateIntervalMillis(500L)
                setMinUpdateDistanceMeters(0.5f)
            }.build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return
                    _currentLocation.value = location
                    _isGpsActive.value = true

                    // Convert m/s from GPS to km/h (speed * 3.6)
                    val speedKmH = if (location.hasSpeed()) {
                        (location.speed * 3.6f).roundToInt().coerceAtLeast(0)
                    } else {
                        0
                    }
                    _currentSpeedKmH.value = speedKmH
                    onLocationUpdate?.invoke(location)
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback as LocationCallback,
                Looper.getMainLooper()
            )

            // Try getting last known location immediately
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    _currentLocation.value = loc
                    if (loc.hasSpeed()) {
                        _currentSpeedKmH.value = (loc.speed * 3.6f).roundToInt().coerceAtLeast(0)
                    }
                }
            }

        } catch (e: SecurityException) {
            Log.e("LocationSpeedTracker", "Location permission not granted: ${e.message}")
            _isGpsActive.value = false
        } catch (e: Exception) {
            Log.e("LocationSpeedTracker", "Error starting GPS tracking: ${e.message}")
            _isGpsActive.value = false
        }
    }

    fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
        _isGpsActive.value = false
    }

    fun setManualSpeed(speedKmH: Int) {
        _currentSpeedKmH.value = speedKmH.coerceAtLeast(0)
    }
}
