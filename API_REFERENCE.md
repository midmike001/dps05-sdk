# `deepal-s05-sdk` API Reference Manual

Detailed API documentation for the **Changan Deepal S05 Kotlin SDK** (Platform C857 / EPA OpenOS).
Reverse engineered and verified 100% against Changan OpenOS system framework (`DEEPAL_S05_C857` and `DEEPAL_S05_CABIN_WRITES`), and low-level AIDL Binder IPC services.

---

## 1. Class: `DeepalS05Client`

The primary access point for monitoring vehicle telemetry, dispatching multi-domain cabin controls, and executing unified `WriteIntent` plans on the Deepal S05 C857.

### Constructor
```kotlin
class DeepalS05Client(
    val connection: VirtualCarConnection = VirtualCarConnection(),
    val hudClient: DeepalHudClient = DeepalHudClient(),
    val polymeric: TinnovePolymericClient = TinnovePolymericClient(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
)
```

### Properties
- `telemetry: StateFlow<DeepalS05Telemetry>`: Reactive StateFlow stream of live vehicle telemetry.
- `detectedDeviceProfile: VehicleDeviceProfile`: Automatically identified vehicle hardware profile (`deepal-s05` matched via `Build.MODEL == "C857"`).
- `activeVehicleProfile: VehicleProfile`: Full signal and write mapping model from `BuiltInProfiles.DEEPAL_S05_C857`.
- `hudClient: DeepalHudClient`: InCall AR-HUD navigation and instrument cluster manager.
- `polymeric: TinnovePolymericClient`: Changan Tinnove Polymeric Service client for `vc_alias_*` properties.
- `isSimulatedMode: Boolean`: Toggle to enable simulated telemetry for offline development and UI testing.

### Lifecycle Methods
- `startMonitoring()`: Initiates background polling of vehicle signals. High-frequency telemetry (speed, gear) polls at 250ms; slow cabin, battery, and tire pressure metrics poll at 1000ms.
- `stopMonitoring()`: Stops background polling jobs and releases coroutine resources.
- `updateTelemetry(update: (DeepalS05Telemetry) -> DeepalS05Telemetry)`: Mutates live telemetry state directly (used in simulations and testing).

### Unified Write Intent & Planning Pipeline
- `fun planWrite(intent: WriteIntent, currentValue: Number? = null): WritePlan`
  - Validates constraints, allowed choices, and park gear requirements before dispatching IPC.
  - Returns `WritePlan.Proceed(valueToWrite)`, `WritePlan.AlreadyThere(raw)`, or `WritePlan.Refused(reason)`.
- `suspend fun executeWriteIntent(intent: WriteIntent): WriteResult`
  - Plans and writes signals to the physical vehicle bus. Returns `WriteResult.Confirmed`, `WriteResult.Failed`, or `WriteResult.Refused`.

### Climate & HVAC Methods
- `suspend fun setClimatePower(enabled: Boolean): Boolean`
  - Controls main climate power (`PROP_HVAC_POWER_ON = 0x35400101`, Area 1).
- `suspend fun setClimateTemperature(tempC: Float, area: Int = AREA_DRIVER): Boolean`
  - Sets cabin setpoint (17.5°C - 32.5°C in 0.5°C increments via `PROP_HVAC_TEMP_SET = 0x35600105`).
- `suspend fun setFanSpeed(speed: Int): Boolean`
  - Sets blower speed from 1 to 8 (`PROP_HVAC_FAN_SPEED = 0x35400109`, Area 1).
- `suspend fun setWindDirection(direction: Int): Boolean`
  - Controls airflow vent distribution (`PROP_HVAC_FAN_DIRECTION = 0x35400107`, `8`=Defrost, `9`=Face, `10`=Feet, `11`=Dual Face+Feet).
- `suspend fun setAcEnabled(enabled: Boolean): Boolean`
  - Toggles A/C compressor (`PROP_HVAC_AC_ON = 0x35400102`, Area 1).
- `suspend fun setAutoClimate(enabled: Boolean): Boolean`
  - Toggles automatic climate control (`PROP_HVAC_AUTO = 0x35400104`).
- `suspend fun setRecirculation(recircOn: Boolean): Boolean`
  - Sets air recirculation mode (`PROP_HVAC_RECIRC = 0x35400108`, Vendor Tri-State: 2=Recirc, 1=Fresh).
- `suspend fun setMaxAc(enabled: Boolean): Boolean`
  - Commands maximum cooling (`PROP_HVAC_MAX_AC = 0x3540010b`).
- `suspend fun setSyncMode(enabled: Boolean): Boolean`
  - Synchronizes driver and passenger temperature zones (`PROP_HVAC_SYNC = 0x3540010d`).
