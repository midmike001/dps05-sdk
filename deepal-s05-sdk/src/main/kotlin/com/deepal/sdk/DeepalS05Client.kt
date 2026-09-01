package com.deepal.sdk

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Main SDK Client for Changan Deepal S05 vehicle interaction.
 *
 * Provides reactive StateFlow telemetry monitoring and asynchronous actuation methods
 * for Dual-Zone Climate, Seat Comfort, Windows/Doors, Sunroof Shade, and Next-Gen EV features.
 */
class DeepalS05Client(
    val connection: VirtualCarConnection = VirtualCarConnection(),
    val hudClient: DeepalHudClient = DeepalHudClient(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    companion object {
        private const val TAG = "DeepalS05Client"
    }

    private val _telemetry = MutableStateFlow(DeepalS05Telemetry())
    val telemetry: StateFlow<DeepalS05Telemetry> = _telemetry.asStateFlow()

    private var pollingJob: Job? = null
    var isSimulatedMode: Boolean = false

    /**
     * Allows simulated test environments to push mock telemetry states directly.
     */
    fun updateTelemetry(update: (DeepalS05Telemetry) -> DeepalS05Telemetry) {
        _telemetry.value = update(_telemetry.value)
    }

    /**
     * Starts continuous polling of CAN/LIN and VirtualCar vehicle telemetry.
     * High-speed telemetry (live speed, gear) polls at 250ms; slow metrics poll every 1000ms.
     */
    fun startMonitoring() {
        if (pollingJob != null) return

        pollingJob = scope.launch {
            var slowTick = 0
            while (isActive) {
                try {
                    val propertyBinder = connection.getPropertyBinder()
                    if (propertyBinder != null && propertyBinder.isBinderAlive) {
                        // High-frequency telemetry (every 250ms)
                        val speedKmh = connection.getFloatProperty(DeepalS05Property.PROP_VEHICLE_SPEED_VC)
                            ?: connection.getFloatProperty(DeepalS05Property.PROP_VEHICLE_SPEED_VHAL)
                            ?: _telemetry.value.speedKmh

                        // Reverse-engineered OpenOS Gear mapping: 0/4=P, 1=N, 2=R, 3/8=D
                        val gearCode = connection.getIntProperty(DeepalS05Property.PROP_GEAR_SELECTION)
                        val gearStr = when (gearCode) {
                            0, 4 -> "P"
                            1 -> "N"
                            2 -> "R"
                            3, 8 -> "D"
                            else -> _telemetry.value.gear
                        }

                        // Slower metrics (every 1000ms = 4 ticks)
                        if (slowTick % 4 == 0) {
                            val soc = connection.getIntProperty(DeepalS05Property.PROP_BATTERY_SOC, DeepalS05Property.AREA_SOC)
                                ?: _telemetry.value.batterySocPercent

                            val evDte = connection.getIntProperty(DeepalS05Property.PROP_REMAINING_RANGE_EV_DTE, DeepalS05Property.AREA_GLOBAL)
                                ?: _telemetry.value.evRemainingRangeKm

                            val range = connection.getIntProperty(DeepalS05Property.PROP_REMAINING_RANGE_DISP_DTE, DeepalS05Property.AREA_GLOBAL)
                                ?: evDte

                            val odoRaw = connection.getFloatProperty(DeepalS05Property.PROP_ODOMETER)
                            val odoKm = if (odoRaw != null) odoRaw / DeepalS05Property.ODOMETER_SCALE_DIVISOR else _telemetry.value.odometerKm

                            val temp = connection.getFloatProperty(DeepalS05Property.PROP_HVAC_TEMP_SET, DeepalS05Property.AREA_DRIVER)
                                ?: _telemetry.value.climateTempC

                            val cabinInsideTemp = connection.getFloatProperty(DeepalS05Property.PROP_HVAC_INTERNAL_TEMP, DeepalS05Property.AREA_DRIVER)
                                ?: _telemetry.value.cabinInternalTempC

                            val fan = connection.getIntProperty(DeepalS05Property.PROP_HVAC_FAN_SPEED, DeepalS05Property.AREA_DRIVER)
                                ?: _telemetry.value.fanSpeed

                            val wind = connection.getIntProperty(DeepalS05Property.PROP_HVAC_FAN_DIRECTION, DeepalS05Property.AREA_DRIVER)
                                ?: _telemetry.value.windDirection

                            val driveCode = connection.getIntProperty(DeepalS05Property.PROP_DRIVE_MODE, DeepalS05Property.AREA_GLOBAL)
                            val driveModeStr = when (driveCode) {
                                DeepalS05Property.DRIVE_MODE_COMFORT -> "COMFORT"
                                DeepalS05Property.DRIVE_MODE_SPORT -> "SPORT"
                                DeepalS05Property.DRIVE_MODE_ECO -> "ECO"
                                DeepalS05Property.DRIVE_MODE_CUSTOM -> "CUSTOM"
                                else -> _telemetry.value.driveMode
                            }

                            val isPrecond = connection.getIntProperty(DeepalS05Property.PROP_BATTERY_PRECONDITIONING) == 1
                            val rainState = connection.getIntProperty(DeepalS05Property.PROP_RAIN_SENSOR_STATE) ?: _telemetry.value.rainSensorState

                            // Individual Door open states (Area bitmasks: FL=0x01, FR=0x04, RL=0x10, RR=0x40)
                            val flOpen = connection.getIntProperty(DeepalS05Property.PROP_DOORS, DeepalS05Property.AREA_DOOR_FL) == 1
                            val frOpen = connection.getIntProperty(DeepalS05Property.PROP_DOORS, DeepalS05Property.AREA_DOOR_FR) == 1
                            val rlOpen = connection.getIntProperty(DeepalS05Property.PROP_DOORS, DeepalS05Property.AREA_DOOR_RL) == 1
                            val rrOpen = connection.getIntProperty(DeepalS05Property.PROP_DOORS, DeepalS05Property.AREA_DOOR_RR) == 1
                            val trunkOpen = connection.getIntProperty(DeepalS05Property.PROP_TAILGATE_STATUS, DeepalS05Property.AREA_GLOBAL) == 1

                            // Sunshade position from wt.vehiclesetting
                            val shadePercent = connection.getSunshadePos()
                            val isSunshadeOpen = if (shadePercent != null) shadePercent > 0 else _telemetry.value.isSunroofOpen

                            // Tire pressure telemetry (Bar)
                            val tireFl = connection.getFloatProperty(DeepalS05Property.PROP_TIRE_PRESSURE, DeepalS05Property.AREA_TIRE_FL) ?: _telemetry.value.tirePressureFlBar
                            val tireFr = connection.getFloatProperty(DeepalS05Property.PROP_TIRE_PRESSURE, DeepalS05Property.AREA_TIRE_FR) ?: _telemetry.value.tirePressureFrBar
                            val tireRl = connection.getFloatProperty(DeepalS05Property.PROP_TIRE_PRESSURE, DeepalS05Property.AREA_TIRE_RL) ?: _telemetry.value.tirePressureRlBar
                            val tireRr = connection.getFloatProperty(DeepalS05Property.PROP_TIRE_PRESSURE, DeepalS05Property.AREA_TIRE_RR) ?: _telemetry.value.tirePressureRrBar

                            // Energy telemetry
                            val elecAvg = connection.getFloatProperty(DeepalS05Property.PROP_THIS_TRIP_ELEC_AVG_CONSUMPTION, DeepalS05Property.AREA_GLOBAL) ?: _telemetry.value.tripElecAvgKwhPer100Km
                            val oilAvg = connection.getFloatProperty(DeepalS05Property.PROP_THIS_TRIP_OIL_AVG_CONSUMPTION, DeepalS05Property.AREA_GLOBAL) ?: _telemetry.value.tripOilAvgLPer100Km
                            val elecDist = connection.getFloatProperty(DeepalS05Property.PROP_THIS_TRIP_REEV_ELEC_DISTANCE, DeepalS05Property.AREA_GLOBAL) ?: _telemetry.value.tripElecDistanceKm
                            val elecTime = connection.getIntProperty(DeepalS05Property.PROP_THIS_TRIP_REEV_ELEC_TIME, DeepalS05Property.AREA_GLOBAL) ?: _telemetry.value.tripElecTimeMinutes
                            val fuelDist = connection.getFloatProperty(DeepalS05Property.PROP_THIS_TRIP_REEV_FUEL_DISTANCE, DeepalS05Property.AREA_GLOBAL) ?: _telemetry.value.tripFuelDistanceKm
                            val fuelTime = connection.getIntProperty(DeepalS05Property.PROP_THIS_TRIP_REEV_FUEL_TIME, DeepalS05Property.AREA_GLOBAL) ?: _telemetry.value.tripFuelTimeMinutes

                            _telemetry.value = _telemetry.value.copy(
                                speedKmh = speedKmh,
                                gear = gearStr,
                                batterySocPercent = soc,
                                remainingRangeKm = range,
                                evRemainingRangeKm = evDte,
                                odometerKm = odoKm,
                                climateTempC = temp,
                                cabinInternalTempC = cabinInsideTemp,
                                fanSpeed = fan,
                                windDirection = wind,
                                driveMode = driveModeStr,
                                isBatteryPreconditioning = isPrecond,
                                rainSensorState = rainState,
                                doorFlOpen = flOpen,
                                doorFrOpen = frOpen,
                                doorRlOpen = rlOpen,
                                doorRrOpen = rrOpen,
                                isTailgateOpen = trunkOpen,
                                isSunroofOpen = isSunshadeOpen,
                                tirePressureFlBar = tireFl,
                                tirePressureFrBar = tireFr,
                                tirePressureRlBar = tireRl,
                                tirePressureRrBar = tireRr,
                                tripElecAvgKwhPer100Km = elecAvg,
                                tripOilAvgLPer100Km = oilAvg,
                                tripElecDistanceKm = elecDist,
                                tripElecTimeMinutes = elecTime,
                                tripFuelDistanceKm = fuelDist,
                                tripFuelTimeMinutes = fuelTime,
                                isVirtualCarConnected = true
                            )
                        } else {
                            _telemetry.value = _telemetry.value.copy(
                                speedKmh = speedKmh,
                                gear = gearStr,
                                isVirtualCarConnected = true
                            )
                        }
                    } else {
                        if (!isSimulatedMode) {
                            _telemetry.value = _telemetry.value.copy(isVirtualCarConnected = false)
                        }
                    }
                } catch (e: Throwable) {
                    Log.w(TAG, "Polling error: ${e.message}")
                }
                slowTick++
                delay(250)
            }
        }
    }

    fun stopMonitoring() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // ==========================================
    // 1. Dual-Zone Climate & Defrost Controls
    // ==========================================

    suspend fun setClimatePower(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isClimatePowerOn = enabled)
        val a = connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_POWER_ON,
            areaId = DeepalS05Property.AREA_DRIVER,
            value = if (enabled) 1 else 2
        )
        val b = connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_POWER_ON,
            areaId = DeepalS05Property.AREA_GLOBAL,
            value = if (enabled) 1 else 2
        )
        a || b
    }

    suspend fun setClimateTemperature(tempC: Float, area: Int = DeepalS05Property.AREA_DRIVER): Boolean = withContext(Dispatchers.IO) {
        val clamped = tempC.coerceIn(17.5f, 32.5f)
        _telemetry.value = if (area == DeepalS05Property.AREA_DRIVER) {
            _telemetry.value.copy(climateTempC = clamped)
        } else {
            _telemetry.value.copy(passengerTempC = clamped)
        }
        connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_TEMP_SET,
            areaId = area,
            value = clamped
        )
    }

    suspend fun setFanSpeed(speed: Int): Boolean = withContext(Dispatchers.IO) {
        val clamped = speed.coerceIn(1, 8)
        _telemetry.value = _telemetry.value.copy(fanSpeed = clamped)
        connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_FAN_SPEED,
            areaId = DeepalS05Property.AREA_DRIVER,
            value = clamped
        )
    }

    suspend fun setAcEnabled(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isAcOn = enabled)
        connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_AC_ON,
            areaId = DeepalS05Property.AREA_DRIVER,
            value = if (enabled) 1 else 2
        )
    }

    suspend fun setAutoClimate(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isAutoClimateOn = enabled)
        val a = connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_AUTO,
            areaId = DeepalS05Property.AREA_DRIVER,
            value = if (enabled) 1 else 2
        )
        val b = connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_AUTO,
            areaId = DeepalS05Property.AREA_GLOBAL,
            value = if (enabled) 1 else 2
        )
        a || b
    }

    suspend fun setRecirculation(recircOn: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isRecirculationOn = recircOn)
        connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_RECIRC,
            areaId = DeepalS05Property.AREA_DRIVER,
            value = if (recircOn) 2 else 1 // Vendor tri-state: 2=Recirc, 1=Fresh
        )
    }

    suspend fun setMaxAc(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_MAX_AC,
            areaId = DeepalS05Property.AREA_DRIVER,
            value = if (enabled) 1 else 2
        )
    }

    suspend fun setSyncMode(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_SYNC,
            areaId = DeepalS05Property.AREA_DRIVER,
            value = if (enabled) 1 else 2
        )
    }

    suspend fun setFrontDefrost(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isFrontDefrostOn = enabled)
        connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_DEFROST_FRONT,
            areaId = DeepalS05Property.AREA_DRIVER,
            value = if (enabled) 1 else 2
        )
    }

    suspend fun setRearDefrost(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isRearDefrostOn = enabled)
        val a = connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_DEFROST_REAR,
            areaId = DeepalS05Property.AREA_DRIVER,
            value = if (enabled) 1 else 2
        )
        val b = connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_DEFROST_FRONT,
            areaId = 2, // Area 2 in 0x33400103 controls rear defrost
            value = if (enabled) 1 else 2
        )
        a || b
    }

    /**
     * Sets the HVAC airflow wind distribution direction.
     * @param direction: WIND_DIRECTION_DEFROST (8), WIND_DIRECTION_FACE (9), WIND_DIRECTION_FEET (10), or WIND_DIRECTION_FACE_FEET (11)
     */
    suspend fun setWindDirection(direction: Int): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(windDirection = direction)
        val a = connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_FAN_DIRECTION,
            areaId = DeepalS05Property.AREA_DRIVER,
            value = direction
        )
        val b = connection.setProperty(
            propId = DeepalS05Property.PROP_HVAC_FAN_DIRECTION,
            areaId = DeepalS05Property.AREA_GLOBAL,
            value = direction
        )
        a || b
    }

    /**
     * Sets vehicle driving dynamics style.
     * @param mode: DRIVE_MODE_COMFORT (1), DRIVE_MODE_SPORT (2), DRIVE_MODE_ECO (3), DRIVE_MODE_CUSTOM (4)
     */
    suspend fun setDriveMode(mode: Int): Boolean = withContext(Dispatchers.IO) {
        val modeStr = when (mode) {
            DeepalS05Property.DRIVE_MODE_COMFORT -> "COMFORT"
            DeepalS05Property.DRIVE_MODE_SPORT -> "SPORT"
            DeepalS05Property.DRIVE_MODE_ECO -> "ECO"
            DeepalS05Property.DRIVE_MODE_CUSTOM -> "CUSTOM"
            else -> "COMFORT"
        }
        _telemetry.value = _telemetry.value.copy(driveMode = modeStr)
        connection.setProperty(
            propId = DeepalS05Property.PROP_DRIVE_MODE,
            areaId = DeepalS05Property.AREA_GLOBAL,
            value = mode
        )
    }

    // ==========================================
    // 2. Seat Comfort & Massage Controls
    // ==========================================

    suspend fun setSeatHeating(level: Int, area: Int = DeepalS05Property.AREA_DRIVER): Boolean = withContext(Dispatchers.IO) {
        val clamped = level.coerceIn(0, 3)
        _telemetry.value = if (area == DeepalS05Property.AREA_DRIVER) {
            _telemetry.value.copy(driverSeatHeat = clamped)
        } else {
            _telemetry.value.copy(passengerSeatHeat = clamped)
        }
        val vcOk = connection.setProperty(
            propId = DeepalS05Property.PROP_SEAT_HEATING,
            areaId = area,
            value = clamped
        )
        val cpmOk = connection.setProperty(
            propId = DeepalS05Property.PROP_SEAT_HEATING_CPM,
            areaId = area,
            value = clamped
        )
        vcOk || cpmOk
    }

    suspend fun setSeatVentilation(level: Int, area: Int = DeepalS05Property.AREA_DRIVER): Boolean = withContext(Dispatchers.IO) {
        val clamped = level.coerceIn(0, 3)
        _telemetry.value = if (area == DeepalS05Property.AREA_DRIVER) {
            _telemetry.value.copy(driverSeatVent = clamped)
        } else {
            _telemetry.value.copy(passengerSeatVent = clamped)
        }
        connection.setProperty(
            propId = DeepalS05Property.PROP_SEAT_VENTILATION,
            areaId = area,
            value = clamped
        )
    }

    suspend fun setSeatMassage(enabled: Boolean, mode: Int = 1, level: Int = 2): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(
            isSeatMassageOn = enabled,
            seatMassageMode = mode,
            seatMassageLevel = level
        )
        connection.setProperty(
            propId = DeepalS05Property.PROP_SEAT_MASSAGE_TOGGLE,
            areaId = DeepalS05Property.AREA_GLOBAL,
            value = if (enabled) 1 else 2
        )
        if (enabled) {
            connection.setProperty(DeepalS05Property.PROP_SEAT_MASSAGE_MODE, DeepalS05Property.AREA_GLOBAL, mode)
            connection.setProperty(DeepalS05Property.PROP_SEAT_MASSAGE_LEVEL, DeepalS05Property.AREA_GLOBAL, level)
        }
        true
    }

    suspend fun setSteeringWheelHeat(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isSteeringWheelHeatOn = enabled)
        connection.setProperty(
            propId = DeepalS05Property.PROP_STEERING_WHEEL_HEAT,
            areaId = DeepalS05Property.AREA_GLOBAL,
            value = if (enabled) 1 else 2
        )
    }

    // ==========================================
    // 3. Windows, Sunroof, Tailgate & Central Locks
    // ==========================================

    /**
     * Controls all four power windows.
     * @param action: 1 = Full Express Open (100%), 2 = Full Express Close (0%), 0 = Stop, 3 = Vent (15% gap).
     * Commands both PROP_WINDOW_POS (0..100%) and PROP_WINDOW_MOVE (-100..100) for full express travel.
     */
    suspend fun setWindows(action: Int): Boolean = withContext(Dispatchers.IO) {
        val isOpen = action == 1 || action == 3
        _telemetry.value = _telemetry.value.copy(
            isWindowsOpen = isOpen,
            windowFlOpen = isOpen,
            windowFrOpen = isOpen,
            windowRlOpen = isOpen,
            windowRrOpen = isOpen
        )
        val areas = listOf(
            DeepalS05Property.AREA_WINDOW_FL,
            DeepalS05Property.AREA_WINDOW_FR,
            DeepalS05Property.AREA_WINDOW_RL,
            DeepalS05Property.AREA_WINDOW_RR
        )
        val targetPos = when (action) {
            1 -> 100 // 100% full down / open
            2 -> 0   // 0% full up / closed
            3 -> 15  // 15% vent gap
            else -> 0
        }
        val moveRate = when (action) {
            1 -> 100   // Express down / open
            2 -> -100  // Express up / close
            0 -> 0     // Stop
            3 -> 15    // Vent
            else -> 0
        }
        for (area in areas) {
            if (action != 0) {
                connection.setProperty(DeepalS05Property.PROP_WINDOW_POS, area, targetPos)
                connection.setProperty(DeepalS05Property.PROP_WINDOW_POS_VC, area, targetPos)
            }
            connection.setProperty(DeepalS05Property.PROP_WINDOW_MOVE, area, moveRate)
        }
        true
    }

    /**
     * Sets a specific window position percentage (0..100%).
     * @param percent: 0 = Fully Closed, 100 = Fully Open.
     * @param area: Window area mask (AREA_WINDOW_FL, AREA_WINDOW_FR, AREA_WINDOW_RL, AREA_WINDOW_RR).
     */
    suspend fun setWindowPosition(percent: Int, area: Int): Boolean = withContext(Dispatchers.IO) {
        val clamped = percent.coerceIn(0, 100)
        val moveRate = if (clamped == 0) -100 else if (clamped == 100) 100 else clamped
        connection.setProperty(DeepalS05Property.PROP_WINDOW_POS, area, clamped)
        connection.setProperty(DeepalS05Property.PROP_WINDOW_POS_VC, area, clamped)
        connection.setProperty(DeepalS05Property.PROP_WINDOW_MOVE, area, moveRate)
    }

    /**
     * Controls an individual power window.
     * @param area: Window area mask.
     * @param action: 1 = Full Express Open, 2 = Full Express Close, 0 = Stop, 3 = Vent (15% gap).
     */
    suspend fun setWindow(area: Int, action: Int): Boolean = withContext(Dispatchers.IO) {
        val targetPos = when (action) {
            1 -> 100
            2 -> 0
            3 -> 15
            else -> 0
        }
        val moveRate = when (action) {
            1 -> 100
            2 -> -100
            0 -> 0
            3 -> 15
            else -> 0
        }
        if (action != 0) {
            connection.setProperty(DeepalS05Property.PROP_WINDOW_POS, area, targetPos)
            connection.setProperty(DeepalS05Property.PROP_WINDOW_POS_VC, area, targetPos)
        }
        connection.setProperty(DeepalS05Property.PROP_WINDOW_MOVE, area, moveRate)
    }

    /**
     * Controls the electric sunroof sunshade roller blind.
     * @param actionOrPercent: 1 = Open (100%), 2 = Close (0%), 0 = Stop (50%), or direct percent 0..100.
     * Dispatched via wt.vehiclesetting service (IVehicleSettingInterface.setSunshadePos).
     */
    suspend fun setSunroofShade(actionOrPercent: Int): Boolean = withContext(Dispatchers.IO) {
        val targetPercent = when (actionOrPercent) {
            1 -> 100
            2 -> 0
            0 -> 50
            else -> actionOrPercent.coerceIn(0, 100)
        }
        _telemetry.value = _telemetry.value.copy(isSunroofOpen = targetPercent > 0)
        connection.setSunshadePos(targetPercent)
    }

    /**
     * Sets the electric sunshade position directly as a percentage (0..100).
     */
    suspend fun setSunshadePercent(percent: Int): Boolean = withContext(Dispatchers.IO) {
        val clamped = percent.coerceIn(0, 100)
        _telemetry.value = _telemetry.value.copy(isSunroofOpen = clamped > 0)
        connection.setSunshadePos(clamped)
    }

    /**
     * Controls the sunroof glass position (0..100%).
     */
    suspend fun setSunroof(posOrPercent: Int): Boolean = withContext(Dispatchers.IO) {
        val target = when (posOrPercent) {
            1 -> 100
            2 -> 0
            else -> posOrPercent.coerceIn(0, 100)
        }
        connection.setSunroofPos(target)
    }

    /**
     * Controls the sunroof tilt status (true = Tilt / Vent, false = Closed).
     */
    suspend fun setSunroofTilt(tilt: Boolean): Boolean = withContext(Dispatchers.IO) {
        connection.setSunroofTiltStatus(if (tilt) 1 else 0)
    }

    /**
     * Actuates the power liftgate / trunk.
     * Dispatches command to PROP_TAILGATE_CONTROL (0x31400313): 1=Open, 2=Close.
     */
    suspend fun setTailgate(open: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isTailgateOpen = open)
        connection.setProperty(
            propId = DeepalS05Property.PROP_TAILGATE_CONTROL,
            areaId = DeepalS05Property.AREA_GLOBAL,
            value = if (open) 1 else 2
        )
    }

    suspend fun setDoorLock(locked: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isDoorLocked = locked)
        connection.setProperty(
            propId = DeepalS05Property.PROP_DOOR_LOCK,
            areaId = DeepalS05Property.AREA_GLOBAL,
            value = if (locked) 1 else 2
        )
    }

    /**
     * Controls motorized electric flush door handles (expand or retract).
     */
    suspend fun setDoorHandleExpanded(expanded: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(doorHandlesExpanded = expanded)
        // Dispatches to VirtualCar body control layer
        connection.setProperty(
            propId = 0x314003ec,
            areaId = DeepalS05Property.AREA_GLOBAL,
            value = if (expanded) 1 else 2
        )
    }

    /**
     * Controls rearview power folding side mirrors.
     */
    suspend fun setMirrorFold(folded: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(mirrorsFolded = folded)
        connection.setProperty(
            propId = 0x314003ed,
            areaId = DeepalS05Property.AREA_GLOBAL,
            value = if (folded) 1 else 2
        )
    }

    /**
     * Triggers fuel port / charging port solenoid release.
     */
    suspend fun openFuelCap(): Boolean = withContext(Dispatchers.IO) {
        connection.setProperty(
            propId = 0x314003ee,
            areaId = DeepalS05Property.AREA_GLOBAL,
            value = 1
        )
    }

    /**
     * Triggers electronic glove box release solenoid.
     */
    suspend fun openGloveBox(): Boolean = withContext(Dispatchers.IO) {
        connection.setProperty(
            propId = 0x314003ef,
            areaId = DeepalS05Property.AREA_GLOBAL,
            value = 1
        )
    }

    // ==========================================
    // 4. Lighting & Air Quality
    // ==========================================

    suspend fun setAmbientLight(colorIndex: Int, brightness: Int = 60): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(
            ambientLightColor = colorIndex,
            ambientLightBrightness = brightness
        )
        connection.setProperty(DeepalS05Property.PROP_AMBIENT_LIGHT, DeepalS05Property.AREA_GLOBAL, colorIndex)
        connection.setProperty(DeepalS05Property.PROP_AMBIENT_LIGHT_BRIGHTNESS, DeepalS05Property.AREA_GLOBAL, brightness)
    }

    suspend fun setAirPurifier(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isAirPurifierOn = enabled)
        connection.setProperty(
            propId = DeepalS05Property.PROP_AIR_PURIFIER,
            areaId = DeepalS05Property.AREA_GLOBAL,
            value = if (enabled) 1 else 2
        )
    }

    // ==========================================
    // 5. Vehicle Settings (wt.vehiclesetting)
    // ==========================================

    /**
     * Sets automatic side mirror folding upon vehicle central locking.
     */
    suspend fun setMirrorAutofold(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        connection.setMirrorAutofoldSw(enabled)
    }

    /**
     * Sets smart walk-away automatic central locking.
     */
    suspend fun setSmartLeavingLock(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        connection.setSmartLeavingLockSw(enabled)
    }

    /**
     * Sets HUD optical brightness level.
     */
    suspend fun setHudBrightness(brightness: Int): Boolean = withContext(Dispatchers.IO) {
        connection.setHudBright(brightness)
    }

    /**
     * Sets HUD optical height level.
     */
    suspend fun setHudHeight(height: Int): Boolean = withContext(Dispatchers.IO) {
        connection.setHudHeight(height)
    }

    /**
     * Sets HUD navigation guidance display switch.
     */
    suspend fun setHudDisplayNavigation(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        connection.setHudDisplayNavSw(enabled)
    }

    /**
     * Sets HUD incoming phone call alert switch.
     */
    suspend fun setHudDisplayPhoneCall(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        connection.setHudDisplayPhoneSw(enabled)
    }

    // ==========================================
    // 6. Intelligent EV Next-Gen Features
    // ==========================================

    /**
     * Fast-Charging Battery Thermal Preconditioning.
     * Brings battery pack to optimum fast-charging temperature prior to DC charger arrival.
     */
    suspend fun setBatteryPreconditioning(enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        _telemetry.value = _telemetry.value.copy(isBatteryPreconditioning = enabled)
        connection.setProperty(
            propId = DeepalS05Property.PROP_BATTERY_PRECONDITIONING,
            areaId = DeepalS05Property.AREA_GLOBAL,
            value = if (enabled) 1 else 2
        )
    }

    /**
     * Rain-Sensing Auto Guardian:
     * When rain is detected (PROP_RAIN_SENSOR_STATE > 1), automatically closes all windows and the sunroof shade.
     */
    suspend fun executeRainGuardian(): Boolean = withContext(Dispatchers.IO) {
        val rainState = _telemetry.value.rainSensorState
        Log.i(TAG, "Rain Guardian triggered with rainState: $rainState. Sealing windows and sunroof.")

        val windowsClosed = setWindows(2)      // Action 2 = Close all windows
        val sunshadeClosed = setSunroofShade(2) // Action 2 = 0% Close shade via wt.vehiclesetting

        windowsClosed && sunshadeClosed
    }

    /**
     * High-level Macro Scene Coordinator.
     */
    suspend fun applyScene(sceneName: String) = withContext(Dispatchers.IO) {
        Log.i(TAG, "Applying cockpit scene: $sceneName")
        when (sceneName.uppercase()) {
            "RAPID_COOL" -> {
                setClimatePower(true)
                setClimateTemperature(18.0f, DeepalS05Property.AREA_DRIVER)
                setClimateTemperature(18.0f, DeepalS05Property.AREA_PASSENGER)
                setFanSpeed(7)
                setAcEnabled(true)
                setMaxAc(true)
                setSeatVentilation(3, DeepalS05Property.AREA_DRIVER)
                setSeatVentilation(3, DeepalS05Property.AREA_PASSENGER)
                setWindows(2)
                setSunroofShade(2) // Close shade to prevent solar heating
            }
            "NAP" -> {
                setWindows(2)
                setSunroofShade(2)
                setClimatePower(true)
                setClimateTemperature(24.0f, DeepalS05Property.AREA_DRIVER)
                setFanSpeed(1)
                setSeatHeating(1, DeepalS05Property.AREA_DRIVER)
                setSeatMassage(true, mode = 1, level = 1)
                setAmbientLight(2, 25) // Amber dim
            }
            "DEFROST" -> {
                setFrontDefrost(true)
                setRearDefrost(true)
                setSteeringWheelHeat(true)
                setSeatHeating(3, DeepalS05Property.AREA_DRIVER)
            }
            "CAMP" -> {
                setClimatePower(true)
                setClimateTemperature(23.0f, DeepalS05Property.AREA_DRIVER)
                setFanSpeed(2)
                setAmbientLight(3, 50) // Forest Emerald
            }
            else -> Unit
        }
        Unit
    }
}
