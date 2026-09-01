# Changan Deepal S05 Head-Up Display (AR-HUD) Development Guide
### In-Depth Engineering & Implementation Manual for Secondary Display Windshield Presentation and InCall IPC Protocols

---

## 1. System Overview & Hardware Architecture

The **Changan Deepal S05** (Platform C857 / EPA OpenOS) is equipped with an Augmented Reality Head-Up Display (AR-HUD) system that projects navigational cues, driving dynamics, and vehicle telemetry directly into the driver's forward field of view on the front windshield.

In the Android OS subsystem, the HUD is driven through two complementary mechanisms:
1. **Physical Windshield Secondary Display Presentation (`android.app.Presentation`)**:
   - The automotive head unit exposes a dedicated secondary hardware `Display` (display resolution `800 x 480` pixels).
   - The application renders a customized native Android `Presentation` surface on this display.
2. **Changan InCall Double-Interactive IPC Protocol (`DeepalHudClient`)**:
   - Inter-process communication (IPC) via Android Binder with `com.incall.SVR_MNG_SERVICE` and `com.incall.double.INTERACTIVE_SERVICE`.
   - Transmits maneuver icons (`0x18`), countdown distances (`0x18`), road names (`0x1a`), remain metrics (`0x1b`), and navigation state (`0x16`) to the vehicle's native cluster/HUD controller.
   - Manages navigation focus (`0x3f` request focus, `0x40` abandon focus) attaching the `INaviFocusCallback` token.

---

## 2. Optical Projection Physics & Display Geometry

```
+---------------------------------------------------------------------------------------+
|  800 x 480 Total Secondary Display Surface (Pure Black #000000)                       |
|                                                                                       |
|   [Left Zone: x=40]          [Center Navigation Axis]          [Right Zone: x=573]    |
|   [🔋] 59% • 209km           ‹ ‹ ‹ ‹                           Next: Russian Blvd (A) |
|   LIMIT 60                   ↰ 57m                             +------------------+   |
|   TPMS 2.4 | 2.4             P  0 km/h                         | x=573, y=167     |   |
|                                                                | w=227, h=188     |   |
|                                                                | Vector Map View  |   |
|                                                                +------------------+   |
+---------------------------------------------------------------------------------------+
```

### Optical Rules
- **Pure Black Canvas (`#000000`)**: The optical prism in the windshield projection assembly projects light onto the glass. Pixels with value `#000000` (RGB 0,0,0) emit zero luminance and appear completely transparent to the driver. Non-black pixels are projected as luminous elements floating over the road.
- **High-Contrast Colors**:
  - **Cyan Neon (`#00E5FF` / `#4DD0E1`)**: High daylight visibility and night-driving comfort.
  - **Pure White (`#FFFFFF`)**: Maneuver icons, countdown distances, speed values.
  - **Red Alert (`#FF5252`)**: Overspeed warnings and camera alerts.

### Pixel Layout Coordinate Map

| Element | Alignment / Position | Width x Height | Description |
|:---|:---|:---|:---|
| **Root Canvas** | `(0, 0)` | `800 x 480` | Background color set to `#000000` |
| **Left Zone** | `Gravity.START`, `x = 40` | `Wrap Content` | Battery SoC, Range, Speed Limit, TPMS |
| **Center Axis** | `Gravity.CENTER`, `x = 400` | `Wrap Content` | AR Chevrons, Turn Arrow, Distance, Gear, Speed |
| **Right Banner** | `x = 553, y = 135` | `267 x 32` | Next Road name ribbon and ADAS Pilot `(A)` |
| **Optical Map View** | `x = 573, y = 167` | `227 x 188` | MapLibre Texture-Mode vector map viewport |

---

## 3. InCall IPC Protocol (Ground Truth)

```kotlin
val hud = DeepalHudClient()

suspend fun updateGuidance() {
    // 1. Request navigation display focus (Transact 0x3f)
    hud.requestNaviFocus("com.deepalnav")

    // 2. Set active guidance state (Transact 0x16: 1=Active)
    hud.sendNavigateStatus(1)

    // 3. Send turn icon (e.g. 2 = Right Turn) and distance (150m) (Transact 0x18)
    hud.sendNavigateTurnInfo(turnIcon = 2, turnDistMeters = 150)

    // 4. Send road names (Transact 0x1a)
    hud.sendNavigateRoadInfo(nextRoad = "Russian Blvd", curRoad = "Monivong Blvd")

    // 5. Send remaining distance (12500m) and remaining time (840s) (Transact 0x1b)
    hud.sendNavigateRemainInfo(remainDistMeters = 12500, remainTimeSec = 840)
}
```
