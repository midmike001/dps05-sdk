package com.deepal.sdk

/**
 * Hardware, VHAL, and CAN property definitions for the Changan Deepal S05 (Platform C857 / EPA OpenOS).
 *
 * Reverse-engineered and verified against ground truth Changan OpenOS system framework,
 * G2/E0 built-in profile bytecode (DEEPAL_S05_C857 / DEEPAL_S05_CABIN_WRITES),
 * and low-level Binder IPC service contracts.
 */
object DeepalS05Property {

    // OpenOS VirtualCar System Service Identifiers
    const val VIRTUALCAR_SERVICE = "virtualcar_service"
    const val VIRTUALCAR_PROPERTY_SERVICE = "virtualcar_property_service"
    const val DESCRIPTOR_VIRTUAL_CAR = "com.openos.virtualcar.IVirtualCar"
    const val DESCRIPTOR_VIRTUAL_CAR_PROPERTY = "com.openos.virtualcar.IVirturalCarProperty"
    const val DESCRIPTOR_VIRTUAL_CAR_LISTENER = "com.openos.virtualcar.IVirtualCarPropertyEventListener"

    // Vehicle Settings Service (wt.vehiclesetting) Identifiers & Transacts
    const val VEHICLE_SETTING_SERVICE = "wt.vehiclesetting"
    const val DESCRIPTOR_VEHICLE_SETTING = "com.openos.settings.vehiclesettings.IVehicleSettingInterface"
    const val TRANSACT_GET_SUNSHADE_POS = 0x3f              // 63: getSunshadePos() -> int (0..100)
    const val TRANSACT_SET_SUNSHADE_POS = 0x40              // 64: setSunshadePos(int pos) (0..100)
    const val TRANSACT_GET_SUNSHADE_MOVE_STATUS = 0x41      // 65: getSunshadeMoveStatus() -> int
    const val TRANSACT_GET_SUNROOF_POS = 0x3a               // 58: getSunroofPos() -> int (0..100)
    const val TRANSACT_SET_SUNROOF_POS = 0x3b               // 59: setSunroofPos(int pos) (0..100)
    const val TRANSACT_SET_SUNROOF_TILT = 0x3c              // 60: setSunroofTiltStatus(int tilt)
    const val TRANSACT_GET_SUNROOF_MOVE_STATUS = 0x3d       // 61: getSunroofMoveStatus() -> int
    const val TRANSACT_GET_SUNROOF_RAIN_DETECT = 0x42       // 66: getSunroofrainDetectcloseSw() -> boolean
    const val TRANSACT_SET_SUNROOF_RAIN_DETECT = 0x43       // 67: setSunroofrainDetectcloseSw(boolean)
    const val TRANSACT_GET_SMART_WELCOME_UNLOCK = 0x4c      // 76: getSmartWelcomeUnlockSw() -> boolean
    const val TRANSACT_SET_SMART_WELCOME_UNLOCK = 0x4d      // 77: setSmartWelcomeUnlockSw(boolean)
    const val TRANSACT_GET_SMART_LEAVING_LOCK = 0x4e        // 78: getSmartLeavingLockSw() -> boolean
    const val TRANSACT_SET_SMART_LEAVING_LOCK = 0x4f        // 79: setSmartLeavingLockSw(boolean)
    const val TRANSACT_GET_SMART_TRUNK_UNLOCK = 0x50        // 80: getSmartTrunkulockSw() -> boolean
    const val TRANSACT_SET_SMART_TRUNK_UNLOCK = 0x51        // 81: setSmartTrunkulockSw(boolean)
    const val TRANSACT_GET_MIRROR_AUTOFOLD = 0x58           // 88: getMirrorAutofoldSw() -> boolean
    const val TRANSACT_SET_MIRROR_AUTOFOLD = 0x59           // 89: setMirrorAutofoldSw(boolean)
    const val TRANSACT_GET_WIRELESS_CHARGE = 0x5a           // 90: getWirelessChargeSw() -> boolean
    const val TRANSACT_SET_WIRELESS_CHARGE = 0x5b           // 91: setWirelessChargeSw(boolean)
    const val TRANSACT_GET_HUD_SWITCH = 0x82                // 130: getHudSwitchStatus() -> boolean
    const val TRANSACT_SET_HUD_SWITCH = 0x83                // 131: setHudSwitchStatus(boolean)
    const val TRANSACT_GET_HUD_BRIGHT = 0x84                // 132: getHudBright() -> int
    const val TRANSACT_SET_HUD_BRIGHT = 0x85                // 133: setHudBright(int)
    const val TRANSACT_GET_HUD_HEIGHT = 0x86                // 134: getHudHeight() -> int
    const val TRANSACT_SET_HUD_HEIGHT = 0x87                // 135: setHudHeight(int)
    const val TRANSACT_GET_HUD_DISPLAY_PHONE = 0x8e         // 142: getHudDisplayPhone() -> boolean
    const val TRANSACT_SET_HUD_DISPLAY_PHONE = 0x8f         // 143: setHudDisplayPhone(boolean)
    const val TRANSACT_GET_HUD_DISPLAY_NAV = 0x90           // 144: getHudDisplayNav() -> boolean
    const val TRANSACT_SET_HUD_DISPLAY_NAV = 0x91           // 145: setHudDisplayNav(boolean)

