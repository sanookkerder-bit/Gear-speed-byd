package com.example.model

/**
 * 5-point seatbelt status for BYD vehicle interior:
 * 1. FL (คนขับ Front Left)
 * 2. FR (ผู้โดยสารหน้า Front Right)
 * 3. RL (ผู้โดยสารหลังซ้าย Rear Left)
 * 4. RC (ผู้โดยสารหลังกลาง Rear Center)
 * 5. RR (ผู้โดยสารหลังขวา Rear Right)
 *
 * Reads telemetry from ADB shell commands:
 * - getprop persist.byd.seatbelts
 * - dumpsys car_service (SEAT_BELT_BUCKLED)
 */
data class SeatbeltStatus(
    val driverFastened: Boolean = true,
    val passengerFastened: Boolean = true,
    val rearLeftFastened: Boolean = true,
    val rearCenterFastened: Boolean = true,
    val rearRightFastened: Boolean = true
) {
    val totalSeats: Int = 5

    val fastenedCount: Int
        get() = (if (driverFastened) 1 else 0) +
                (if (passengerFastened) 1 else 0) +
                (if (rearLeftFastened) 1 else 0) +
                (if (rearCenterFastened) 1 else 0) +
                (if (rearRightFastened) 1 else 0)

    val allFastened: Boolean
        get() = fastenedCount == totalSeats

    fun toggleSeat(seatIndex: Int): SeatbeltStatus {
        return when (seatIndex) {
            0 -> copy(driverFastened = !driverFastened)
            1 -> copy(passengerFastened = !passengerFastened)
            2 -> copy(rearLeftFastened = !rearLeftFastened)
            3 -> copy(rearCenterFastened = !rearCenterFastened)
            4 -> copy(rearRightFastened = !rearRightFastened)
            else -> this
        }
    }

    companion object {
        fun fromAdbProp(propValue: String?): SeatbeltStatus {
            if (propValue.isNullOrBlank()) return SeatbeltStatus()
            val clean = propValue.trim()

            // Format: "1,1,1,1,1" or "11111"
            if (clean.contains(",")) {
                val parts = clean.split(",").map { it.trim() == "1" || it.trim().equals("true", true) }
                return SeatbeltStatus(
                    driverFastened = parts.getOrElse(0) { true },
                    passengerFastened = parts.getOrElse(1) { true },
                    rearLeftFastened = parts.getOrElse(2) { true },
                    rearCenterFastened = parts.getOrElse(3) { true },
                    rearRightFastened = parts.getOrElse(4) { true }
                )
            } else if (clean.length == 5 && clean.all { it == '0' || it == '1' }) {
                return SeatbeltStatus(
                    driverFastened = clean[0] == '1',
                    passengerFastened = clean[1] == '1',
                    rearLeftFastened = clean[2] == '1',
                    rearCenterFastened = clean[3] == '1',
                    rearRightFastened = clean[4] == '1'
                )
            }
            return SeatbeltStatus()
        }
    }
}
