package com.deepal.sdk

/**
 * Immutable vehicle telemetry data model representing the live state of Deepal S05 / EPA platform.
 *
 * Ground truth properties and area masks verified against Changan OpenOS framework
 */
data class DeepalS05Telemetry(
    // Powertrain & Dynamics
    val speedKmh: Float = 0f,
    val gear: String = "P",
    val batterySocPercent: Int = 0,
    val remainingRangeKm: Int = 0,
    val evRemainingRangeKm: Int = 0,
    val odometerKm: Float = 0f,
    val exteriorTempC: Float = 25.0f,
    val cabinInternalTempC: Float = 24.0f,
    val driveMode: String = "COMFORT",
    val isPowerOn: Boolean = true,

    // Tire Pressure Telemetry (in Bar, property 0x37600211)
    val tirePressureFlBar: Float = 2.4f,
    val tirePressureFrBar: Float = 2.4f,
    val tirePressureRlBar: Float = 2.4f,
    val tirePressureRrBar: Float = 2.4f,

    // Trip & REEV Energy Metrics
    val tripElecAvgKwhPer100Km: Float = 0f,
    val tripOilAvgLPer100Km: Float = 0f,
    val tripElecDistanceKm: Float = 0f,
    val tripElecTimeMinutes: Int = 0,
    val tripFuelDistanceKm: Float = 0f,
    val tripFuelTimeMinutes: Int = 0,

    // Climate & HVAC
    val isClimatePowerOn: Boolean = false,
    val isAcOn: Boolean = true,
    val isAutoClimateOn: Boolean = true,
    val climateTempC: Float = 24.0f,
    val passengerTempC: Float = 24.0f,
    val fanSpeed: Int = 1,
    val windDirection: Int = DeepalS05Property.WIND_DIRECTION_FACE, // 8=Defrost, 9=Face, 10=Feet, 11=Dual
    val isRecirculationOn: Boolean = false,
    val isFrontDefrostOn: Boolean = false,
    val isRearDefrostOn: Boolean = false,
    val isSteeringWheelHeatOn: Boolean = false,

    // Seats & Comfort (Driver & Passenger)
    val driverSeatHeat: Int = 0,     // 0=Off, 1=Low, 2=Med, 3=High
    val passengerSeatHeat: Int = 0,
    val driverSeatVent: Int = 0,     // 0=Off, 1=Low, 2=Med, 3=High
    val passengerSeatVent: Int = 0,
    val isSeatMassageOn: Boolean = false,
    val seatMassageMode: Int = 1,    // 1..8 (Pneumatic massage pattern)
    val seatMassageLevel: Int = 1,   // 1..3 (Pneumatic massage intensity level)
    val isPassengerSeatMassageOn: Boolean = false,
    val passengerSeatMassageMode: Int = 1,  // 1..8
    val passengerSeatMassageLevel: Int = 1, // 1..3

    // Doors, Windows & Body Access
    val isDoorLocked: Boolean = true,
    val doorHandlesExpanded: Boolean = false,
    val mirrorsFolded: Boolean = false,
    val doorFlOpen: Boolean = false,
    val doorFrOpen: Boolean = false,
    val doorRlOpen: Boolean = false,
    val doorRrOpen: Boolean = false,
    val isTailgateOpen: Boolean = false,
    val isSunroofOpen: Boolean = false,
    val isWindowsOpen: Boolean = false,
    val windowFlOpen: Boolean = false,
    val windowFrOpen: Boolean = false,
    val windowRlOpen: Boolean = false,
    val windowRrOpen: Boolean = false,

    // Lighting & Cabin Environment
    val ambientLightColor: Int = 1,
    val ambientLightBrightness: Int = 60,
    val ambientLightPattern: Int = 1,     // 1..3 (Dynamic pattern effect)
    val ambientLightColorChoice: Int = 54, // Color preset code (54, 42, 33, 12, 6, 1)
    val isAirPurifierOn: Boolean = false,
    val isAebOn: Boolean = true,

    // Outside Audio & Speaker
    val isOutsideMusicPlaying: Boolean = false,

    // Intelligent Automation Signals
    val isBatteryPreconditioning: Boolean = false,
    val rainSensorState: Int = 1, // 1=No Rain, 2=Light Rain, 3=Heavy Rain

    // Vehicle Identification & Platform
    val detectedProfileId: String = "deepal-s05-c857",

    // Connection Status
    val isVirtualCarConnected: Boolean = false
)