    // Area IDs
    const val AREA_GLOBAL = 0
    const val AREA_DRIVER = 1          // Left zone / Driver
    const val AREA_PASSENGER = 4       // Right zone / Front Passenger
    const val AREA_SOC = 0x1b          // 27: Battery State of Charge Area

    // Door Bitmasks (PROP_DOORS = 0x36400311)
    const val AREA_DOOR_FL = 0x01      // Front-Left (Driver) Door
    const val AREA_DOOR_FR = 0x04      // Front-Right (Passenger) Door
    const val AREA_DOOR_RL = 0x10      // Rear-Left Door
    const val AREA_DOOR_RR = 0x40      // Rear-Right Door

    // Window Area Constants (PROP_WINDOW_MOVE = 0x33400301 / PROP_WINDOW_POS = 0x33400300)
    const val AREA_WINDOW_FL = 0x010   // 16: Front-Left Window
    const val AREA_WINDOW_FR = 0x040   // 64: Front-Right Window
    const val AREA_WINDOW_RL = 0x100   // 256: Rear-Left Window
    const val AREA_WINDOW_RR = 0x400   // 1024: Rear-Right Window

    // Tire Area Constants (PROP_TIRE_PRESSURE = 0x37600211)
    const val AREA_TIRE_FL = 0x01      // Front-Left Tire
    const val AREA_TIRE_FR = 0x02      // Front-Right Tire
    const val AREA_TIRE_RL = 0x04      // Rear-Left Tire
    const val AREA_TIRE_RR = 0x08      // Rear-Right Tire

    // Powertrain & Battery Properties (Read)
    const val PROP_BATTERY_SOC = 0x3140028c        // Int: 0 - 100% (Area: 0x1b / 27)
    const val PROP_REMAINING_RANGE_C857 =
        0x314006c4 // Int: Deepal S05 C857 remaining range (km, Area: 0, BuiltInProfiles.DEEPAL_S05_C857)
    const val PROP_REMAINING_RANGE_EV_DTE =
        0x31400501 // Int: EV pure electric DTE (km, Area: 0, alias vc_alias_left_ev_dte)
    const val PROP_REMAINING_RANGE_DISP_DTE =
        0x31600205 // Int: Displayed remaining DTE (km, Area: 0, alias vc_alias_disp_dte)
    const val PROP_REMAINING_RANGE_RAW = 0x3140028d // Int: Fallback Raw DTE (Area: 0)
    const val PROP_ODOMETER = 0x31600204           // Raw reading in meters, scale divisor = 1000f -> km (Area: 0)
    const val ODOMETER_SCALE_DIVISOR = 1000f
    const val PROP_GEAR_SELECTION = 0x31400231     // Int: 0/4=P, 1=N, 2=R, 3/8=D (Area: 0, alias vc_alias_vehicle_gear)
    const val PROP_GEAR_SELECTION_VHAL = 0x11400400 // Standard Android VHAL Gear Selection ID
    const val GEAR_RAW_PARK = 4                    // Raw gear P code (or 0)
    const val GEAR_RAW_NEUTRAL = 1                 // Raw gear N code
    const val GEAR_RAW_REVERSE = 2                 // Raw gear R code
    const val GEAR_RAW_DRIVE = 3                   // Raw gear D code (or 8)
    const val PROP_VEHICLE_SPEED_VHAL = 0x11600207 // Standard VHAL Float vehicle speed
    const val PROP_VEHICLE_SPEED_VC =
        0x31600202   // OpenOS VirtualCar Float vehicle speed (alias vc_alias_vehicle_speed)
    const val PROP_EXTERIOR_TEMP = 0x35600403      // Float: °C (Area: 0)
    const val PROP_TIRE_PRESSURE =
        0x37600211      // Float: Tire pressure in Bar (alias vc_alias_tire_pressure, Areas: 1, 2, 4, 8)
    const val PROP_TIRE_PRESSURE_LEGACY = 0x31410605 // Legacy integer tyre pressure ID (scale canonical 4 divisor 3)

