# Changan OpenOS VirtualCar Subsystem Reference
### Cockpit Architecture, Vehicle Hardware Abstraction, and Multi-Domain Bus Matrix

`com.openos.virtualcar` is the **core Vehicle Hardware Abstraction Layer (VHAL) and system service** built into Changan Deepal vehicles (including the Deepal S05, S07, and L07 running Changan OpenOS).

It acts as the **bridge between Android applications and physical vehicle microcontrollers (ECUs)** across the car's CAN, LIN, and Automotive Ethernet buses.

---

## 1. System Architecture & Topology

```
┌────────────────────────────────────────────────────────┐
│             DeepalNav Application / SDK                │
└───────────────────────────┬────────────────────────────┘
                            │ (Android Binder IPC)
                            ▼
┌────────────────────────────────────────────────────────┐
│        com.openos.virtualcar.IVirtualCar               │
│     Registered in ServiceManager as "virtualcar_service"│
└───────────────────────────┬────────────────────────────┘
                            │ (Transact Code 2)
                            ▼
┌────────────────────────────────────────────────────────┐
│     com.openos.virtualcar.IVirturalCarProperty         │
│     (OEM Property Broker & Hardware Dispatcher)        │
└───────────────────────────┬────────────────────────────┘
                            │ (CAN / LIN / Ethernet Bus Gateway)
         ┌──────────────────┼──────────────────┐
         ▼                  ▼                  ▼
   ┌───────────┐      ┌───────────┐      ┌───────────┐
   │ HVAC & AC │      │  Body &   │      │ Battery & │
   │  Domain   │      │  Windows  │      │ Powertrain│
   └───────────┘      └───────────┘      └───────────┘
```

---

## 2. Key AIDL Interfaces & System Services

### 1. `IVirtualCar` (Service Root)
Registered in the Android system server under the service name `"virtualcar_service"`.
* **Resolution**: Acquired via `ServiceManager.getService("virtualcar_service")`.
* **Methods**:
  * `setVirtualCarServiceHelper(IBinder helper)` (Transact `1`)
  * `getVirtualCarService(String)` (Transact `2`): Returns `IVirturalCarProperty` binder when called with `"virtualcar_property_service"`.
  * `getVirtualCarConnectionType()` (Transact `3`).

### 2. `IVirturalCarProperty` (Hardware Property Broker)
*(Note: Uses the OEM spelling with 'r')*
* **Reading Signals (Transact Code 3)**:
  * `getValue(int propId, int areaId)`: Returns `VirtualCarValue` containing typed telemetry (e.g. Battery SoC %, Gear, Speed, Cabin Temp, TPMS, Range).
* **Writing Commands (Transact Code 2)**:
  * `setValue(VirtualCarValue value)`: Actuates vehicle hardware with polymorphic value serialization. Status code `0 == OK / success`.
* **Event Subscription (Transact Code 5 & 6)**:
  * `register(int[] propIds, IVirtualCarPropertyEventListener listener)` / `unRegister(...)`.

### 3. `wt.vehiclesetting` (`IVehicleSettingInterface`)
Vehicle preferences and settings service for HUD brightness/height/nav toggle, exterior lighting, mirrors, smart trunk, and ADAS driving assistance.

### 4. `com.incall.double.INTERACTIVE_SERVICE` (`IDouInteractiveManager`)
AR-HUD and instrument cluster interconnect service for navigation state (`0x16`), turn icons (`0x18`), street names (`0x1a`), ETA (`0x1b`), media metadata (`0x25`-`0x27`), and focus management (`0x3f`/`0x40`).

### 5. `com.tinnove.vrlogic.server.VrLogicService` (`IVrLogicService`)
Speech engine providing in-cabin TTS (`0x1b`) and external vehicle speaker broadcast (`0x62`).

---

## 3. What Domains Does `com.openos.virtualcar` Control?

| Domain | What It Controls | Verified Property IDs (Ground Truth) |
|:---|:---|:---|
| ⚡ **Powertrain & Battery** | Live Speed, Gear (`P/R/N/D`), Battery SoC %, C857 Range, EV Range DTE, Display DTE, Total Odometer, Battery Preconditioning | `0x11600207`, `0x31600202`, `0x31400231`, `0x11400400`, `0x3140028c`, `0x314006c4`, `0x31400501`, `0x31600205`, `0x31600204`, `0x314006c6` |
| 📊 **Trip & REEV Dynamics** | Trip Electric Consumption (kWh/100km), REEV Fuel Consumption (L/100km), Electric/Fuel Distances & Times | `0x314005a6`, `0x314005cf`, `0x314005ce`, `0x31400590`, `0x31400591`, `0x314005ae`, `0x314005af` |
| 🛞 **Tire Pressure (TPMS)** | Real-Time Tire Pressures in Bar for FL (`0x01`), FR (`0x02`), RL (`0x04`), RR (`0x08`), Legacy TPMS ID | `0x37600211`, `0x31410605` |
| ❄️ **Climate & HVAC** | Dual-Zone Temperature (17.5°C-32.5°C), Cabin Internal Thermometer, 8 Fan Speeds, AC, Front/Rear Defrost, Recirculation, Auto, Sync | `0x35600105`, `0x38600112`, `0x35400109`, `0x35400102`, `0x33400103`, `0x3540010c`, `0x35400108`, `0x35400104`, `0x3540010d` |
| 💺 **Seat Comfort** | 3-Level Heating, 3-Level Ventilation, Pneumatic Massage (Pattern Modes 1-8: `0x31400b30`, Intensity Levels 1-3: `0x31400b31`), Steering Heat | `0x3540010f`, `0x1540050b`, `0x35400111`, `0x31400b2f`, `0x31400b30`, `0x31400b31`, `0x314003eb` |
| 🚪 **Doors & Access** | Individual 4-Door Sensing (`0x01`, `0x04`, `0x10`, `0x40`), 4 Power Windows, Power Tailgate Actuate & Status, Central Locks, Sunroof Sunshade | `0x36400311`, `0x33400301`, `0x33400300`, `0x31400313`, `0x31400314`, `0x314003eb`, `0x31400303`, `wt.vehiclesetting` (0x40) |
| 💡 **Cabin Lighting** | Ambient Light Preset Color & Toggle (`0x3140039a`), Brightness (`0x3140039b`), Color Choices (54, 42, 33, 12, 6, 1), Dynamic Patterns 1-3 (`0x31400677`), Air Purifier (`0x35400122`) | `0x3140039a`, `0x3140039b`, `0x31400677`, `0x35400122` |
| 🏎️ **Drive Dynamics & ADAS** | Drive Mode (`0x3140040d` / `0x314003f5`), Auto Emergency Braking (`0x3140040d` / `0x31400244`) | `0x3140040d`, `0x314003f5`, `0x31400244` |
| 🔊 **Audio & Voice** | Outside Speaker Music Event, In-Cabin & Outside Vehicle Speech TTS | Audio Event `0x66`, Speech Transacts `0x1b`, `0x62` |
