# Deepal S05 Vehicle Management SDK (`deepal-s05-sdk`)
### Native Kotlin Automotive Library for Changan Deepal S05 (Model C857 / EPA OpenOS Platform)

`deepal-s05-sdk` is an automotive-grade, standalone Kotlin library designed to interface directly with the **Changan Deepal S05** head unit, cockpit domain controller, and vehicle body controllers. 

Reverse engineered 100% from the Changan OpenOS system framework and verified against ground truth bytecode.

---

## 1. Features

- **Direct OpenOS AIDL IPC**:
  - `com.openos.virtualcar.IVirtualCar` (`virtualcar_service`, Transact 2 for property service resolution)
  - `com.openos.virtualcar.IVirturalCarProperty` (OEM spelling with 'r', Transact 2 `setProperty`, Transact 3 `getProperty`)
- **Reactive Vehicle Telemetry (`StateFlow<DeepalS05Telemetry>`)**:
  - High-frequency live speed (`0x11600207`, km/h) & current gear (`0x31400231`: `P`, `R`, `N`, `D`).
  - Battery SoC % (`0x3140028c`, Area `0x1b`), remaining range (`0x314006c4`, km), odometer (`0x31600204`, raw meters / `1000f` = km), tyre pressure (`0x31410605`), and exterior temperature (`0x35600403`).
  - Power window states, door locks (`0x314003eb`), tailgate (`0x3140040d`), and sunroof shade position (`0x31400313`).
- **Complete Vehicle Actuation**:
  - **Dual-Zone Climate Control**: 17.5°C to 32.5°C (`0x35600105`) in 0.5°C steps, 8 fan speeds (`0x35400109`), AC toggle (`0x35400102`), front defrost (`0x33400103`), rear defrost (`0x3540010c`), auto mode (`0x35400104`), max AC (`0x3540010b`), recirculation (`0x35400108`: 2=Recirc, 1=Fresh), sync mode (`0x3540010d`).
  - **Seat Comfort**: 3-level seat ventilation (`0x35400111`) & heating (`0x3540010f`) for driver (Area 1) and passenger (Area 4), plus pneumatic seat massage toggle (`0x31400b2f`), modes 1-3 (`0x31400b31`), and intensity levels 1-8 (`0x31400b30`).
  - **Body & Access**: Power window control (FL `0x10`, FR `0x40`, RL `0x100`, RR `0x400` via `0x33400301`), window lock (`0x31400303`), electric sunroof shade (`0x31400313`), tailgate open/close (`0x3140040d`), central locks (`0x314003eb`).
  - **Cabin Environment**: Ambient lighting colors 1-6 (`0x3140039a`) & brightness 0-100% (`0x3140039b`), PM2.5 air purifier (`0x35400122`).
  - **Smart Scenes**: Rapid Cool, Nap/Rest, Quick Defrost, Camp Mode.
- **Next-Gen EV Features**:
  - **Battery Fast-Charging Thermal Preconditioning** (`PROP_BATTERY_PRECONDITIONING = 0x314006c6`).
  - **Rain-Sensing Auto Guardian** (`PROP_RAIN_SENSOR_STATE = 0x31400277`).
- **OEM InCall AR-HUD & Windshield Presentation**:
  - Full hardware AR-HUD secondary display support (800x480 resolution, optical window crop `x=573, y=167, w=227, h=188`).
  - Transmits turn maneuver icons (`0x18`), countdown distances (`0x18`), road names (`0x1a`), ETA/remain distance (`0x1b`), and navigation status (`0x16`) via `com.incall.double.INTERACTIVE_SERVICE`.
  - Navigation focus management (`0x3f` request focus, `0x40` abandon focus) with `INaviFocusCallback` token attachment.

---

## 2. In-Depth Developer Documentation

The SDK includes dedicated, in-depth architectural and development manuals:

| Guide Document | Focus Area | Contents |
|:---|:---|:---|
| 🖥️ **[HUD Development Guide](HUD_DEVELOPMENT_GUIDE.md)** | Windshield Presentation & AR-HUD | 800x480 secondary display, optical window crop (`573, 167`), centered turn arrows, InCall IPC codes (`0x16`, `0x18`, `0x1a`, `0x1b`, `0x3f`, `0x40`). |
| ❄️ **[Climate & Seats Guide](CLIMATE_AND_SEATS_GUIDE.md)** | HVAC & Comfort | Dual-zone climate, 17.5°C-32.5°C in 0.5°C steps, 8 fan speeds, 3-level seat ventilation & heating, pneumatic massage modes (1-3, levels 1-8), memory presets. |
| 🚪 **[Body & Access Control Guide](BODY_CONTROL_GUIDE.md)** | Body Domains & Windows | Power windows (FL, FR, RL, RR), window lock, electric sunroof roller blind, power tailgate, door locks, Rain-Sensing Auto Guardian. |
| ⚡ **[EV Battery & Charging Guide](EV_BATTERY_CHARGING_GUIDE.md)** | BMS & Power Dynamics | Battery SoC % (Area `0x1b`), remaining range estimation, DC fast-charging thermal preconditioning (`0x314006c6`), charging station waypoints. |
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

---

## 5. Quick Start Guide

### Initializing the Client
```kotlin
import com.deepal.sdk.DeepalS05Client
import com.deepal.sdk.DeepalS05Property
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

val client = DeepalS05Client()

// Start real-time signal monitoring
client.startMonitoring()

// Collect reactive telemetry
lifecycleScope.launch {
    client.telemetry.collect { telemetry ->
        println("Speed: ${telemetry.speedKmh} km/h | Gear: ${telemetry.gear}")
        println("Battery: ${telemetry.batterySocPercent}% | Range: ${telemetry.remainingRangeKm} km | Odometer: ${telemetry.odometerKm} km")
        println("Cabin Temp: ${telemetry.climateTempC}°C | Fan: ${telemetry.fanSpeed}")
    }
}

// Actuating Hardware Controls
lifecycleScope.launch {
    // 1. Dual-Zone Climate
    client.setClimatePower(true)
    client.setClimateTemperature(22.5f, area = DeepalS05Property.AREA_DRIVER)
    client.setFanSpeed(4)
    client.setAcEnabled(true)
    client.setAutoClimate(true)

    // 2. Seat Comfort
    client.setSeatVentilation(level = 2, area = DeepalS05Property.AREA_DRIVER)
    client.setSeatHeating(level = 1, area = DeepalS05Property.AREA_PASSENGER)
    client.setSeatMassage(enabled = true, mode = 1, level = 3)

    // 3. Body & Access
    client.setSunroofShade(action = 1) // 1=Open, 2=Close
    client.setDoorLock(locked = true)

    // 4. EV Fast-Charging Battery Preconditioning
    client.setBatteryPreconditioning(enabled = true)
}
```