    // Hardware CAN Bus Signal Definition Properties for Deepal S05 C857
    const val PROP_CAN_SPEED_GEAR = 0x21410605     // CAN Speed/Gear signal ID in DEEPAL_S05_C857 config
    const val PROP_CAN_STEERING = 0x31600204       // CAN Steering/Odometer signal ID in DEEPAL_S05_C857 config
    const val PROP_CAN_HVAC_TPMS = 0x31410605      // CAN HVAC/TPMS signal ID in DEEPAL_S05_C857 config

    // Trip & REEV Energy Telemetry (Read)
    const val PROP_THIS_TRIP_ELEC_AVG_CONSUMPTION =
        0x314005a6 // Float: kWh/100km (alias vc_alias_this_journey_average_electric_consumption)
    const val PROP_THIS_TRIP_REEV_ELEC_AVG_POWER = 0x314005cf  // Float: Average electric power
    const val PROP_THIS_TRIP_OIL_AVG_CONSUMPTION =
        0x314005ce  // Float: L/100km (alias vc_alias_this_journey_average_oil_consumption)
    const val PROP_THIS_TRIP_REEV_ELEC_DISTANCE = 0x31400590   // Float: Electric distance (km)
    const val PROP_THIS_TRIP_REEV_ELEC_TIME = 0x31400591       // Int: Electric driving time (min)
    const val PROP_THIS_TRIP_REEV_FUEL_DISTANCE = 0x314005ae   // Float: Fuel distance (km)
    const val PROP_THIS_TRIP_REEV_FUEL_TIME = 0x314005af       // Int: Fuel driving time (min)
    const val PROP_SUPPLEMENT_REEV_ELEC_AVG_POWER =
        0x314005a3 // Float: Recharge elec avg power (fallback 0x31400594 / 0x3140058a)
    const val PROP_SUPPLEMENT_REEV_FUEL_AVG_FUEL = 0x31400592  // Float: Recharge fuel avg fuel
    const val PROP_SUPPLEMENT_REEV_ELEC_DISTANCE = 0x31400595  // Float: Recharge elec distance
    const val PROP_SUPPLEMENT_REEV_ELEC_TIME = 0x31400596      // Int: Recharge elec time
    const val PROP_SUPPLEMENT_REEV_FUEL_DISTANCE = 0x314005b0  // Float: Recharge fuel distance
    const val PROP_SUPPLEMENT_REEV_FUEL_TIME = 0x314005b1      // Int: Recharge fuel time
    const val PROP_EV_RECHARGE_ELEC_DRIVING_POWER = 0x314005a4 // Float: EV recharge elec driving power
    const val PROP_EV_RECHARGE_ELEC_ATTACHMENT_POWER = 0x314005a5 // Float: EV recharge elec attachment power

