# Deepal S05 OpenOS AIDL Property Bus Architecture Guide
### Direct Binder IPC, ServiceManager Reflection, and Low-Level Hardware Interface

---

## 1. OpenOS VirtualCar Subsystem Architecture

On the **Changan Deepal S05** (Model C857 / EPA Platform), hardware signals and actuators are mediated by the `virtualcar_service` running in the Android system server.

### IPC Topology
```
[ DeepalNav Application / deepal-s05-sdk ]
               │
               ▼ (ServiceManager.checkService("virtualcar_service"))
[ com.openos.virtualcar.IVirtualCar ] (IBinder)
               │
               ▼ (transact code 2 -> getVirtualCarService("virtualcar_property_service"))
[ com.openos.virtualcar.IVirturalCarProperty ] (OEM Spelling with 'r')
               │
               ├──> Transact 2: setValue(VirtualCarValue value) -> int (0 == OK / success)
               ├──> Transact 3: getValue(int propId, int areaId) -> VirtualCarValue
               ├──> Transact 4: isSupport(int propId, int areaId) -> boolean
               ├──> Transact 5: register(int[] propIds, IVirtualCarPropertyEventListener listener)
               └──> Transact 6: unRegister(int[] propIds, IVirtualCarPropertyEventListener listener)
               │ (CAN / LIN / Automotive Ethernet Gateway)
         ┌─────┴──────────────┬──────────────────┐
         ▼                    ▼                  ▼
   ┌───────────┐        ┌───────────┐      ┌───────────┐
   │ HVAC & AC │        │  Body &   │      │ Battery & │
   │  Domain   │        │  Windows  │      │ Powertrain│
   └───────────┘        └───────────┘      └───────────┘
```

---

## 2. Low-Level AIDL Interface Definitions

### `IVirtualCar.aidl`
```java
package com.openos.virtualcar;

import android.os.IBinder;

interface IVirtualCar {
    void setVirtualCarServiceHelper(in IBinder helper); // Transact 1
    IBinder getVirtualCarService(String serviceName);    // Transact 2
    int getVirtualCarConnectionType();                   // Transact 3
}
```

### `IVirturalCarProperty.aidl`
```java
package com.openos.virtualcar;

import com.openos.virtualcar.entity.VirtualCarValue;
import com.openos.virtualcar.entity.VirtualPropertyConfig;
import com.openos.virtualcar.IVirtualCarPropertyEventListener;
import java.util.List;
import java.util.Map;

interface IVirturalCarProperty {
    int setValue(in VirtualCarValue value);                                                              // Transact 2
    VirtualCarValue getValue(int propId, int areaId);                                                   // Transact 3
    boolean isSupport(int propId, int areaId);                                                          // Transact 4
    void register(in int[] propIds, in IVirtualCarPropertyEventListener listener);                       // Transact 5
    void unRegister(in int[] propIds, in IVirtualCarPropertyEventListener listener);                     // Transact 6
    boolean isConnected();                                                                              // Transact 7
    List<VirtualPropertyConfig> getPropertyConfigList();                                                // Transact 8
    VirtualPropertyConfig getPropertyConfig(int propId);                                                // Transact 9
    boolean reportConcern(in Map concernMap, in Map params, in IVirtualCarPropertyEventListener listener);// Transact 10
    boolean reportUnConcern();                                                                          // Transact 11
}
```

### `IVirtualCarPropertyEventListener.aidl`
```java
package com.openos.virtualcar;

import com.openos.virtualcar.entity.VirtualCarValue;
import java.util.List;

interface IVirtualCarPropertyEventListener {
    void onEventList(in List<VirtualCarValue> list); // Transact 2
    void onChangeEvent(in VirtualCarValue value);    // Transact 3
    void onErrorEvent(int propId, int errorCode);    // Transact 4
}
```

---

## 3. Reverse Engineering Bytecode Mapping (from OEM Launcher & Framework)

The SDK implementation matches the ground-truth bytecode decompiled from `Deepal+.v26.0521.apk` (`com.deepalhome.launcher`):

