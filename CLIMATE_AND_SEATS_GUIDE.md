# Deepal S05 Climate & Seat Comfort Development Guide
### Dual-Zone HVAC, 8-Speed Blower, Multi-Level Seat Heat/Vent, and Pneumatic Massage

---

## 1. System Architecture

The Climate and Comfort domain in the **Changan Deepal S05** (C857 platform) controls dual-zone cabin temperature, airflow distribution, multi-stage seat heating/ventilation, and pneumatic massage programs through the OpenOS VirtualCar property bus.

### Key Property IDs & Area Masks

| Property Constant | Hex ID | Area Mask | Data Type | Description |
|:---|:---|:---|:---|:---|
| `PROP_HVAC_TEMP_SET` | `0x35600105` | Area 1 (Driver), Area 4 (Passenger) | `Float` | Target cabin temperature (°C) |
| `PROP_HVAC_FAN_SPEED` | `0x35400107` | Area 1 (Driver) | `Int` | Blower fan speed (0 to 8) |
| `PROP_HVAC_AC_ON` | `0x35200106` | Area 1 (Driver) | `Int` | AC compressor state (1=On, 0=Off) |
| `PROP_HVAC_AUTO_ON` | `0x3520010a` | Area 1 (Driver) | `Int` | Full automatic climate control |
| `PROP_HVAC_DUAL_SYNC` | `0x35200130` | Area 1 (Driver) | `Int` | Dual-zone sync mode |
| `PROP_DEFROST_FRONT` | `0x3520010c` | Area 0 (Global) | `Int` | Front windshield max defroster |
| `PROP_DEFROST_REAR` | `0x3520010d` | Area 0 (Global) | `Int` | Rear glass & heated side mirrors |
| `PROP_SEAT_HEATING` | `0x3540010f` | Area 1 (Driver), Area 4 (Passenger) | `Int` | Seat heating level (0 to 3) |
| `PROP_SEAT_VENTILATION`| `0x35400111` | Area 1 (Driver), Area 4 (Passenger) | `Int` | Seat ventilation level (0 to 3) |
| `PROP_SEAT_MASSAGE` | `0x35400125` | Area 1 (Driver), Area 4 (Passenger) | `Int` | Pneumatic massage mode & level |

---

## 2. Temperature Conversion & Control

The Deepal S05 HVAC system accepts temperature values between **17.5°C and 32.5°C** in **0.5°C increments**:

### Setting Cabin Temperature
```kotlin
val client = DeepalS05Client()

suspend fun setDriverTemperature(targetTempC: Float) {
    val clamped = targetTempC.coerceIn(17.5f, 32.5f)
    // Send to Driver Zone (Area 1)
    client.setClimateTemperature(clamped, areaId = DeepalS05Property.AREA_DRIVER)
}

suspend fun setPassengerTemperature(targetTempC: Float) {
    val clamped = targetTempC.coerceIn(17.5f, 32.5f)
    // Send to Passenger Zone (Area 4)
    client.setClimateTemperature(clamped, areaId = DeepalS05Property.AREA_PASSENGER)
}
```

---

## 3. Fan Speed & Defrost Programs

The HVAC blower supports **8 discrete speed levels** (Level 1 to Level 8):

```kotlin
suspend fun configureBlowerAndDefrost() {
    // Set fan speed to level 4
    client.setFanSpeed(4)

    // Activate Front Max Windshield Defrost
    client.setFrontDefrost(true)

    // Activate Rear Window & Mirror Heating
    client.setRearDefrost(true)
}
```

---

## 4. Seat Heating, Ventilation & Pneumatic Massage

### Seat Levels & Area Identifiers
- **Driver Seat**: `DeepalS05Property.AREA_DRIVER = 1`
- **Front Passenger Seat**: `DeepalS05Property.AREA_PASSENGER = 4`
- **Levels**: `0` = Off, `1` = Low, `2` = Medium, `3` = High

### Setting Seat Comfort
```kotlin
suspend fun configureSeats() {
    // Driver Seat: Heating Level 2, Ventilation Off
    client.setSeatHeating(level = 2, areaId = DeepalS05Property.AREA_DRIVER)
    client.setSeatVentilation(level = 0, areaId = DeepalS05Property.AREA_DRIVER)

    // Passenger Seat: Ventilation Level 3
    client.setSeatVentilation(level = 3, areaId = DeepalS05Property.AREA_PASSENGER)
}
```

### Pneumatic Seat Massage
The Deepal S05 driver and passenger seats contain multi-chamber pneumatic air bladders supporting 3 massage modes and 3 intensity levels:
- **Mode 1**: Pulse Massage
- **Mode 2**: Wave Massage
- **Mode 3**: Lumbar Focused Massage

```kotlin
suspend fun activateDriverMassage() {
    // Enable Mode 2 (Wave) at Intensity Level 3 (High) for Driver Seat
    client.setSeatMassage(
        enabled = true,
        mode = 2,
        level = 3,
        areaId = DeepalS05Property.AREA_DRIVER
    )
}
```

---

## 5. Memory Presets (M1, M2, M3) Architecture

The application allows users to store and restore comfort presets:
- **State Saved**: Driver Target Temperature, Fan Speed, Seat Heating Level, Seat Ventilation Level.
- **Rule**: Tapping a memory preset button (e.g. `M2`) loads and applies the saved temperature to the climate system and ruler. Updating the temperature while in that memory slot automatically saves the new setting to that preset.