    // Climate Control Properties (Read/Write)
    const val PROP_HVAC_TEMP_SET = 0x35600105      // Float: 17.5 - 32.5 °C (Area 1: Driver, Area 4: Passenger)
    const val PROP_HVAC_POWER_ON = 0x35400101      // Int: 2=On, 1=Off (Area: 1)
    const val PROP_HVAC_AC_ON = 0x35400102         // Int: 1=On, 2=Off (Area: 1)
    const val PROP_HVAC_AUTO = 0x35400104          // Int: 1=Auto, 2=Manual (Area: 1 or 2)
    const val PROP_HVAC_FAN_DIRECTION = 0x35400107 // Int: Air vent blow direction (Area: 1)
    const val WIND_DIRECTION_DEFROST = 8           // Windshield defrost vents
    const val WIND_DIRECTION_FACE = 9              // Face blower vents
    const val WIND_DIRECTION_FEET = 10             // Floor feet vents
    const val WIND_DIRECTION_FACE_FEET = 11        // Face + Feet dual vents
    const val PROP_HVAC_RECIRC = 0x35400108        // Int: 2=Recirc, 1=Fresh air (Vendor Tri-State: 2=Recirc, 1=Fresh)
    const val PROP_HVAC_FAN_SPEED = 0x35400109     // Int: 1 - 8 (Area: 1)
    const val PROP_HVAC_GENERATOR = 0x3540010a     // Int: HVAC generator mode (Area: 1)
    const val PROP_HVAC_MAX_AC = 0x3540010b        // Int: 1=On, 2=Off (Area: 1 or 2)
    const val PROP_HVAC_DEFROST_REAR = 0x3540010c  // Int: 1=On, 2=Off (Area: 1)
    const val PROP_HVAC_SYNC = 0x3540010d          // Int: 1=Sync, 2=Dual (Area: 1)
    const val PROP_HVAC_DEFROST_FRONT = 0x33400103 // Int: 1=On, 2=Off (Area 1: Front defrost, Area 2: Rear defrost)
    const val PROP_HVAC_INTERNAL_TEMP = 0x38600112 // Float: Cabin internal temperature °C (Area: 1)
    const val PROP_CAR_POWER_ON = 0x31400201       // Int: Vehicle power status (1=On, 2=Off, Area: 1)

    // Driving Mode & ADAS Assistance
    const val PROP_DRIVE_MODE =
        0x3140040d         // Int: 1=COMFORT, 2=SPORT, 3=ECO, 4=CUSTOM (Area: 0, alias vc_alias_drive_style)
    const val PROP_DRIVE_MODE_CHOICE =
        0x314003f5  // Int: Choice write (1=Comfort, 2=Sport, 3=Eco) in DEEPAL_S05_CABIN_WRITES
    val DRIVE_MODE_CHOICES = setOf(1, 2, 3)
    const val DRIVE_MODE_COMFORT = 1
    const val DRIVE_MODE_SPORT = 2
    const val DRIVE_MODE_ECO = 3
    const val DRIVE_MODE_CUSTOM = 4

    const val PROP_AEB_COMMAND =
        0x3140040d        // CabinCommandWrite for AEB: onCommand=2, offCommand=1, onState=2, offState=1
    const val PROP_AEB_SWITCH = 0x31400244         // Int: 1=On, 2=Off (Area: 0)

    // Comfort & Seats (Read/Write)
    const val PROP_SEAT_HEATING = 0x3540010f       // Int: 0=Off, 1=Low, 2=Med, 3=High (Area: 1=Driver, 4=Passenger)
    const val PROP_SEAT_HEATING_CPM =
        0x1540050b   // Int: Seat heating via CarPropertyManager (Area: 1=Driver, 4=Passenger)
    const val PROP_SEAT_VENTILATION = 0x35400111   // Int: 0=Off, 1=Low, 2=Med, 3=High (Area: 1=Driver, 4=Passenger)
    const val PROP_SEAT_MASSAGE_TOGGLE = 0x31400b2f // Int: 2=On, 1=Off (Area: 0=Driver, 4=Passenger)
    const val PROP_SEAT_MASSAGE_MODE = 0x31400b30   // Int: Mode/Pattern 1-8 (Area: 0=Driver, 4=Passenger
    const val PROP_SEAT_MASSAGE_LEVEL = 0x31400b31  // Int: Intensity Level 1-3 (Area: 0=Driver, 4=Passenger
    const val PROP_SEAT_MASSAGE_PATTERN = 0x31400b30 // Alias for massage pattern/mode (1..8)
    const val PROP_SEAT_MASSAGE_INTENSITY = 0x31400b31 // Alias for massage intensity level (1..3)
    const val PROP_STEERING_WHEEL_HEAT = 0x314003eb // Int: 1=On, 2=Off (Area: 0)