- `suspend fun setFrontDefrost(enabled: Boolean): Boolean`
  - Toggles front windshield defroster (`PROP_HVAC_DEFROST_FRONT = 0x33400103`, Area 1).
- `suspend fun setRearDefrost(enabled: Boolean): Boolean`
  - Toggles rear window & mirror defrosters (`PROP_HVAC_DEFROST_REAR = 0x3540010c`, Area 1 / Area 2).

### Driving Dynamics & ADAS
- `suspend fun setDriveMode(mode: Int): Boolean`
  - Sets driving style (`PROP_DRIVE_MODE = 0x3140040d` / `0x314003f5`, `1`=COMFORT, `2`=SPORT, `3`=ECO, `4`=CUSTOM).
- `suspend fun setAutoEmergencyBraking(enabled: Boolean): Boolean`
  - Toggles Automatic Emergency Braking (`PROP_AEB_COMMAND = 0x3140040d` / `PROP_AEB_SWITCH = 0x31400244`).

### Seats Comfort & Massage
- `suspend fun setSeatHeating(level: Int, area: Int = AREA_DRIVER): Boolean`
  - Level: `0` (Off), `1` (Low), `2` (Medium), `3` (High) via `PROP_SEAT_HEATING = 0x3540010f`.
- `suspend fun setSeatVentilation(level: Int, area: Int = AREA_DRIVER): Boolean`
  - Level: `0` (Off), `1` (Low), `2` (Medium), `3` (High) via `PROP_SEAT_VENTILATION = 0x35400111`.
- `suspend fun setSeatMassage(enabled: Boolean, mode: Int = 1, level: Int = 2): Boolean`
  - Driver pneumatic massage: Toggle (`0x31400b2f`), Pattern Mode 1..8 (`PROP_SEAT_MASSAGE_MODE = 0x31400b30`), Intensity Level 1..3 (`PROP_SEAT_MASSAGE_LEVEL = 0x31400b31`).
- `suspend fun setPassengerSeatMassage(enabled: Boolean, mode: Int = 1, level: Int = 2): Boolean`
  - Passenger pneumatic massage on Area 4.
- `suspend fun setSteeringWheelHeat(enabled: Boolean): Boolean`
  - Controls steering wheel heating (`PROP_STEERING_WHEEL_HEAT = 0x314003eb`).

### Windows, Sunroof, Tailgate & Central Locks
- `suspend fun setWindows(action: Int): Boolean`
  - Controls all 4 windows (`1`=Open 100%, `2`=Close 0%, `3`=Vent 15%, `0`=Stop) via `0x33400300` & `0x33400301`.
- `suspend fun setWindowPosition(percent: Int, area: Int): Boolean`
  - Sets exact window position percentage (0..100%).
- `suspend fun setWindow(area: Int, action: Int): Boolean`
  - Controls an individual window area.
- `suspend fun setSunroofShade(actionOrPercent: Int): Boolean`
  - Controls electric sunshade blind (`1`=Open 100%, `2`=Close 0%, `0`=Stop 50%, or direct percent) via `wt.vehiclesetting` Transact `0x40` or `0x31400303`.
- `suspend fun setSunshadePercent(percent: Int): Boolean`
  - Sets sunshade position directly as 0..100%.
- `suspend fun setSunroof(posOrPercent: Int): Boolean`
  - Sets sunroof glass position (0..100%) via `wt.vehiclesetting` Transact `0x3b`.
- `suspend fun setSunroofTilt(tilt: Boolean): Boolean`
  - Toggles sunroof vent tilt via `wt.vehiclesetting` Transact `0x3c`.
- `suspend fun setTailgate(open: Boolean): Boolean`
  - Actuates power liftgate (`PROP_TAILGATE_CONTROL = 0x31400313`: 2=Open, 1=Close).
- `suspend fun setDoorLock(locked: Boolean): Boolean`
  - Locks/unlocks central door locks (`PROP_DOOR_LOCK = 0x314003eb`: 2=Locked, 1=Unlocked).
- `suspend fun setDoorHandleExpanded(expanded: Boolean): Boolean`
  - Commands flush electric door handles (`0x314003ec`: 1=Expanded, 2=Retracted).
- `suspend fun setMirrorFold(folded: Boolean): Boolean`
  - Commands power side mirrors (`0x314003ed`: 1=Folded, 2=Unfolded).
- `suspend fun openFuelCap(): Boolean`
  - Triggers charging port / fuel cap release solenoid (`0x314003ee`).
- `suspend fun openGloveBox(): Boolean`
  - Triggers electronic glove box release (`0x314003ef`).

