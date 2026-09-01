# Deepal S05 Body, Doors & Access Control Development Guide
### 4-Door Sensing, Tailgate, Windows, Sunroof Shade, Central Locks & Rain Guardian

---

## 1. System Architecture

The Body and Access domain in the **Changan Deepal S05** (Platform Model C857) interfaces with:
1. **OpenOS VirtualCar Property Bus (`com.openos.virtualcar`)**: Doors open status, windows actuation, tailgate actuation/status, and central locks.
2. **Vehicle Settings Service (`wt.vehiclesetting`)**: Sunroof sunshade roller blind, sunroof glass positioning, and tilt status.

### Key Hardware Constants

| Property Constant | Hex ID / Transact | System Bus / Service | Action / State Values |
|:---|:---|:---|:---|
| `PROP_DOORS` | `0x36400311` | VirtualCar Property Bus | Area Bitmasks: FL (`0x01`), FR (`0x04`), RL (`0x10`), RR (`0x40`). `1` = Open, `0` / `2` = Closed (`vc_alias_door_pos_*`) |
| `PROP_TAILGATE_CONTROL` | `0x31400313` | VirtualCar Property Bus | Actuation Command: `1` = Open Tailgate, `2` = Close Tailgate |
| `PROP_TAILGATE_STATUS` | `0x31400314` | VirtualCar Property Bus | Position Status: `1` = Open, `0` / `2` = Closed (`vc_alias_door_trunk_pos`) |
| `PROP_WINDOW_MOVE` | `0x33400301` | VirtualCar Property Bus | FL (`0x10`), FR (`0x40`), RL (`0x100`), RR (`0x400`): `1` = Open, `2` = Close, `0` = Stop |
| `PROP_WINDOW_LOCK` | `0x31400303` | VirtualCar Property Bus | `1` = Locked, `0` = Unlocked |
| `TRANSACT_SET_SUNSHADE_POS` | `0x40` (64) | `wt.vehiclesetting` | `setSunshadePos(int pos)`: `0` (Closed) to `100` (Fully Open) |
| `TRANSACT_GET_SUNSHADE_POS` | `0x3f` (63) | `wt.vehiclesetting` | `getSunshadePos() -> int` (0..100 percent) |
| `TRANSACT_SET_SUNROOF_POS` | `0x3b` (59) | `wt.vehiclesetting` | `setSunroofPos(int pos)`: `0` (Closed) to `100` (Fully Open) |
| `TRANSACT_SET_SUNROOF_TILT` | `0x3c` (60) | `wt.vehiclesetting` | `setSunroofTiltStatus(int tilt)`: `1` = Tilt / Vent, `0` = Closed |
| `PROP_DOOR_LOCK` | `0x314003eb` | VirtualCar Property Bus | `1` = Lock, `2` = Unlock |
| `PROP_RAIN_SENSOR_STATE`| `0x31400277` | VirtualCar Property Bus | `1` = Dry / No Rain, `2` = Light Rain, `3` = Heavy Rain |

> [!IMPORTANT]
> **Why `0x31400313` opens the trunk:**
> On the Changan Deepal S05 CAN bus, `0x31400313` is the hardware actuation command for the **Power Tailgate (Liftgate)** (`1` = Open, `2` = Close). 
> The Sunroof Sunshade roller blind is commanded through the `wt.vehiclesetting` system service via `setSunshadePos(int percent)` (Transact `0x40`).

---

## 2. Individual Door Open/Closed Sensing

The Deepal S05 transmits door open states on property `0x36400311` using dedicated area IDs:

```kotlin
// Area Constants
const val AREA_DOOR_FL = 0x01 // Front-Left (Driver) Door
const val AREA_DOOR_FR = 0x04 // Front-Right (Passenger) Door
const val AREA_DOOR_RL = 0x10 // Rear-Left Passenger Door
const val AREA_DOOR_RR = 0x40 // Rear-Right Passenger Door
```

### Reading Door States
```kotlin
val client = DeepalS05Client()

// Monitored automatically in client.telemetry
lifecycleScope.launch {
    client.telemetry.collect { telemetry ->
        println("Driver Door Open: ${telemetry.doorFlOpen}")
        println("Passenger Door Open: ${telemetry.doorFrOpen}")
        println("Rear Left Door Open: ${telemetry.doorRlOpen}")
        println("Rear Right Door Open: ${telemetry.doorRrOpen}")
        println("Trunk / Tailgate Open: ${telemetry.isTailgateOpen}")
    }
}
```