    // Windows & Access (Read/Write)
    const val PROP_DOORS =
        0x36400311             // Int: Door position / open state (Areas: FL=0x01, FR=0x04, RL=0x10, RR=0x40)
    const val PROP_WINDOW_POS =
        0x33400300        // Int: 0..100% Window Position (0=Closed, 100=Fully Open, Areas: 0x10, 0x40, 0x100, 0x400)
    const val PROP_WINDOW_POS_VC = 0x31400300     // Int: VirtualCar Window Position fallback
    const val PROP_WINDOW_MOVE = 0x33400301       // Int: Rate/Direction (-100=Express Close, 100=Express Open, 0=Stop)
    const val PROP_WINDOW_LOCK = 0x31400303       // Int: 1=Locked, 0=Unlocked (Area: 0)
    const val PROP_SUNSHADE_POS_VC = 0x31400303   // Int: Sunshade position write in DEEPAL_S05_CABIN_WRITES (Area: 0)
    const val PROP_TAILGATE_CONTROL = 0x31400313  // Int: Tailgate Actuation Command (2=Open, 1=Close, Area: 0)
    const val PROP_TAILGATE_STATUS =
        0x31400314   // Int: Tailgate Position Status (1=Open, 2=Closed / 0=Closed, Area: 0, alias vc_alias_door_trunk_pos)
    const val PROP_TAILGATE = 0x31400314          // Backwards-compatible alias for tailgate status
    const val PROP_DOOR_LOCK = 0x314003eb         // Int: 2=Locked, 1=Unlocked (Area: 0)

    // Lighting & Air Quality
    const val PROP_AMBIENT_LIGHT = 0x3140039a      // Int: Color & Toggle Command (1=On, 0=Off, Area: 0)
    const val PROP_AMBIENT_LIGHT_BRIGHTNESS = 0x3140039b // Int: 0-100% (Area: 0)
    const val PROP_AMBIENT_LIGHT_COLOR_CHOICE = 0x3140039b // Int: Color choices in CabinChoiceWrite (Area: 1)
    val AMBIENT_COLOR_CHOICES = setOf(54, 42, 33, 12, 6, 1) // Verified ambient color choice values
    const val PROP_AMBIENT_LIGHT_PATTERN = 0x31400677 // Int: Dynamic lighting effect pattern (1..3, Area: 0)
    const val PROP_AIR_PURIFIER = 0x35400122        // Int: 1=On, 2=Off (Area: 0)

    // Intelligent EV & Body Automation
    const val PROP_BATTERY_PRECONDITIONING = 0x314006c6 // Int: 1=Preheating for Fast Charging, 2=Off (Area: 0)
    const val PROP_RAIN_SENSOR_STATE = 0x31400277       // Int: 1=No Rain, 2=Light Rain, 3=Heavy Rain (Area: 0)

    // Audio & Outside Speaker Event ID
    const val AUDIO_EVENT_OUTSIDE_MUSIC = 0x66     // Int: 102 (CarAudioManager event for outside speaker music)

    // Speech & Voice Assistant Service Constants
    const val SPEECH_SERVICE_PACKAGE = "com.tinnove.wecarspeech"
    const val SPEECH_SERVICE_PACKAGE_FALLBACK = "com.wt.speechserver"
    const val SPEECH_SERVICE_CLASS = "com.tinnove.vrlogic.server.VrLogicService"
    const val SPEECH_DESCRIPTOR = "com.tinnove.vrinterface.IVrLogicService"
    const val SPEECH_TRANSACT_PLAY_TTS = 0x1b      // 27: In-cabin TTS playback
    const val SPEECH_TRANSACT_PLAY_OUTSIDE_TTS = 0x62 // 98: Outside vehicle TTS speaker broadcast
    const val SPEECH_TRANSACT_CLEAR_OUTSIDE_TTS = 0x60 // 96: Clear / stop outside speaker speech
    const val SPEECH_SETTING_OUTSIDE_SPEAKER = "tinnove_voice_outofcar" // Global Settings key (1=enabled, 0=disabled)

