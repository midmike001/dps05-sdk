# Changan Deepal S05 Head-Up Display (AR-HUD) Development Guide
### In-Depth Engineering & Implementation Manual for Secondary Display Windshield Presentation and InCall IPC Protocols

---

## 1. System Overview & Hardware Architecture

The **Changan Deepal S05** (Platform C857 / EPA OpenOS) is equipped with an Augmented Reality Head-Up Display (AR-HUD) system that projects navigational cues, driving dynamics, and vehicle telemetry directly into the driver's forward field of view on the front windshield.

In the Android OS subsystem, the HUD is driven through two complementary mechanisms:
1. **Physical Windshield Secondary Display Presentation (`android.app.Presentation`)**:
   - The automotive head unit exposes a dedicated secondary hardware `Display` (display resolution `800 x 480` pixels, `displayId != 0`).
   - The application renders a customized native Android `Presentation` surface with overlay type `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY` (type 2038).
2. **Changan InCall Double-Interactive IPC Protocol (`DeepalHudClient`)**:
   - Inter-process communication (IPC) via Android Binder with `com.incall.SVR_MNG_SERVICE` and `com.incall.double.INTERACTIVE_SERVICE`.
   - Transmits maneuver icons (`0x18`), countdown distances (`0x18`), road names (`0x1a`), remain metrics (`0x1b`), cross road junction views (`0x17`), lane guidance (`0x19`), camera warnings (`0x1c`), and navigation state (`0x16`) to the vehicle's native cluster/HUD controller.
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

---

## 3. InCall IPC Protocol (Ground Truth from `d+`)

### Complete InCall Interactive Transaction Codes

