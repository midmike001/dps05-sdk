package com.deepal.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepal.sdk.DeepalHudClient
import com.deepal.sdk.DeepalS05Client
import com.deepal.sdk.DeepalS05Property
import com.deepal.sdk.DeepalS05Telemetry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VehicleControlViewModel : ViewModel() {

    val client = DeepalS05Client()
    val hud: DeepalHudClient get() = client.hudClient

    val telemetry: StateFlow<DeepalS05Telemetry> = client.telemetry

    private val _isSimulated = MutableStateFlow(false)
    val isSimulated: StateFlow<Boolean> = _isSimulated.asStateFlow()

    private val _lastActionLog = MutableStateFlow("System initialized. Ready for vehicle connection.")
    val lastActionLog: StateFlow<String> = _lastActionLog.asStateFlow()

    private var simJob: Job? = null

    init {
        client.startMonitoring()
    }

    fun setSimulatedMode(enabled: Boolean) {
        _isSimulated.value = enabled
        client.isSimulatedMode = enabled

        if (enabled) {
            logAction("Simulation Mode ACTIVE: Simulating live CAN bus telemetry")
            client.updateTelemetry {
                it.copy(
                    isVirtualCarConnected = true,
                    speedKmh = 42f,
                    gear = "D",
                    batterySocPercent = 78,
                    remainingRangeKm = 345,
                    odometerKm = 12450.5f,
                    exteriorTempC = 28.5f,
                    climateTempC = 22.0f,
                    passengerTempC = 23.0f,
                    fanSpeed = 3,
                    isAcOn = true,
                    isClimatePowerOn = true
                )
            }
            startSimulationLoop()
        } else {
            logAction("Simulation Mode DISABLED: Connected to live Android Binder IPC")
            simJob?.cancel()
            simJob = null
        }
    }

    private fun startSimulationLoop() {
        simJob?.cancel()
        simJob = viewModelScope.launch {
            var speedDelta = 1f
            while (isActive && _isSimulated.value) {
                delay(300)
                client.updateTelemetry { current ->
                    var newSpeed = current.speedKmh + speedDelta
                    if (newSpeed > 75f) speedDelta = -1.5f
                    if (newSpeed < 20f) speedDelta = 1.2f
                    current.copy(speedKmh = newSpeed)
                }
            }
        }
    }

    fun logAction(msg: String) {
        _lastActionLog.value = "[${System.currentTimeMillis() % 100000}] $msg"
    }

    // --- Climate & HVAC Actuations ---
    fun toggleClimatePower() {
        val next = !telemetry.value.isClimatePowerOn
        viewModelScope.launch {
            client.setClimatePower(next)
            logAction("Climate Power set to $next")
        }
    }

    fun setDriverTemp(temp: Float) {
        viewModelScope.launch {
            client.setClimateTemperature(temp, DeepalS05Property.AREA_DRIVER)
            logAction("Driver Temp set to ${temp}°C")
        }
    }

    fun setPassengerTemp(temp: Float) {
        viewModelScope.launch {
            client.setClimateTemperature(temp, DeepalS05Property.AREA_PASSENGER)
            logAction("Passenger Temp set to ${temp}°C")
        }
    }

    fun setFanSpeed(speed: Int) {
        viewModelScope.launch {
            client.setFanSpeed(speed)
            logAction("Fan speed set to $speed")
        }
    }

    fun toggleAc() {
        val next = !telemetry.value.isAcOn
        viewModelScope.launch {
            client.setAcEnabled(next)
            logAction("A/C set to $next")
        }
    }

    fun toggleFrontDefrost() {
        val next = !telemetry.value.isFrontDefrostOn
        viewModelScope.launch {
            client.setFrontDefrost(next)
            logAction("Front Defrost set to $next")
        }
    }

    fun toggleRearDefrost() {
        val next = !telemetry.value.isRearDefrostOn
        viewModelScope.launch {
            client.setRearDefrost(next)
            logAction("Rear Defrost set to $next")
        }
    }

    fun toggleAutoClimate() {
        val next = !telemetry.value.isAutoClimateOn
        viewModelScope.launch {
            client.setAutoClimate(next)
            logAction("Auto Climate set to $next")
        }
    }

    // --- Seats Comfort ---
    fun setDriverSeatHeat(level: Int) {
        viewModelScope.launch {
            client.setSeatHeating(level, DeepalS05Property.AREA_DRIVER)
            logAction("Driver Seat Heat level $level")
        }
    }

    fun setDriverSeatVent(level: Int) {
        viewModelScope.launch {
            client.setSeatVentilation(level, DeepalS05Property.AREA_DRIVER)
            logAction("Driver Seat Vent level $level")
        }
    }

    fun setPassengerSeatHeat(level: Int) {
        viewModelScope.launch {
            client.setSeatHeating(level, DeepalS05Property.AREA_PASSENGER)
            logAction("Passenger Seat Heat level $level")
        }
    }

    fun setPassengerSeatVent(level: Int) {
        viewModelScope.launch {
            client.setSeatVentilation(level, DeepalS05Property.AREA_PASSENGER)
            logAction("Passenger Seat Vent level $level")
        }
    }

    fun toggleMassage(mode: Int = 1, level: Int = 2) {
        val next = !telemetry.value.isSeatMassageOn
        viewModelScope.launch {
            client.setSeatMassage(next, mode, level)
            logAction("Driver Massage set to $next (Mode $mode, Level $level)")
        }
    }

    fun toggleSteeringHeat() {
        val next = !telemetry.value.isSteeringWheelHeatOn
        viewModelScope.launch {
            client.setSteeringWheelHeat(next)
            logAction("Steering Wheel Heat set to $next")
        }
    }

    // --- Body & Access ---
    fun operateWindows(action: Int) {
        // 1=Open, 2=Close, 0=Stop
        viewModelScope.launch {
            client.setWindows(action)
            val actionStr = when (action) { 1 -> "OPEN"; 2 -> "CLOSE"; else -> "STOP" }
            logAction("All Windows action: $actionStr")
        }
    }

    fun operateSunroofShade(action: Int) {
        // 1=Open, 2=Close, 0=Stop
        viewModelScope.launch {
            client.setSunroofShade(action)
            val actionStr = when (action) { 1 -> "OPEN"; 2 -> "CLOSE"; else -> "STOP" }
            logAction("Sunroof Sunshade action: $actionStr")
        }
    }

    fun toggleTailgate() {
        val next = !telemetry.value.isTailgateOpen
        viewModelScope.launch {
            client.setTailgate(next)
            logAction("Tailgate set to ${if (next) "OPEN" else "CLOSED"}")
        }
    }

    fun toggleDoorLocks() {
        val next = !telemetry.value.isDoorLocked
        viewModelScope.launch {
            client.setDoorLock(next)
            logAction("Door Central Locks set to ${if (next) "LOCKED" else "UNLOCKED"}")
        }
    }

    // --- EV Battery & Charging ---
    fun toggleBatteryPreconditioning() {
        val next = !telemetry.value.isBatteryPreconditioning
        viewModelScope.launch {
            client.setBatteryPreconditioning(next)
            logAction("DC Fast-Charge Battery Preconditioning set to $next")
        }
    }

    // --- Ambient & Purifier ---
    fun setAmbientPreset(colorIdx: Int, brightness: Int = 80) {
        viewModelScope.launch {
            client.setAmbientLight(colorIdx, brightness)
            logAction("Ambient Light set to Preset $colorIdx, Brightness $brightness%")
        }
    }

    fun toggleAirPurifier() {
        val next = !telemetry.value.isAirPurifierOn
        viewModelScope.launch {
            client.setAirPurifier(next)
            logAction("PM2.5 Air Purifier set to $next")
        }
    }

    // --- Scenes & Automations ---
    fun triggerScene(sceneName: String) {
        viewModelScope.launch {
            client.applyScene(sceneName)
            logAction("Triggered Cockpit Scene: $sceneName")
        }
    }

    fun triggerRainGuardian() {
        viewModelScope.launch {
            client.executeRainGuardian()
            logAction("Rain-Sensing Auto Guardian EXECUTED (Windows & Roof Sealed)")
        }
    }

    // --- AR-HUD & InCall Navigation ---
    fun requestHudFocus(pkg: String = "com.deepal.sample") {
        viewModelScope.launch {
            val ok = hud.requestNaviFocus(pkg)
            logAction("HUD requestNaviFocus (Transact 0x3f) -> success: $ok")
        }
    }

    fun abandonHudFocus(pkg: String = "com.deepal.sample") {
        viewModelScope.launch {
            val ok = hud.abandonNaviFocus(pkg)
            logAction("HUD abandonNaviFocus (Transact 0x40) -> success: $ok")
        }
    }

    fun sendHudManeuver(iconId: Int, distMeters: Int) {
        viewModelScope.launch {
            val ok = hud.sendNavigateTurnInfo(iconId, distMeters)
            logAction("HUD Turn Info (Transact 0x18: icon=$iconId, dist=${distMeters}m) -> success: $ok")
        }
    }

    fun sendHudRoadInfo(nextRoad: String, curRoad: String) {
        viewModelScope.launch {
            val ok = hud.sendNavigateRoadInfo(nextRoad, curRoad)
            logAction("HUD Road Info (Transact 0x1a: next='$nextRoad', cur='$curRoad') -> success: $ok")
        }
    }

    fun sendHudRemainInfo(distMeters: Int, timeSec: Int) {
        viewModelScope.launch {
            val ok = hud.sendNavigateRemainInfo(distMeters, timeSec)
            logAction("HUD Remain Info (Transact 0x1b: dist=${distMeters}m, time=${timeSec}s) -> success: $ok")
        }
    }

    fun sendHudStatus(status: Int) {
        viewModelScope.launch {
            val ok = hud.sendNavigateStatus(status)
            logAction("HUD Status (Transact 0x16: status=$status) -> success: $ok")
        }
    }

    fun clearHud() {
        viewModelScope.launch {
            hud.clear()
            logAction("HUD display cleared and reset to idle")
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.stopMonitoring()
        simJob?.cancel()
    }
}
