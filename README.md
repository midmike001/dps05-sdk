# Deepal S05 Vehicle Management SDK (`deepal-s05-sdk`)
### Native Kotlin Automotive Library for Changan Deepal S05 (Model C857 / EPA OpenOS Platform)

`deepal-s05-sdk` is an automotive-grade, standalone Kotlin library designed to interface directly with the **Changan Deepal S05** head unit and vehicle body domain controllers. 

---

## 1. Features

- **Direct OpenOS AIDL IPC**:
  - `com.openos.virtualcar.IVirtualCar` (Transact 2 resolution)
  - `com.openos.virtualcar.IVirturalCarProperty` (OEM spelling with 'r')
- **Reactive Vehicle Telemetry (`StateFlow<DeepalS05Telemetry>`)**:
  - High-frequency live speed (km/h) & current gear (`P`, `R`, `N`, `D`).
  - Battery SoC %, remaining range (km), odometer, and exterior temperature.
  - Power window states, door locks, tailgate, and sunroof shade position.
- **Complete Vehicle Actuation**:
  - **Dual-Zone Climate Control**: 17.5°C to 32.5°C in 0.5°C steps, 8 fan speeds, AC toggle, front & rear defrost, auto mode, sync mode.
  - **Seat Comfort**: 3-level seat ventilation & heating for driver (Area 1) and passenger (Area 4), plus seat massage modes.
  - **Body & Access**: Power window control (all or individual FL, FR, RL, RR), electric sunroof shade, tailgate open/close, central locks.
  - **Cabin Environment**: 64-color ambient light presets & brightness, PM2.5 air purifier.
  - **Smart Scenes**: Rapid Cool, Nap/Rest, Quick Defrost, Camp Mode.
- **Next-Gen EV Features**:
  - **Battery Fast-Charging Thermal Preconditioning** (`PROP_BATTERY_PRECONDITIONING = 0x314006c6`).
  - **Rain-Sensing Auto Guardian** (`PROP_RAIN_SENSOR_STATE = 0x31400277`).
- **OEM InCall AR-HUD & Windshield Presentation**:
  - Full hardware AR-HUD secondary display support (800x480 resolution, optical window crop `x=573, y=167, w=227, h=188`).
  - Transmits turn maneuver icons, countdown distances, road names, and ETA via `com.incall.double.INTERACTIVE_SERVICE`.
---

## 2. In-Depth Developer Documentation

The SDK includes dedicated, in-depth architectural and development manuals:

| Guide Document | Focus Area | Contents |
|:---|:---|:---|
| 🖥️ **[HUD Development Guide](HUD_DEVELOPMENT_GUIDE.md)** | Windshield Presentation & AR-HUD | 800x480 secondary display, optical window crop (`573, 167`), centered turn arrows, InCall IPC codes (`0x16`, `0x18`, `0x1a`, `0x1b`, `0x3f`, `0x40`). |
| ❄️ **[Climate & Seats Guide](CLIMATE_AND_SEATS_GUIDE.md)** | HVAC & Comfort | Dual-zone climate, 17.5°C-32.5°C in 0.5°C steps, 8 fan speeds, 3-level seat ventilation & heating, pneumatic massage modes, memory presets. |
| 🚪 **[Body & Access Control Guide](BODY_CONTROL_GUIDE.md)** | Body Domains & Windows | Power windows (FL, FR, RL, RR & all), electric sunroof roller blind, power tailgate, door locks, Rain-Sensing Auto Guardian. |
| ⚡ **[EV Battery & Charging Guide](EV_BATTERY_CHARGING_GUIDE.md)** | BMS & Power Dynamics | Battery SoC %, remaining range estimation, DC fast-charging thermal preconditioning (`PROP_BATTERY_PRECONDITIONING`), charging station waypoints. |
| 🎙️ **[Voice & Cockpit Scenes Guide](VOICE_AND_SCENES_GUIDE.md)** | Voice AI & Scenes | "Hello Deepal" wake word, spoken phrase dispatcher, Rapid Cool, Nap, Defrost, and Camp mode automations. |
| 🔌 **[AIDL Property Bus Guide](AIDL_PROPERTY_BUS_GUIDE.md)** | Low-Level Hardware Interface | Direct Binder IPC, `IVirtualCar`, `IVirturalCarProperty`, area masks, reflection, and reverse engineering mappings. |

---

## 3. Installation & Gradle Setup

Add the library module to your Android Studio project:

### `settings.gradle.kts`:
```kotlin
include(":deepal-s05-sdk")
```

### `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(project(":deepal-s05-sdk"))
}
```

Or consume the prebuilt `.aar` binary directly:
```kotlin
dependencies {
    implementation(files("libs/deepal-s05-sdk-release.aar"))
}
```

---

## 4. Example Showcase Application (`:sample`)

This repository includes a native Android showcase application (`:sample`) exercising every feature in the SDK:

```bash
# Build both the library and sample APK
./gradlew assembleDebug

