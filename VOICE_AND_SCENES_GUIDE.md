# Deepal S05 Voice Assistant & Cockpit Scenes Guide
### "Hello Deepal" Voice Integration, Command Dispatcher, and Smart Scenes

---

## 1. System Overview

The **Changan Deepal S05** cockpit features an integrated voice assistant and automated scene manager capable of controlling hardware subsystems via verbal speech or single-tap macro actions.

---

## 2. Voice Wake Word & Action Dispatcher

The voice engine listens for the wake phrase **"Hello Deepal"** via `VoiceWakeService` using continuous audio analysis:

### Supported Voice Command Phrases

| Spoken Phrase | Triggered Hardware Action | Description |
|:---|:---|:---|
| *"Open the sunroof"* / *"Close sunroof"* | `setSunroofShade(1 / 2)` | Opens or closes the electric sunroof shade (`0x31400313`) |
| *"Turn on driver seat massage"* | `setSeatMassage(true, mode=1, level=3)` | Activates massage for driver (`0x31400b2f`, `0x31400b31`, `0x31400b30`) |
| *"I'm feeling hot"* / *"Rapid cool"* | `applyScene("RAPID_COOL")` | Max AC (`0x3540010b`), blower level 7, max seat ventilation (`0x35400111`) |
| *"Defrost the windshield"* | `applyScene("DEFROST")` | Front max defroster (`0x33400103`), rear defroster (`0x3540010c`) |
| *"Nap mode"* / *"Take a rest"* | `applyScene("NAP")` | Low AC (24°C), fan speed 1, sunroof closed, seat massage on |
| *"Camp mode"* | `applyScene("CAMP")` | Steady cabin AC (23°C), fan speed 2, emerald ambient lighting |
| *"Find charging stations"* | Opens EV Charging Station overlay | Shows nearest high-power DC fast-chargers |

---

## 3. Smart Cockpit Scenes Automation

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
