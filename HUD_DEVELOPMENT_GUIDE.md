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
   - Transmits maneuver icons, countdown distances, road names, and trip metrics to the vehicle's native cluster/HUD controller.

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

## 3. Windshield Presentation Implementation (`DeepalHudPresentation`)

### Why MapLibre `TextureView` is Required
Standard Android `SurfaceView` punches a hole through the view hierarchy and can cause z-ordering conflicts, flicker, or blank frames inside secondary `Presentation` contexts on automotive GPUs. 

Using `MapLibreMapOptions.textureMode(true)` instructs MapLibre to render into a standard `TextureView`, ensuring flawless composition inside the 800x480 presentation window.

### Presentation Class Template
```kotlin
package com.deepalnav.ui.components

import android.app.Activity
import android.app.Presentation
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.os.Bundle
import android.view.Display
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap

class DeepalHudPresentation(
    activity: Activity,
    display: Display,
    private val styleJson: String
) : Presentation(activity, display) {

    companion object {
        const val HUD_SURFACE_WIDTH = 800
        const val HUD_SURFACE_HEIGHT = 480
        const val HUD_MAP_LEFT = 573
        const val HUD_MAP_TOP = 167
        const val HUD_MAP_WIDTH = 227
        const val HUD_MAP_HEIGHT = 188
    }

    private var hudManeuverView: TextView? = null
    private var hudDistanceView: TextView? = null
    private var hudBatteryRangeView: TextView? = null
    private var hudSpeedLimitView: TextView? = null
    private var hudTpmsView: TextView? = null
    private var hudSpeedView: TextView? = null
    private var hudSpeedKmhView: TextView? = null
    private var hudNextRoadView: TextView? = null
    private var mapLibreMap: MapLibreMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Root Black Canvas
        val root = FrameLayout(context).apply {
            setBackgroundColor(Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // 2. Texture-Mode MapView at Optical Window
        val mapOptions = MapLibreMapOptions.createFromAttributes(context)
            .textureMode(true)
            .attributionEnabled(false)
            .logoEnabled(false)
            .compassEnabled(false)

        val mapView = MapView(context, mapOptions).apply {
            layoutParams = FrameLayout.LayoutParams(HUD_MAP_WIDTH, HUD_MAP_HEIGHT).apply {
                leftMargin = HUD_MAP_LEFT
                topMargin = HUD_MAP_TOP
            }
        }
        root.addView(mapView)

        // 3. UI Elements Container
        val hudContainer = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Add Left Block, Center Block, Right Block...
        root.addView(hudContainer)
        setContentView(root)

        // Initialize Map
        mapView.onCreate(null)
        mapView.getMapAsync { map ->
            mapLibreMap = map
            map.setStyle(org.maplibre.android.maps.Style.Builder().fromJson(styleJson))
        }
    }
}
```

---

## 4. Secondary Display Discovery & Auto-Attachment

In your automotive `Activity` or `Service`, listen for display connections or inspect `DisplayManager`:

```kotlin
import android.content.Context
import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import android.view.Display

class HudDisplayManager(private val context: Context) {
    private var hudPresentation: DeepalHudPresentation? = null

    fun attachHudIfAvailable(styleJson: String, activity: android.app.Activity) {
        val dm = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displays = dm.displays

        for (display in displays) {
            if (display.displayId != Display.DEFAULT_DISPLAY) {
                val metrics = DisplayMetrics()
                display.getRealMetrics(metrics)

                // Match Deepal S05 AR-HUD resolution (800x480)
                if (metrics.widthPixels == 800 && metrics.heightPixels == 480) {
                    if (hudPresentation == null) {
                        hudPresentation = DeepalHudPresentation(activity, display, styleJson).apply {
                            show()
                        }
                    }
                    return
                }
            }
        }
    }

    fun detach() {
        hudPresentation?.dismiss()
        hudPresentation = null
    }
}
```

---

## 5. InCall IPC Navigation Protocol (`DeepalHudClient`)

The vehicle domain controller accepts navigation guidance packets via `com.incall.double.INTERACTIVE_SERVICE`.

### Protocol Transaction Summary

| Transact Code | Method | Payload Parameters | Description |
|:---|:---|:---|:---|
| **`0x3f` (63)** | `requestNaviFocus` | `String` (package name) | Acquires cluster/HUD focus for navigation |
| **`0x40` (64)** | `abandonNaviFocus` | `String` (package name) | Releases cluster/HUD focus on trip completion |
| **`0x16` (22)** | `sendNavigateStatus`| `Int` (`1`=Active, `2`=Arrived, `0`=Idle) | Guidance status state machine |
| **`0x18` (24)** | `sendNavigateTurnInfo` | `Int` (iconId), `Int` (distMeters) | Maneuver turn icon and distance countdown |
| **`0x1a` (26)** | `sendNavigateRoadInfo` | `String` (nextRoad), `String` (curRoad) | Road name ribbons |
| **`0x1b` (27)** | `sendNavigateRemainInfo` | `Int` (remainDistM), `Int` (remainSec) | Trip total remaining distance & ETA |

### Maneuver Turn Icon IDs

| Icon ID | Maneuver Description | Visual Symbol |
|:---|:---|:---|
| **`1`** | Continue Straight | `↑` |
| **`2`** | Turn Right | `↱` |
| **`3`** | Turn Left | `↰` |
| **`4`** | Slight Right | `↗` |
| **`5`** | Slight Left | `↖` |
| **`6`** | U-Turn | `⮌` |
| **`7`** | Roundabout | `⮡` |

### Full Guidance Session Example
```kotlin
val hudClient = DeepalHudClient()

suspend fun startGuidanceSession() {
    // 1. Request focus
    hudClient.requestNaviFocus("com.deepalnav")

    // 2. Set status to active
    hudClient.sendNavigateStatus(1)

    // 3. Send turn info (Right turn in 200m)
    hudClient.sendNavigateTurnInfo(turnIcon = 2, turnDistMeters = 200)

    // 4. Send road names
    hudClient.sendNavigateRoadInfo(
        nextRoad = "Russian Federation Blvd",
        curRoad = "Preah Monivong Blvd"
    )

    // 5. Send trip remaining info (4.8 km, 720 sec)
    hudClient.sendNavigateRemainInfo(remainDistMeters = 4800, remainTimeSec = 720)
}

suspend fun endGuidanceSession() {
    hudClient.sendNavigateStatus(2) // Arrived
    hudClient.abandonNaviFocus("com.deepalnav")
    hudClient.clear()
}
```

---

## 6. Live Testing & Diagnostics

To test and verify the HUD integration without physical road driving:
1. Launch **DeepalNav** on the head unit or emulator.
2. Go to **Settings** -> Scroll down to **About**.
3. **Tap 5 times** on the About card to open the **Deepal S05 Full Hardware API Suite**.
4. Select the **HUD** category to trigger turn icons, distances, road names, and status updates interactively.