| Framework / OEM Class | SDK Implementation | Role & Transaction Mapping |
|:---|:---|:---|
| `com.openos.virtualcar.IVirtualCar` | [`VirtualCarConnection.kt`](file:///d:/deepal-s05-sdk/deepal-s05-sdk/src/main/kotlin/com/deepal/sdk/VirtualCarConnection.kt) | ServiceManager resolution (`"virtualcar_service"`), Transact 2 `getVirtualCarService` |
| `com.openos.virtualcar.IVirturalCarProperty` | [`VirtualCarConnection.kt`](file:///d:/deepal-s05-sdk/deepal-s05-sdk/src/main/kotlin/com/deepal/sdk/VirtualCarConnection.kt) | Transact 2 `setValue`, Transact 3 `getValue`, Transact 5 `register` |
| `com.openos.virtualcar.entity.VirtualCarValue` | [`VirtualCarValue.kt`](file:///d:/deepal-s05-sdk/deepal-s05-sdk/src/main/kotlin/com/openos/virtualcar/entity/VirtualCarValue.kt) | Parcelable container: `mCategoryId`, `mFuncId`, `mAreaId`, `mCode`, `mTimestamp`, `mValue` |
| `com.deepalhome.launcher.util.CarS05InfoUtil` | [`DeepalS05Telemetry.kt`](file:///d:/deepal-s05-sdk/deepal-s05-sdk/src/main/kotlin/com/deepal/sdk/DeepalS05Telemetry.kt) | Signal aggregator: Door bitmasks (`0x36400311`), Tailgate (`0x31400314`), TPMS (`0x37600211`), Energy |
| `com.deepalhome.launcher.util.s05.control.S05CarControlHelper` | [`DeepalS05Client.kt`](file:///d:/deepal-s05-sdk/deepal-s05-sdk/src/main/kotlin/com/deepal/sdk/DeepalS05Client.kt) | HVAC, Seat heat/vent, Sunroof shade, Defrost, Windows |
| `com.incall.serversdk.server.ISvrManager` | [`DeepalHudClient.kt`](file:///d:/deepal-s05-sdk/deepal-s05-sdk/src/main/kotlin/com/deepal/sdk/DeepalHudClient.kt) | Resolves `"com.incall.SVR_MNG_SERVICE"` and `"com.incall.double.INTERACTIVE_SERVICE"` |
| `com.incall.serversdk.interactive.IDouInteractiveManager` | [`DeepalHudClient.kt`](file:///d:/deepal-s05-sdk/deepal-s05-sdk/src/main/kotlin/com/deepal/sdk/DeepalHudClient.kt) | AR-HUD & Cluster IPC: Status (`0x16`), Maneuvers (`0x18`), Roads (`0x1a`), ETA (`0x1b`), Focus (`0x3f`/`0x40`) |
| `com.tinnove.vrlogic.server.VrLogicService` | [`DeepalS05Property.kt`](file:///d:/deepal-s05-sdk/deepal-s05-sdk/src/main/kotlin/com/deepal/sdk/DeepalS05Property.kt) | In-cabin (`0x1b`) and outside-speaker (`0x62`) speech TTS playback |

---

## 4. Property Setter Implementation (Polymorphic Parcel Serialization)

```kotlin
suspend fun setVirtualCarValue(value: VirtualCarValue): Boolean = withContext(Dispatchers.IO) {
    val binder = getPropertyBinder() ?: return@withContext false

    val data = Parcel.obtain()
    val reply = Parcel.obtain()
    try {
        data.writeInterfaceToken("com.openos.virtualcar.IVirturalCarProperty")
        data.writeInt(1) // Has parcelable flag
        
        // VirtualCarValue Parcel serialization:
        data.writeInt(value.mCategoryId) // 0xFF00 and propId
        data.writeInt(value.mFuncId)     // propId
        data.writeInt(value.mAreaId)     // areaId
        data.writeInt(value.mCode)       // 0
        data.writeLong(value.mTimestamp) // timestamp
        if (value.mValue == null) {
            data.writeString("null")
        } else {
            data.writeString(value.mValue.javaClass.name)
        }
        data.writeValue(value.mValue)

        val ok = binder.transact(2, data, reply, 0)
        if (!ok) return@withContext false

        reply.readException()
        val resultCode = reply.readInt()
        resultCode == 0                  // 0 = Success / OK
    } finally {
        data.recycle()
        reply.recycle()
    }
}
```

---

## 5. Property Getter Implementation (Parcel Deserialization)

```kotlin
fun getVirtualCarValue(propId: Int, areaId: Int = DeepalS05Property.AREA_GLOBAL): VirtualCarValue? {
    val binder = getPropertyBinder() ?: return null

    val data = Parcel.obtain()
    val reply = Parcel.obtain()
    try {
        data.writeInterfaceToken("com.openos.virtualcar.IVirturalCarProperty")
        data.writeInt(propId)
        data.writeInt(areaId)

        val ok = binder.transact(3, data, reply, 0)
        if (!ok) return null

        reply.readException()
        val hasValue = reply.readInt()
        if (hasValue == 0) return null

        // Unpack VirtualCarValue from parcel:
        val categoryId = reply.readInt()
        val funcId = reply.readInt()
        val area = reply.readInt()
        val code = reply.readInt()
        val timestamp = reply.readLong()
        val typeString = reply.readString()

        val classLoader = if (!typeString.isNullOrEmpty() && typeString != "null") {
            try {
                Class.forName(typeString).classLoader
            } catch (_: Throwable) {
                null
            }
        } else null

        val value = reply.readValue(classLoader)
        return VirtualCarValue(categoryId, funcId, area, code, timestamp, value)
    } finally {
        data.recycle()
        reply.recycle()
    }
}
```
