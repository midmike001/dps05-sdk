# Deepal S05 Climate & Seat Comfort Development Guide
### Dual-Zone HVAC, 8-Speed Blower, Multi-Level Seat Heat/Vent, and Pneumatic Massage

---

## 1. System Architecture

The Climate and Comfort domain in the **Changan Deepal S05** (Platform Model C857) controls dual-zone cabin temperature, airflow distribution, multi-stage seat heating/ventilation, and pneumatic massage programs through the OpenOS VirtualCar property bus and CarPropertyManager.

### Key Property IDs & Area Masks

| Property Constant | Hex ID | Area Mask | Data Type | Description |
|:---|:---|:---|:---|:---|
| `PROP_HVAC_TEMP_SET` | `0x35600105` | Area 1 (Driver), Area 4 (Passenger) | `Float` | Target cabin temperature (17.5°C .. 32.5°C in 0.5°C steps) |
| `PROP_HVAC_INTERNAL_TEMP` | `0x38600112` | Area 1 (Driver) | `Float` | Cabin ambient internal thermometer (°C) |
| `PROP_HVAC_POWER_ON` | `0x35400101` | Area 1 (Driver) & Area 0 (Global) | `Int` | Climate system power (1=On, 2=Off) |
| `PROP_HVAC_AC_ON` | `0x35400102` | Area 1 (Driver) | `Int` | AC compressor state (1=On, 2=Off) |
| `PROP_HVAC_AUTO` | `0x35400104` | Area 1 (Driver) & Area 0 (Global) | `Int` | Full automatic climate control (1=Auto, 2=Manual) |
| `PROP_HVAC_FAN_DIRECTION` | `0x35400107` | Area 1 (Driver) | `Int` | Air vent direction / blow mode |
| `PROP_HVAC_RECIRC` | `0x35400108` | Area 1 (Driver) | `Int` | Vendor Tri-State: `2`=Recirculation, `1`=Fresh Air |
| `PROP_HVAC_FAN_SPEED` | `0x35400109` | Area 1 (Driver) | `Int` | Blower fan speed (1 to 8) |
| `PROP_HVAC_MAX_AC` | `0x3540010b` | Area 1 (Driver) | `Int` | Maximum cooling mode (1=On, 2=Off) |
| `PROP_HVAC_SYNC` | `0x3540010d` | Area 1 (Driver) | `Int` | Dual-zone sync mode (1=Sync, 2=Dual) |
| `PROP_HVAC_DEFROST_FRONT` | `0x33400103` | Area 1 (Front Defrost), Area 2 (Rear Defrost) | `Int` | Front & Rear windshield defroster (1=On, 2=Off) |
| `PROP_HVAC_DEFROST_REAR` | `0x3540010c` | Area 1 (Driver) | `Int` | Rear glass & heated side mirrors (1=On, 2=Off) |
| `PROP_SEAT_HEATING` | `0x3540010f` | Area 1 (Driver), Area 4 (Passenger) | `Int` | Seat heating level (0 to 3) via VirtualCar |
| `PROP_SEAT_HEATING_CPM` | `0x1540050b` | Area 1 (Driver), Area 4 (Passenger) | `Int` | Seat heating level (0 to 3) via CarPropertyManager |
| `PROP_SEAT_VENTILATION`| `0x35400111` | Area 1 (Driver), Area 4 (Passenger) | `Int` | Seat ventilation level (0 to 3) |
| `PROP_SEAT_MASSAGE_TOGGLE` | `0x31400b2f` | Area 0 (Global), Area 4 (Passenger) | `Int` | Pneumatic massage toggle (1=On, 2=Off) |
| `PROP_SEAT_MASSAGE_MODE` | `0x31400b31` | Area 0 (Global), Area 4 (Passenger) | `Int` | Pneumatic massage program (1 to 3) |
| `PROP_SEAT_MASSAGE_LEVEL` | `0x31400b30` | Area 0 (Global), Area 4 (Passenger) | `Int` | Pneumatic massage intensity (1 to 8) |
| `PROP_STEERING_WHEEL_HEAT` | `0x314003eb` | Area 0 (Global) | `Int` | Steering wheel heating (1=On, 2=Off) |

---

## 2. Temperature Conversion & Dual-Zone Control

The Deepal S05 HVAC system accepts temperature values between **17.5°C and 32.5°C** in **0.5°C increments**:

### Setting Cabin Temperature
```kotlin
val client = DeepalS05Client()

suspend fun setDriverTemperature(targetTempC: Float) {
    val clamped = targetTempC.coerceIn(17.5f, 32.5f)
    // Send to Driver Zone (Area 1) via PROP_HVAC_TEMP_SET = 0x35600105
    client.setClimateTemperature(clamped, area = DeepalS05Property.AREA_DRIVER)
}

suspend fun setPassengerTemperature(targetTempC: Float) {
    val clamped = targetTempC.coerceIn(17.5f, 32.5f)
    // Send to Passenger Zone (Area 4) via PROP_HVAC_TEMP_SET = 0x35600105
    client.setClimateTemperature(clamped, area = DeepalS05Property.AREA_PASSENGER)
}
```

---

## 3. Fan Speed & Defrost Programs

The HVAC blower supports **8 discrete speed levels** (Level 1 to Level 8 via `PROP_HVAC_FAN_SPEED = 0x35400109`):

```kotlin
suspend fun configureBlowerAndDefrost() {
    // Set fan speed to level 4
    client.setFanSpeed(4)

    // Activate Front Max Windshield Defrost (0x33400103, Area 1)
    client.setFrontDefrost(true)

    // Activate Rear Window & Mirror Heating (0x3540010c / 0x33400103 Area 2)
    client.setRearDefrost(true)
}
```

---

## 4. Airflow Vent Distribution (Wind Direction)

The HVAC system supports discrete vent airflow distribution paths:
- `WIND_DIRECTION_DEFROST = 8`: Windshield defrost vents
- `WIND_DIRECTION_FACE = 9`: Dashboard / Face vents
- `WIND_DIRECTION_FEET = 10`: Footwell / Floor vents
- `WIND_DIRECTION_FACE_FEET = 11`: Dual Face + Feet vents

```kotlin
suspend fun configureAirflowDirection() {
    // Direct airflow to Face vents
    client.setWindDirection(DeepalS05Property.WIND_DIRECTION_FACE)

    // Direct airflow to Footwell vents
    client.setWindDirection(DeepalS05Property.WIND_DIRECTION_FEET)

    // Direct airflow to Windshield Defrost vents
    client.setWindDirection(DeepalS05Property.WIND_DIRECTION_DEFROST)
}
```

---

## 5. Seat Heating, Ventilation & Pneumatic Massage

### Seat Levels & Area Identifiers
- **Driver Seat**: `DeepalS05Property.AREA_DRIVER = 1`
- **Front Passenger Seat**: `DeepalS05Property.AREA_PASSENGER = 4`
- **Levels**: `0` = Off, `1` = Low, `2` = Medium, `3` = High

### Setting Seat Comfort
```kotlin
suspend fun configureSeats() {
    // Driver Seat: Heating Level 2, Ventilation Off
    client.setSeatHeating(level = 2, area = DeepalS05Property.AREA_DRIVER)
    client.setSeatVentilation(level = 0, area = DeepalS05Property.AREA_DRIVER)

    // Passenger Seat: Ventilation Level 3
    client.setSeatVentilation(level = 3, area = DeepalS05Property.AREA_PASSENGER)

    // Activate Driver Seat Pneumatic Massage (Mode 1, Intensity Level 3)
    client.setSeatMassage(enabled = true, mode = 1, level = 3)
}
```
