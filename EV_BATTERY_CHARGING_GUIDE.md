# Deepal S05 EV Battery, Preconditioning & Energy Dynamics Guide
### High-Voltage Battery Telemetry, Fast-Charge Thermal Preconditioning, REEV Telemetry & TPMS

---

## 1. System Architecture

The **Changan Deepal S05** features a dedicated Battery Management System (BMS), Thermal Management Domain, and Range Extender (REEV) Controller communicating via OpenOS VirtualCar property buses.

### Key Property IDs (Ground Truth from `d+` Disassembly)

| Property Constant | Hex ID | Area Mask | Data Type | Description |
|:---|:---|:---|:---|:---|
| `PROP_BATTERY_SOC` | `0x3140028c` | Area `0x1b` (27) | `Int` (0 to 100) | High-voltage battery state of charge (%) |
| `PROP_REMAINING_RANGE_EV_DTE` | `0x31400501` | Area 0 (Global) | `Int` (km) | Remaining pure EV driving range (`vc_alias_e_dte`) |
| `PROP_REMAINING_RANGE_DISP_DTE` | `0x31600205` | Area 0 (Global) | `Int` (km) | Instrument cluster display DTE (`vc_alias_disp_dte`) |
| `PROP_ODOMETER` | `0x31600204` | Area 0 (Global) | `Float` (raw meters) | Vehicle total odometer (divide by `1000f` for km) |
| `PROP_TIRE_PRESSURE` | `0x37600211` | FL (`0x01`), FR (`0x02`), RL (`0x04`), RR (`0x08`) | `Float` (Bar) | Real-time tyre pressure telemetry (`vc_alias_tire_pressure`) |
| `PROP_BATTERY_PRECONDITIONING` | `0x314006c6` | Area 0 (Global) | `Int` (1=On, 2=Off) | DC Fast-Charging Thermal Preconditioning |
| `PROP_THIS_TRIP_ELEC_AVG_CONSUMPTION` | `0x314005a6` | Area 0 (Global) | `Float` (kWh/100km) | Current trip average electric consumption |
| `PROP_THIS_TRIP_OIL_AVG_CONSUMPTION` | `0x314005ce` | Area 0 (Global) | `Float` (L/100km) | Current trip average fuel consumption (REEV models) |
| `PROP_THIS_TRIP_REEV_ELEC_DISTANCE` | `0x31400590` | Area 0 (Global) | `Float` (km) | Current trip pure electric driving distance |
| `PROP_THIS_TRIP_REEV_ELEC_TIME` | `0x31400591` | Area 0 (Global) | `Int` (minutes) | Current trip pure electric driving duration |
| `PROP_THIS_TRIP_REEV_FUEL_DISTANCE` | `0x314005ae` | Area 0 (Global) | `Float` (km) | Current trip fuel driving distance |
| `PROP_THIS_TRIP_REEV_FUEL_TIME` | `0x314005af` | Area 0 (Global) | `Int` (minutes) | Current trip fuel driving duration |

---

## 2. Battery Telemetry & Reactive Flow

The SDK exposes continuous battery and energy telemetry through `StateFlow<DeepalS05Telemetry>`:

```kotlin
val client = DeepalS05Client()

// Monitor battery and energy metrics in CoroutineScope
lifecycleScope.launch {
    client.telemetry.collect { telemetry ->
        val soc = telemetry.batterySocPercent
        val rangeKm = telemetry.remainingRangeKm
        val evRangeKm = telemetry.evRemainingRangeKm
        val isPreconditioning = telemetry.isBatteryPreconditioning

        println("Battery SoC: $soc%, Remaining Range: $rangeKm km (EV DTE: $evRangeKm km)")
        println("Trip Consumption: ${telemetry.tripElecAvgKwhPer100Km} kWh/100km")
        println("Tire Pressures (Bar): FL=${telemetry.tirePressureFlBar}, FR=${telemetry.tirePressureFrBar}, RL=${telemetry.tirePressureRlBar}, RR=${telemetry.tirePressureRrBar}")
        println("Battery Thermal Preconditioning: $isPreconditioning")
    }
}
```

---

## 3. DC Fast-Charging Battery Preconditioning

### Why Battery Preconditioning is Required
Lithium Iron Phosphate (LFP) and Ternary NMC battery cells require an internal core temperature between **25°C and 35°C** to safely accept maximum DC fast-charging power (up to **120 kW / 150 kW** on Deepal S05).

When approaching a fast-charging station in cold or ambient conditions:
- Turning on `PROP_BATTERY_PRECONDITIONING = 0x314006c6` activates the vehicle's heat pump / PTC coolant heater to bring the battery pack to peak charging temperature.
- This reduces 30% to 80% DC charge times down to **15-20 minutes**.

### Enabling / Disabling Preconditioning
```kotlin
suspend fun controlPreconditioning() {
    // 1. Activate Preconditioning 15 minutes before reaching DC charger
    client.setBatteryPreconditioning(enabled = true)

    // 2. Disable Preconditioning once charging begins
    client.setBatteryPreconditioning(enabled = false)
}
```

---

## 4. Driving Dynamics Modes (`PROP_DRIVE_MODE = 0x3140040d`)

The Deepal S05 powertrain controller supports 4 driving mode presets:
- `DRIVE_MODE_COMFORT = 1`: Standard balanced regeneration and steering weight (`\u8212\u9002`)
- `DRIVE_MODE_SPORT = 2`: Instant torque response, firm suspension tuning (`\u8fd0\u52a8`)
- `DRIVE_MODE_ECO = 3`: Maximum efficiency, optimized energy recovery (`\u7ecf\u6d4e`)
- `DRIVE_MODE_CUSTOM = 4`: User configurable response curves (`\u81ea\u5b9a\u4e49`)

```kotlin
suspend fun setPowertrainMode() {
    // Switch to Sport Mode for mountain driving
    client.setDriveMode(DeepalS05Property.DRIVE_MODE_SPORT)

    // Switch to Eco Mode for highway range preservation
    client.setDriveMode(DeepalS05Property.DRIVE_MODE_ECO)
}
```