---

## 3. Power Windows Control

Windows are actuated by dispatching to both `PROP_WINDOW_POS` (`0x33400300`: `0..100%`) and `PROP_WINDOW_MOVE` (`0x33400301`: `-100..100` rate) across the four window area masks:

```kotlin
suspend fun controlWindows() {
    // 1. Full Express Open All 4 Windows (100% Travel)
    client.setWindows(action = 1)

    // 2. Full Express Close All 4 Windows (0% Travel)
    client.setWindows(action = 2)

    // 3. Crack Open for Ventilation (15% Vent Gap)
    client.setWindows(action = 3)

    // 4. Halt Window Movement Mid-Travel
    client.setWindows(action = 0)

    // 5. Set Specific Percentage on Driver Window (e.g. 50% Half-Open)
    client.setWindowPosition(percent = 50, area = DeepalS05Property.AREA_WINDOW_FL)

    // 6. Operate Individual Window (1=Open, 2=Close, 3=Vent, 0=Stop)
    client.setWindow(area = DeepalS05Property.AREA_WINDOW_FR, action = 1)
}
```

> [!NOTE]
> **Why earlier versions only inched slightly:**
> Sending a small scalar integer (like `1` or `2`) to `PROP_WINDOW_MOVE` commands low-speed jog / micro-stepping mode (5–10mm per call). The Deepal S05 SDK dispatches full express travel rates (`100` / `-100`) and target positions (`PROP_WINDOW_POS` = `100%` / `0%`) to execute continuous auto up/down travel.

---

## 4. Electric Sunroof Sunshade & Glass Control

Sunroof Sunshade and Glass movements are managed via `wt.vehiclesetting`:

```kotlin
suspend fun controlSunroofAndShade() {
    // 1. Fully Open Sunshade (100%)
    client.setSunroofShade(actionOrPercent = 1) // Or client.setSunshadePercent(100)

    // 2. Fully Close Sunshade (0%)
    client.setSunroofShade(actionOrPercent = 2) // Or client.setSunshadePercent(0)

    // 3. Open Sunroof Glass to 80%
    client.setSunroof(posOrPercent = 80)

    // 4. Tilt / Vent Sunroof
    client.setSunroofTilt(tilt = true)
}
```

---

## 5. Power Tailgate (`PROP_TAILGATE_CONTROL = 0x31400313`)

```kotlin
suspend fun controlTailgate() {
    // Open Power Tailgate Lift
    client.setTailgate(open = true)

    // Close Power Tailgate Lift
    client.setTailgate(open = false)
}
```

---

## 6. Central Door Locks (`PROP_DOOR_LOCK = 0x314003eb`)

```kotlin
suspend fun controlCentralLocks() {
    // Lock all 4 doors
    client.setDoorLock(locked = true)

    // Unlock all 4 doors
    client.setDoorLock(locked = false)
}
```

---

## 7. Motorized Flush Handles & Power Side Mirrors

The Deepal S05 features motorized pop-out flush door handles and power folding side mirrors:

```kotlin
suspend fun controlBodyActuators() {
    // Pop out electric door handles for passenger ingress
    client.setDoorHandleExpanded(expanded = true)

    // Retract flush door handles for aerodynamics
    client.setDoorHandleExpanded(expanded = false)

    // Fold side rearview mirrors (e.g. narrow parking)
    client.setMirrorFold(folded = true)

    // Unfold side rearview mirrors
    client.setMirrorFold(folded = false)

    // Release fuel port / charging port solenoid
    client.openFuelCap()

    // Release electronic glove box solenoid
    client.openGloveBox()
}
```

---

## 8. Rain-Sensing Auto Guardian

The Auto Guardian monitors `PROP_RAIN_SENSOR_STATE` (`0x31400277`). When rain intensity reaches `2` (Light Rain) or `3` (Heavy Rain), it seals the vehicle:

```kotlin
suspend fun onRainDetected() {
    client.executeRainGuardian()
    // 1. Sends Action 2 (Close) to all 4 window motors
    // 2. Sends 0% (Close) to the electric sunroof sunshade via wt.vehiclesetting
}
```
