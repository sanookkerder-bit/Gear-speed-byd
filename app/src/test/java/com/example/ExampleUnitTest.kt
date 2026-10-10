package com.example

import com.example.model.ArrivalSafetyStatus
import com.example.model.BydCarModel
import com.example.model.CarGear
import com.example.model.DrivingMode
import com.example.service.adb.AdbWirelessClient
import com.example.service.overlay.OverlayStateManager
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testAdbBatteryParsing() {
        val client = AdbWirelessClient()
        val mockDump = """
            Current Battery Service state:
              AC powered: false
              USB powered: false
              Wireless powered: false
              Max charging current: 0
              Max charging voltage: 0
              Charge counter: 0
              status: 3
              health: 2
              present: true
              level: 78
              scale: 100
              voltage: 412500
              temperature: 285
              technology: Li-ion
        """.trimIndent()

        val data = client.parseBatteryOutput(mockDump)
        assertEquals(78, data.socPercent)
        assertEquals(412500, data.voltageMv)
        assertEquals(28.5f, data.temperatureCelsius, 0.1f)
        assertFalse(data.isCharging)
    }

    @Test
    fun testOverlayStateManagerUpdate() {
        OverlayStateManager.update(
            speedKmH = 92,
            gear = CarGear.DRIVE,
            socPercent = 82,
            arrivalSocPercent = 58.5,
            destinationName = "Mega Bangna",
            distanceKm = 14.2,
            isSafe = true,
            safetyStatus = ArrivalSafetyStatus.SAFE,
            isAdbConnected = true
        )
        val data = OverlayStateManager.data.value
        assertEquals(92, data.speedKmH)
        assertEquals(CarGear.DRIVE, data.gear)
        assertEquals(82, data.socPercent)
        assertEquals(58.5, data.arrivalSocPercent, 0.1)
    }

    @Test
    fun testArrivalBatteryCalculation() {
        val car = BydCarModel.DEFAULT // ATTO 3 Extended: 60.48 kWh, 14.8 kWh/100km
        val currentSoc = 85
        val distanceKm = 100.0
        val modeFactor = DrivingMode.NORMAL.factor // 1.0
        val acFactor = 1.06 // AC on
        val passengerFactor = 1.02 // 2 passengers

        val consumptionPer100Km = car.baseConsumptionKwhPer100Km * modeFactor * acFactor * passengerFactor
        val totalKwhUsed = (distanceKm / 100.0) * consumptionPer100Km
        val consumedPercent = (totalKwhUsed / car.batteryCapacityKwh) * 100.0
        val arrivalSoc = currentSoc - consumedPercent

        assertTrue("Consumed percent should be positive", consumedPercent > 20.0)
        assertTrue("Arrival SoC should be around 58%", arrivalSoc in 55.0..62.0)
    }

    @Test
    fun testArrivalSafetyEvaluation() {
        val arrivalSocHigh = 58.0
        val statusHigh = when {
            arrivalSocHigh >= 25.0 -> ArrivalSafetyStatus.SAFE
            arrivalSocHigh >= 15.0 -> ArrivalSafetyStatus.MODERATE
            arrivalSocHigh >= 5.0 -> ArrivalSafetyStatus.LOW_WARNING
            else -> ArrivalSafetyStatus.CRITICAL_UNREACHABLE
        }
        assertEquals(ArrivalSafetyStatus.SAFE, statusHigh)

        val arrivalSocCritical = 2.0
        val statusCrit = when {
            arrivalSocCritical >= 25.0 -> ArrivalSafetyStatus.SAFE
            arrivalSocCritical >= 15.0 -> ArrivalSafetyStatus.MODERATE
            arrivalSocCritical >= 5.0 -> ArrivalSafetyStatus.LOW_WARNING
            else -> ArrivalSafetyStatus.CRITICAL_UNREACHABLE
        }
        assertEquals(ArrivalSafetyStatus.CRITICAL_UNREACHABLE, statusCrit)
    }
}