    // InCall Multi-Screen & HUD Interactive Manager Identifiers
    const val INCALL_SVR_MNG_SERVICE = "com.incall.SVR_MNG_SERVICE"
    const val INCALL_DOUBLE_INTERACTIVE_SERVICE = "com.incall.double.INTERACTIVE_SERVICE"
    const val INCALL_DESCRIPTOR = "com.incall.serversdk.interactive.IDouInteractiveManager"
    const val INCALL_DESCRIPTOR_SVR_MANAGER = "com.incall.serversdk.servermanager.IServerManager"
    const val INCALL_DESCRIPTOR_INTERACTIVE_MANAGER = "com.incall.serversdk.interactive.IDouInteractiveManager"
    const val INCALL_TRANSACT_CODE = 1

    // InCall Function Codes (mCode in transact IPC)
    const val INCALL_CMD_LOCATION_INFO = 0x04          // 4: Vehicle GPS location string
    const val INCALL_CMD_WEATHER_TIME_INFO = 0x0e      // 14: Weather & time metadata string
    const val INCALL_CMD_NAVIGATE_STATUS = 0x16        // 22: Navigation state (1=Active guidance, 2=Arrived, 0=Idle)
    const val INCALL_CMD_CROSS_ROAD = 0x17             // 23: Cross road junction view status
    const val INCALL_CMD_NAVIGATE_CROSS_ROAD = 0x17
    const val INCALL_CMD_TURN_INFO = 0x18              // 24: Turn maneuver icon ID & countdown distance (meters)
    const val INCALL_CMD_NAVIGATE_TURN_INFO = 0x18
    const val INCALL_CMD_LANE_INFO = 0x19              // 25: Recommended lane guidance string
    const val INCALL_CMD_NAVIGATE_LANE_INFO = 0x19
    const val INCALL_CMD_ROAD_INFO = 0x1a              // 26: Next road name & Current road name strings
    const val INCALL_CMD_NAVIGATE_ROAD_INFO = 0x1a
    const val INCALL_CMD_REMAIN_INFO = 0x1b            // 27: Remaining trip distance (m) & ETA (seconds)
    const val INCALL_CMD_NAVIGATE_REMAIN_INFO = 0x1b
    const val INCALL_CMD_CAMERA_INFO = 0x1c            // 28: Speed camera & radar alerts string
    const val INCALL_CMD_NAVIGATE_CAMERA_INFO = 0x1c
    const val INCALL_CMD_AI_SMART_STATUS = 0x1f        // 31: AI driving status
    const val INCALL_CMD_AI_SMART_SCENE = 0x20         // 32: AI smart driving scene
    const val INCALL_CMD_VR_STATUS = 0x23              // 35: Voice recognition state
    const val INCALL_CMD_VR_RESULT = 0x24              // 36: Voice recognition parsed result string
    const val INCALL_CMD_MEDIA_SOURCE = 0x25           // 37: Media playback source
    const val INCALL_CMD_MEDIA_TIME = 0x26             // 38: Media track duration & elapsed time
    const val INCALL_CMD_MEDIA_ALBUM_ICON = 0x27       // 39: Media cover album icon
    const val INCALL_CMD_CALL_INFO = 0x2a              // 42: Incoming/outgoing phone call metadata
    const val INCALL_CMD_CALL_TIME = 0x2b              // 43: Call duration timer
    const val INCALL_CMD_CALL_AVATAR = 0x2c            // 44: Contact caller avatar icon
    const val INCALL_CMD_BACK_STATUS = 0x31            // 49: Background status
    const val INCALL_CMD_PERCENT = 0x32                // 50: Progress percentage
    const val INCALL_CMD_REQUEST_FOCUS = 0x3f          // 63: Request HUD graphics focus with callback
    const val INCALL_CMD_REQUEST_NAVI_FOCUS = 0x3f
    const val INCALL_CMD_ABANDON_FOCUS = 0x40          // 64: Release HUD graphics focus with callback
    const val INCALL_CMD_ABANDON_NAVI_FOCUS = 0x40
}