### Lighting & Air Quality
- `suspend fun setAmbientLight(colorIndex: Int, brightness: Int = 60): Boolean`
  - Sets ambient lighting color preset and brightness (0..100%).
- `suspend fun setAmbientLightPattern(pattern: Int): Boolean`
  - Sets dynamic light pattern mode 1..3 (`PROP_AMBIENT_LIGHT_PATTERN = 0x31400677`).
- `suspend fun setAmbientColorChoice(colorChoice: Int): Boolean`
  - Sets exact ambient color code (`54`, `42`, `33`, `12`, `6`, `1` via `0x3140039b`).
- `suspend fun setAirPurifier(enabled: Boolean): Boolean`
  - Toggles cabin PM2.5 air purifier (`PROP_AIR_PURIFIER = 0x35400122`).

### Next-Gen EV & Automation
- `suspend fun setBatteryPreconditioning(enabled: Boolean): Boolean`
  - Fast-charging battery thermal preconditioning (`PROP_BATTERY_PRECONDITIONING = 0x314006c6`).
- `suspend fun executeRainGuardian(): Boolean`
  - Automatically seals all 4 windows and roof sunshade when rain is detected (`PROP_RAIN_SENSOR_STATE > 1`).
- `suspend fun applyScene(sceneName: String)`
  - Macro scenes: `"RAPID_COOL"`, `"NAP"`, `"DEFROST"`, `"CAMP"`.

---

## 2. Class: `DeepalHudClient`

Controls the AR-HUD display, navigation banner, turn icons, and digital cluster via `com.incall.SVR_MNG_SERVICE` and `com.incall.double.INTERACTIVE_SERVICE`.

### Core Methods
- `suspend fun sendNavigateStatus(status: Int): Boolean`: 1=Active guidance, 2=Arrived, 0=Idle (Transact `0x16`).
- `suspend fun sendNavigateTurnInfo(turnIcon: Int, turnDistMeters: Int): Boolean`: Turn maneuver icon ID & countdown distance (Transact `0x18`).
- `suspend fun sendNavigateRoadInfo(nextRoad: String, curRoad: String): Boolean`: Road name display (Transact `0x1a`).
- `suspend fun sendNavigateRemainInfo(remainDistMeters: Int, remainTimeSec: Int): Boolean`: Distance & ETA (Transact `0x1b`).
- `suspend fun sendNavigateCameraInfo(cameraInfo: String): Boolean`: Speed limit camera alert string (Transact `0x1c`).
- `suspend fun sendNavigateLaneInfo(laneInfo: String): Boolean`: Lane guidance diagram (Transact `0x19`).
- `suspend fun sendNavigateCrossRoad(crossRoadState: Int): Boolean`: Junction view state (Transact `0x17`).
- `suspend fun sendNavigatePercent(percent: Int): Boolean`: Trip progress percentage (Transact `0x32`).
- `suspend fun requestNaviFocus(packageName: String): Boolean`: Requests HUD navigation focus (Transact `0x3f`).
- `suspend fun abandonNaviFocus(packageName: String): Boolean`: Releases HUD focus (Transact `0x40`).
- `suspend fun clear(): Boolean`: Resets HUD to idle guidance state.

---

## 3. Package: `com.deepal.sdk.device`

Auto-detects vehicle hardware platform using device fingerprints extracted from `android.os.Build`.

### Classes
- `VehicleProfiles.detectCurrent(deviceInfo: DeviceInfo = DeviceInfo()): VehicleDeviceProfile`
  - Detects Deepal S05 (`Build.MODEL == "C857"`) or generic fallback.
- `VehicleProfiles.resolveVehicleConfig(platformId: String): VehicleProfileConfig`
  - Returns `VehicleConfigurations.DEEPAL_S05_C857`.
- `VehicleProfiles.resolveVehicleProfile(platformId: String): VehicleProfile`
  - Returns `BuiltInProfiles.DEEPAL_S05_C857`.

---

## 4. Package: `com.deepal.sdk.vehicle`

Core signal definition and cabin execution models 

### Classes
- `BuiltInProfiles.DEEPAL_S05_C857`: Deepal S05 signal and cabin writes definition.
- `BuiltInProfiles.DEEPAL_S05_CABIN_WRITES`: Full cabin actuation specification for Deepal S05.
- `VehicleConfigurations.DEEPAL_S05_C857`: Low-level hardware CAN signal mapping for Deepal S05 C857.
- `WriteIntent`: Sealed hierarchy for `TempSet`, `TempStep`, `LevelStep`, `Position`, `Command`, `Choice`.
- `WritePlan`: `Proceed`, `AlreadyThere`, `Refused`.
- `WriteResult`: `Confirmed`, `Failed`, `Refused`.
