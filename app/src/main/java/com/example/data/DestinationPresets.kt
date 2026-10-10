package com.example.data

import com.example.model.DestinationCategory
import com.example.model.TripDestination

object DestinationPresets {
    val POPULAR_DESTINATIONS = listOf(
        TripDestination(
            id = "ptt_bangna_ev",
            nameThai = "PTT EV Station PluX บางนา-ตราด กม.14",
            nameEn = "PTT EV Station PluX Bangna-Trad KM.14",
            category = DestinationCategory.CHARGING_HUB,
            distanceKm = 18.5,
            latitude = 13.6425,
            longitude = 100.7180,
            hasDcFastCharger = true,
            chargingProvider = "PTT EV Station PluX (160 kW DC CCS2)"
        ),
        TripDestination(
            id = "mega_bangna",
            nameThai = "เมกาบางนา (Mega Bangna)",
            nameEn = "Mega Bangna",
            category = DestinationCategory.MALL,
            distanceKm = 14.2,
            latitude = 13.6468,
            longitude = 100.6806,
            hasDcFastCharger = true,
            chargingProvider = "EA Anywhere Super Charge (150 kW DC)"
        ),
        TripDestination(
            id = "central_world",
            nameThai = "เซ็นทรัลเวิลด์ (CentralWorld ราชประสงค์)",
            nameEn = "CentralWorld Bangkok",
            category = DestinationCategory.MALL,
            distanceKm = 22.0,
            latitude = 13.7466,
            longitude = 100.5393,
            hasDcFastCharger = true,
            chargingProvider = "Evolt / On-ion (120 kW DC CCS2)"
        ),
        TripDestination(
            id = "suvarnabhumi",
            nameThai = "สนามบินสุวรรณภูมิ (Suvarnabhumi Airport)",
            nameEn = "Suvarnabhumi International Airport",
            category = DestinationCategory.MALL,
            distanceKm = 28.5,
            latitude = 13.6900,
            longitude = 100.7501,
            hasDcFastCharger = true,
            chargingProvider = "PEA VOLTA Suvarnabhumi (100 kW DC)"
        ),
        TripDestination(
            id = "ayutthaya_historic",
            nameThai = "พระนครศรีอยุธยา (อุทยานประวัติศาสตร์)",
            nameEn = "Ayutthaya Historical Park",
            category = DestinationCategory.PROVINCE,
            distanceKm = 78.0,
            latitude = 14.3532,
            longitude = 100.5684,
            hasDcFastCharger = true,
            chargingProvider = "PEA VOLTA จุดพักอยุธยา (120 kW DC)"
        ),
        TripDestination(
            id = "pattaya_beach",
            nameThai = "พัทยาเหนือ - หาดวงอมาตย์",
            nameEn = "North Pattaya Beach",
            category = DestinationCategory.PROVINCE,
            distanceKm = 142.0,
            latitude = 12.9567,
            longitude = 100.8872,
            hasDcFastCharger = true,
            chargingProvider = "PTT EV Station PluX มอเตอร์เวย์ (180 kW DC)"
        ),
        TripDestination(
            id = "khao_yai",
            nameThai = "อุทยานแห่งชาติเขาใหญ่ (ปากช่อง)",
            nameEn = "Khao Yai National Park (Pak Chong)",
            category = DestinationCategory.PROVINCE,
            distanceKm = 168.0,
            latitude = 14.4392,
            longitude = 101.3723,
            hasDcFastCharger = true,
            chargingProvider = "PEA VOLTA มิตรภาพ-ปากช่อง (120 kW DC)"
        ),
        TripDestination(
            id = "hua_hin_market",
            nameThai = "หัวหิน มาร์เก็ตวิลเลจ (Hua Hin)",
            nameEn = "Hua Hin Market Village",
            category = DestinationCategory.PROVINCE,
            distanceKm = 195.0,
            latitude = 12.5539,
            longitude = 99.9602,
            hasDcFastCharger = true,
            chargingProvider = "PTT EV Station PluX วังมะนาว / ชะอำ (160 kW DC)"
        ),
        TripDestination(
            id = "korat_city",
            nameThai = "นครราชสีมา (อนุสาวรีย์ท้าวสุรนารี)",
            nameEn = "Nakhon Ratchasima (Korat)",
            category = DestinationCategory.PROVINCE,
            distanceKm = 255.0,
            latitude = 14.9744,
            longitude = 102.0978,
            hasDcFastCharger = true,
            chargingProvider = "PEA VOLTA ปั๊มบางจาก สีคิ้ว (150 kW DC)"
        ),
        TripDestination(
            id = "chiang_mai",
            nameThai = "เชียงใหม่ (ประตูท่าแพ)",
            nameEn = "Chiang Mai (Tha Phae Gate)",
            category = DestinationCategory.PROVINCE,
            distanceKm = 692.0,
            latitude = 18.7877,
            longitude = 98.9931,
            hasDcFastCharger = true,
            chargingProvider = "PEA VOLTA สายเอเชีย / เครือข่ายสถานีชาร์จภาคเหนือ"
        )
    )
}
