`com.openos.virtualcar` is the **core Vehicle Hardware Abstraction Layer (VHAL) and system service** built into Changan Deepal vehicles (including the Deepal S05, S07, and L07 running Changan OpenOS).

It acts as the **bridge between Android applications and the physical vehicle microcontrollers (ECUs)** across the car's CAN, LIN, and Automotive Ethernet buses.

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
│     Registered in Android ServiceManager as "virtualcar"│
└───────────────────────────┬────────────────────────────┘
                            │ (Transact Code 2)
                            ▼
┌────────────────────────────────────────────────────────┐
│     com.openos.virtualcar.IVirturalCarProperty         │
│     (OEM Property Broker & Hardware Dispatcher)        │
└───────────────────────────┬────────────────────────────┘
                            │ (CAN / Ethernet Bus Gateway)
         ┌──────────────────┼──────────────────┐
         ▼                  ▼                  ▼
   ┌───────────┐      ┌───────────┐      ┌───────────┐
   │ HVAC & AC │      │  Body &   │      │ Battery & │
   │  Domain   │      │  Windows  │      │ Powertrain│
   └───────────┘      └───────────┘      └───────────┘
```

---

## 2. Key AIDL Interfaces

### 1. `IVirtualCar` (Service Root)
Registered in the Android system server under the service name `"virtualcar"`.
* **Resolution**: Acquired via `ServiceManager.getService("virtualcar")`.
* **Function**: Exposes `getCarPropertyService()` (Transact Code `2`), which returns the hardware property broker binder.

### 2. `IVirturalCarProperty` (Hardware Property Broker)
*(Note: Uses the OEM spelling with 'r')*
* **Reading Signals**:
  * `getIntProperty(propId, areaId)`: Reads integer telemetry (e.g. Battery SoC %, Gear, Fan Speed).
  * `getFloatProperty(propId, areaId)`: Reads floating point telemetry (e.g. Vehicle Speed km/h, Cabin Temperature °C).
* **Writing Commands**:
  * `setProperty(propId, areaId, className, value)`: Actuates vehicle hardware (e.g. setting climate to 22.5°C, rolling down windows, opening the sunroof shade, or locking doors).

---

## 3. What Domains Does `com.openos.virtualcar` Control?

| Domain | What It Controls | Example Properties |
|:---|:---|:---|
| ⚡ **Powertrain & Battery** | Live Speed, Gear (`P/R/N/D`), Battery SoC %, Range, Battery Preconditioning | `0x31600204`, `0x3140028c`, `0x314006c4` |
| ❄️ **Climate & HVAC** | Dual-Zone Temperature (17.5°C-32.5°C), 8 Fan Speeds, AC, Front/Rear Defrost | `0x35600105`, `0x35400107`, `0x3520010c` |
| 💺 **Seat Comfort** | 3-Level Heating, 3-Level Ventilation, Pneumatic Massage Modes | `0x3540010f`, `0x35400111`, `0x35400125` |
| 🚪 **Body & Access** | 4 Power Windows, Sunroof Roller Blind, Power Tailgate, Central Locks | `0x33400301`, `0x31400313`, `0x15400505` |
| 🌧️ **Environment & Safety** | Rain Sensors, Ambient Lighting, Tire Pressures (TPMS) | `0x31400277`, `0x31400280` |

---

## 4. Why Deepal Uses `com.openos.virtualcar` Instead of Standard Android Car API

Standard Google Android Automotive OS (AAOS) uses `android.car.CarPropertyManager`. 

However, Changan built **`com.openos.virtualcar`** for their **EPA OpenOS platform** to:
1. Provide **sub-millisecond low-latency control** for high-frequency driving telemetry (speedometer, AR-HUD chevrons).
2. Enable custom EV hardware extensions (such as **Fast-Charging Battery Preconditioning**, **Multi-Mode Pneumatic Seat Massage**, and **Rain-Sensing Auto Guardian**).