# Deepal S05 Voice Assistant, Outside Audio & Cockpit Scenes Guide
### "Hello Deepal" Voice Integration, Outside Speaker Broadcast, and Smart Scenes

---

## 1. System Overview

The **Changan Deepal S05** cockpit features an integrated voice assistant, external acoustic speaker system, and automated scene manager capable of controlling hardware subsystems via verbal speech, external broadcast, or single-tap macro actions.

---

## 2. Voice & Speech TTS Architecture (Ground Truth from `d+`)

The Deepal S05 provides in-cabin and external vehicle speaker text-to-speech (TTS) via the `VrLogicService` system binder:

### Speech Service Constants
- **Package**: `com.tinnove.wecarspeech` (fallback: `com.wt.speechserver`)
- **Service Class**: `com.tinnove.vrlogic.server.VrLogicService`
- **AIDL Interface**: `com.tinnove.vrinterface.IVrLogicService`
- **In-Cabin TTS (Transact 0x1b / 27)**: Plays synthetic voice inside the cabin through cockpit speakers.
- **Outside Vehicle TTS (Transact 0x62 / 98)**: Broadcasts speech outside through the pedestrian warning / external speaker horn.
- **Clear Outside Speech (Transact 0x60 / 96)**: Stops and clears active outside broadcast.
- **Outside Speaker Switch**: `Settings.Global.putInt(context.contentResolver, "tinnove_voice_outofcar", 1)`.
- **Outside Music Event**: `CarAudioManager.setCarEvent(0x66, 1 / 0)`.

### Kotlin Sample Code: Dispatching In-Cabin & Outside Speaker Speech TTS

```kotlin
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Parcel
import android.provider.Settings
import com.deepal.sdk.DeepalS05Property

class DeepalSpeechManager(private val context: Context) {

    /**
     * Broadcasts synthetic speech through the external pedestrian warning speaker horn.
     */
    fun playOutsideTts(text: String, binder: IBinder) {
        // 1. Enable outside speaker global setting
        try {
            Settings.Global.putInt(context.contentResolver, DeepalS05Property.SPEECH_SETTING_OUTSIDE_SPEAKER, 1)
        } catch (_: Throwable) {}

        // 2. Dispatch Transact 0x62 (playOutsideTts) to VrLogicService
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.SPEECH_DESCRIPTOR)
            data.writeString(text)
            data.writeInt(1) // Priority
            binder.transact(DeepalS05Property.SPEECH_TRANSACT_PLAY_OUTSIDE_TTS, data, reply, 0)
            reply.readException()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Stops and clears any ongoing outside speaker speech.
     */
    fun stopOutsideTts(binder: IBinder) {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.SPEECH_DESCRIPTOR)
            binder.transact(DeepalS05Property.SPEECH_TRANSACT_CLEAR_OUTSIDE_TTS, data, reply, 0)
            reply.readException()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    /**
     * Plays voice assistant response inside the cabin.
     */
    fun playCabinTts(text: String, binder: IBinder) {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(DeepalS05Property.SPEECH_DESCRIPTOR)
            data.writeString(text)
            binder.transact(DeepalS05Property.SPEECH_TRANSACT_PLAY_TTS, data, reply, 0)
            reply.readException()
        } finally {
            data.recycle()
            reply.recycle()
        }
    }
}
```

---

## 3. Supported Voice Command Phrases

| Spoken Phrase | Triggered Hardware Action | Description |
|:---|:---|:---|
| *"Open the sunroof"* / *"Close sunroof"* | `setSunroofShade(1 / 2)` | Opens or closes the electric sunroof shade (`wt.vehiclesetting` Transact `0x40`) |
| *"Open trunk"* / *"Close trunk"* | `setTailgate(true / false)` | Actuates power liftgate (`0x31400313` / `0x31400314`) |
| *"Turn on driver seat massage"* | `setSeatMassage(true, mode=1, level=3)` | Activates massage for driver (`0x31400b2f`, `0x31400b31`, `0x31400b30`) |
| *"I'm feeling hot"* / *"Rapid cool"* | `applyScene("RAPID_COOL")` | Max AC (`0x3540010b`), blower level 7, max seat ventilation (`0x35400111`) |
| *"Defrost the windshield"* | `applyScene("DEFROST")` | Front max defroster (`0x33400103`), rear defroster (`0x3540010c`) |
| *"Nap mode"* / *"Take a rest"* | `applyScene("NAP")` | Low AC (24°C), fan speed 1, sunroof closed, seat massage on |
| *"Camp mode"* | `applyScene("CAMP")` | Steady cabin AC (23°C), fan speed 2, emerald ambient lighting |
| *"Find charging stations"* | Opens EV Charging Station overlay | Shows nearest high-power DC fast-chargers |

---

## 4. Smart Cockpit Scenes Automation

Scenes orchestrate multiple vehicle domains simultaneously:

```kotlin
val client = DeepalS05Client()

suspend fun activateScene(sceneName: String) {
    when (sceneName.uppercase()) {
        "RAPID_COOL" -> {
            client.setClimatePower(true)
            client.setClimateTemperature(18.0f)
            client.setFanSpeed(7)
            client.setSeatVentilation(3, area = DeepalS05Property.AREA_DRIVER)
            client.setSeatVentilation(3, area = DeepalS05Property.AREA_PASSENGER)
            client.setWindows(2)      // Close windows
            client.setSunroofShade(2) // Close shade to block heat
        }
        "NAP" -> {
            client.setWindows(2)
            client.setSunroofShade(2)
            client.setClimateTemperature(24.0f)
            client.setFanSpeed(1)
            client.setSeatHeating(1, area = DeepalS05Property.AREA_DRIVER)
            client.setSeatMassage(true, mode = 1, level = 1)
            client.setAmbientLight(2, 30) // Amber dim
        }
        "DEFROST" -> {
            client.setFrontDefrost(true)
            client.setRearDefrost(true)
            client.setSteeringWheelHeat(true)
            client.setSeatHeating(3, area = DeepalS05Property.AREA_DRIVER)
        }
        "CAMP" -> {
            client.setClimatePower(true)
            client.setClimateTemperature(23.0f)
            client.setFanSpeed(2)
            client.setAmbientLight(1, 50) // Forest Emerald
        }
    }
}
```
