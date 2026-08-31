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
| *"Open the sunroof"* / *"Close sunroof"* | `setSunroofShade(1 / 2)` | Opens or closes the electric sunroof shade |
| *"Turn on driver seat massage"* | `setSeatMassage(true, mode=2, level=3)` | Activates wave massage for driver |
| *"I'm feeling hot"* / *"Rapid cool"* | `applyScene("RAPID_COOL")` | Max AC, blower level 7, max seat ventilation |
| *"Defrost the windshield"* | `applyScene("DEFROST")` | Front max defroster, high heat, blower level 7 |
| *"Nap mode"* / *"Take a rest"* | `applyScene("NAP")` | Low AC (24°C), fan speed 2, sunroof closed |
| *"Camp mode"* | `applyScene("CAMP")` | Steady cabin AC, screens dimmed, battery monitor |
| *"Find charging stations"* | Opens EV Charging Station overlay | Shows nearest high-power DC fast-chargers |

---

## 3. Smart Cockpit Scenes Automation

Scenes orchestrate multiple vehicle domains simultaneously:

```kotlin
val client = DeepalS05Client()

suspend fun activateScene(sceneName: String) {
    when (sceneName) {
        "RAPID_COOL" -> {
            // Cool down boiling cabin rapidly
            client.setClimateTemperature(18.0f)
            client.setAcEnabled(true)
            client.setFanSpeed(7)
            client.setSeatVentilation(level = 3, areaId = DeepalS05Property.AREA_DRIVER)
            client.setSeatVentilation(level = 3, areaId = DeepalS05Property.AREA_PASSENGER)
            client.setSunroofShade(action = 2) // Close shade to block solar heat
        }
        "NAP" -> {
            // Quiet, comfortable resting cabin
            client.setClimateTemperature(24.0f)
            client.setFanSpeed(2)
            client.setSeatHeating(level = 0)
            client.setSeatVentilation(level = 0)
            client.setSunroofShade(action = 2)
            client.setWindows(action = 2)
        }
        "DEFROST" -> {
            // Rapidly clear glass condensation & frost
            client.setFrontDefrost(true)
            client.setRearDefrost(true)
            client.setClimateTemperature(28.0f)
            client.setFanSpeed(8)
        }
        "CAMP" -> {
            // Continuous camp ventilation
            client.setClimateTemperature(23.0f)
            client.setFanSpeed(3)
        }
    }
}
```