# Run test suite
./gradlew test
```

Generated APK location: `sample/build/outputs/apk/debug/sample-debug.apk`

The sample application provides:
- **Telemetry Dashboard**: Speedometer, gear indicator (`P/R/N/D`), battery SoC %, range, and connection status.
- **Dual-Zone Climate & Seats**: 17.5°C–32.5°C temp steppers, 8 fan speeds, AC, front/rear defrost, 3-level seat ventilation & heating, and pneumatic massage.
- **Body & Access Control**: 4 power windows, sunroof roller blind, power tailgate, and central door locks.
- **EV Fast-Charging & BMS**: Battery thermal preconditioning toggle and range feasibility evaluation.
- **InCall AR-HUD Bridge**: Transacts `0x16`, `0x18`, `0x1a`, `0x1b`, `0x3f`, `0x40`, and physical 800x480 windshield `android.app.Presentation`.
- **Scenes & Rain Guardian**: Rapid Cool, Nap, Defrost, Camp modes, voice command simulator, and rain guardian macro.
- **Vehicle Simulation Bus Toggle**: Easily test and demo all features off-vehicle without physical car hardware.

---

## 5. Quick Start Guide

### Initializing the Client
```kotlin
import com.deepal.sdk.DeepalS05Client
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

val deepalClient = DeepalS05Client()

// Start monitoring real-time vehicle signals (speed, gear, battery SoC)
deepalClient.startMonitoring()

// Observe reactive telemetry in CoroutineScope
lifecycleScope.launch {
    deepalClient.telemetry.collect { telemetry ->
        println("Speed: ${telemetry.speedKmh} km/h, Gear: ${telemetry.gear}")
        println("Battery: ${telemetry.batterySocPercent}%, Range: ${telemetry.remainingRangeKm} km")
        println("Cabin Temp: ${telemetry.climateTempC}°C")
    }
}
```

---

### Controlling Vehicle Climate & Seats
```kotlin
// Set driver climate to 22.0°C and fan speed 3
lifecycleScope.launch {
    deepalClient.setClimateTemperature(22.0f)
    deepalClient.setFanSpeed(3)
    deepalClient.setAcEnabled(true)

    // Turn on driver seat ventilation to level 2 (out of 3)
    deepalClient.setSeatVentilation(level = 2)

    // Activate driver seat massage
    deepalClient.setSeatMassage(enabled = true, mode = 1, level = 2)
}
```

---

### Operating Windows, Sunroof & Locks
```kotlin
lifecycleScope.launch {
    // Open/Close electric sunroof shade (1 = Open, 2 = Close, 0 = Stop)
    deepalClient.setSunroofShade(2)

    // Close all 4 power windows
    deepalClient.setWindows(2)

    // Lock all doors
    deepalClient.setDoorLock(true)
}
```

---

### Using Next-Gen EV Fast-Charging & Rain Guardian
```kotlin
lifecycleScope.launch {
    // 1. Fast-Charging Battery Preconditioning
    // Warms the battery pack to optimum temperature before reaching DC fast-chargers
    deepalClient.setBatteryPreconditioning(true)

    // 2. Rain-Sensing Guardian
    // Closes all 4 windows and sunroof shade automatically
    deepalClient.executeRainGuardian()
}
```

---

### Feeding Maneuvers to AR-HUD & Cluster (`DeepalHudClient`)
```kotlin
val hud = deepalClient.hudClient

lifecycleScope.launch {
    // 1. Request navigation HUD and audio focus (Transact 0x3f)
    hud.requestNaviFocus("com.deepalnav")

    // 2. Set active guidance status (1 = Active, 2 = Arrived, 0 = Inactive)
    hud.sendNavigateStatus(1)

    // 3. Send turn maneuver icon (Center Arrow) and countdown distance in meters (Transact 0x18)
    // Turn icons: 1: Straight, 2: Turn Right, 3: Turn Left, 4: Slight Right, 5: Slight Left, 6: U-Turn, 7: Roundabout
    hud.sendNavigateTurnInfo(turnIcon = 2, turnDistMeters = 200)

    // 4. Update road names (Transact 0x1a)
    hud.sendNavigateRoadInfo(nextRoad = "Russian Federation Blvd", curRoad = "Preah Monivong Blvd")

    // 5. Send remaining route distance and ETA in seconds (Transact 0x1b)
    hud.sendNavigateRemainInfo(remainDistMeters = 4800, remainTimeSec = 720)

    // 6. Release focus and clear HUD on arrival
    hud.abandonNaviFocus("com.deepalnav")
    hud.clear()
}
```

---

## 4. Hardware AR-HUD Presentation & Center Arrow Layout

The physical HUD secondary display is driven at **800 x 480** resolution against a pure black background:

| Layout Zone | Coordinates / Alignment | Display Content |
|:---|:---|:---|
| **Center Navigation Axis** | `Gravity.CENTER` | Dynamic AR Chevrons (`‹ ‹ ‹ ‹` / `▲ ▲ ▲ ▲` / `› › › ›`), Prominent Maneuver Turn Arrow (46sp bold), Countdown Distance (34sp bold), Vehicle Gear `[ P / D / R / N ]` |
| **Left Status Block** | `Gravity.START`, `x = 40` | Battery EV SoC & Remaining Range (`[🔋] 209km`), Speed Limit Warnings |
| **Right Status Block** | `Gravity.END`, `x = 50` | Assisted Driving Pilot `A` (`#4DD0E1`) |
| **Optical Map Window** | `x = 573, y = 167, w = 227, h = 188` | Hardware Texture-Mode MapLibre vector map viewport |

---

## 5. Architecture & Source Mapping

| SDK Component | Description |
|:---|:---|:---|
| `VirtualCarConnection` | Implements `ServiceManager.checkService` reflection and Binder transact code 2 resolution. |
| `DeepalS05Property` | Contains all hardware property IDs, area masks, and data types for platform `C857`. |
| `DeepalS05Telemetry` | Immutable data model holding vehicle telemetry state. |
| `DeepalS05Client` | High-level vehicle manager and actuator. |
| `DeepalHudClient` | Changan InCall double-interactive service IPC manager. |