| Command Constant | Transact Hex / Dec | Signature | Description |
|:---|:---|:---|:---|
| `INCALL_CMD_SWITCH_LVDS_FINISH` | `0x01` (1) | `sendSwitchLVDSFinishEvent(int)` | LVDS display stream switch acknowledge |
| `INCALL_CMD_CONTRA_NAVIGATE_EVENT` | `0x02` (2) | `sendContraNavigateEvent(int)` | Reverse / contra navigation event |
| `INCALL_CMD_GET_LOG_EVENT` | `0x03` (3) | `sendGetLogEvent()` | Diagnostic log trigger |
| `INCALL_CMD_360_TRIG_EVENT` | `0x04` (4) | `send360trigEvent(int)` | 360 panoramic camera trigger |
| `INCALL_CMD_CUSTOM_KEY_EVENT` | `0x05` (5) | `sendCustomKeyEvent(int, int)` | Steering wheel custom button event |
| `INCALL_CMD_SEND_LOCATION_INFO` | `0x0d` (13) | `sendLocationInfo(String)` | GPS coordinate and heading packet |
| `INCALL_CMD_SEND_WEATHER_TIME_INFO` | `0x0e` (14) | `sendWeatherAndTimeInfo(String)`| Ambient weather & time broadcast |
| `INCALL_CMD_NAVIGATE_STATUS` | `0x16` (22) | `sendNavigateStatus(int)` | 1 = Active guidance, 2 = Arrived, 0 = Idle |
| `INCALL_CMD_NAVIGATE_CROSS_ROAD` | `0x17` (23) | `sendNavigateCrossRoad(int)` | Complex intersection / highway junction view |
| `INCALL_CMD_NAVIGATE_TURN_INFO` | `0x18` (24) | `sendNavigateTurnInfo(int, int)` | Maneuver icon ID and countdown distance (m) |
| `INCALL_CMD_NAVIGATE_LANE_INFO` | `0x19` (25) | `sendNavigateLaneInfo(String)` | Multi-lane recommendation diagram |
| `INCALL_CMD_NAVIGATE_ROAD_INFO` | `0x1a` (26) | `sendNavigateRoadInfo(String, String)` | Next Road name and Current Road name |
| `INCALL_CMD_NAVIGATE_REMAIN_INFO` | `0x1b` (27) | `sendNavigateRemainInfo(int, int)` | Remaining distance (m) and ETA duration (s) |
| `INCALL_CMD_NAVIGATE_CAMERA_INFO` | `0x1c` (28) | `sendNavigateCameraInfo(String)` | Speed camera & radar warnings |
| `INCALL_CMD_REGISTER_NAVIGATE_CALLBACK` | `0x1d` (29) | `registerNavigateCallback(...)` | Register listener for HUD user interactions |
| `INCALL_CMD_UNREGISTER_NAVIGATE_CALLBACK` | `0x1e` (30) | `unRegisterNavigateCallback(...)` | Unregister HUD navigation listener |
| `INCALL_CMD_SEND_AI_SMART_STATUS` | `0x1f` (31) | `sendAISmartStatus(int)` | AI assistant widget state |
| `INCALL_CMD_SEND_AI_SMART_RESULT` | `0x20` (32) | `sendAISmartResult(String)` | AI assistant text/card response |
| `INCALL_CMD_SEND_VOICE_STATUS` | `0x23` (35) | `sendVoiceStatus(String)` | Speech recognition listening state |
| `INCALL_CMD_SEND_VOICE_RESULT` | `0x24` (36) | `sendVoiceResult(String)` | Speech recognition parsed intent |
| `INCALL_CMD_SEND_MEDIA_SOURCE` | `0x25` (37) | `sendMediaSource(String)` | Media app name (e.g. "Bluetooth", "USB", "Kugou") |
| `INCALL_CMD_SEND_MEDIA_PLAY_TIME` | `0x26` (38) | `sendMediaPlayTime(int, int, int)` | Current position, total duration, playback state |
| `INCALL_CMD_SEND_MEDIA_ALBUM` | `0x27` (39) | `sendMediaAlbum(int, String, String)` | Media type, song title, and artist name |
| `INCALL_CMD_SEND_CALL_INFO` | `0x2a` (42) | `sendCallInfo(String)` | Bluetooth caller name / phone number |
| `INCALL_CMD_SEND_CALL_TIME` | `0x2b` (43) | `sendCallTime(int)` | In-call duration in seconds |
| `INCALL_CMD_SEND_CALL_HEAD` | `0x2c` (44) | `sendCallHead(String)` | Contact avatar URI |
| `INCALL_CMD_NAVIGATE_BACK_STATUS` | `0x31` (49) | `sendNavigateBackStatus(int)` | Background navigation indicator |
| `INCALL_CMD_NAVIGATE_PERCENT` | `0x32` (50) | `sendNavigatePercent(int)` | Route completion progress percentage (0-100%) |
| `INCALL_CMD_REQUEST_NAVI_FOCUS` | `0x3f` (63) | `requestNaviFocus(String, IBinder)` | Request AR-HUD graphics & audio priority |
| `INCALL_CMD_ABANDON_NAVI_FOCUS` | `0x40` (64) | `abandonNaviFocus(String, IBinder)` | Release AR-HUD graphics priority |

---

## 4. Usage Example

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

    // 6. Send camera alerts (Transact 0x1c)
    hud.sendNavigateCameraInfo("Speed Camera Ahead 60 km/h")

    // 7. Send route progress percentage (Transact 0x32)
    hud.sendNavigatePercent(45) // 45% of route completed

    // 8. Send media metadata to HUD banner (Transacts 0x25, 0x26, 0x27)
    hud.sendMediaSource("Spotify")
    hud.sendMediaAlbum(sourceType = 1, title = "Blinding Lights", artist = "The Weeknd")
    hud.sendMediaPlayTime(currentSec = 120, totalSec = 200, playStatus = 1)

    // 9. Send active incoming phone call notification to HUD (Transact 0x2a, 0x2c)
    hud.sendCallInfo("John Doe (+1 555-0199)")
    hud.sendCallTime(callDurationSec = 45)

    // 10. Send weather info (Transact 0x0e)
    hud.sendWeatherInfo("{\"temp\": 28, \"condition\": \"Sunny\", \"pm25\": 15}")
}
```
