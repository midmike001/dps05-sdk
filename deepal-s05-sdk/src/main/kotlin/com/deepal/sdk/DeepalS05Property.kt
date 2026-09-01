package com.deepal.sdk

/**
 * Vehicle hardware property IDs, area configurations, and transaction codes
 * for Changan Deepal S05 (Platform Model C857 / EPA OpenOS Platform).
 *
 * Verified against decompiled Changan OpenOS system framework and OEM launcher
 * package `com.deepalhome.launcher` (Deepal+ v26.0521).
 */
object DeepalS05Property {
    const val PLATFORM_ID = "deepal-s05"
    const val PLATFORM_LABEL = "Deepal S05"
    const val INTERNAL_CODE = "C857"

    // Services and Descriptors
    const val VIRTUALCAR_SERVICE = "virtualcar_service"
    const val VIRTUALCAR_PROPERTY_SERVICE = "virtualcar_property_service"
    const val DESCRIPTOR_VIRTUAL_CAR = "com.openos.virtualcar.IVirtualCar"
    const val DESCRIPTOR_PROPERTY = "com.openos.virtualcar.IVirturalCarProperty" // Changan OEM spelling with 'r'

    // Vehicle Specifications & Boundaries
    const val BATTERY_CAPACITY_KWH = 56.12f
    const val TEMP_MIN_C = 17.5f
    const val TEMP_MAX_C = 32.5f

    // Area IDs
    const val AREA_GLOBAL = 0
    const val AREA_DRIVER = 1
    const val AREA_PASSENGER = 4
    const val AREA_SOC = 0x1b              // 27: Battery State of Charge Area

    // Door Area Bitmasks (Property 0x36400311)
    const val AREA_DOOR_FL = 0x01          // Front-Left (Driver) Door
    const val AREA_DOOR_FR = 0x04          // Front-Right (Passenger) Door
    const val AREA_DOOR_RL = 0x10          // Rear-Left Door
    const val AREA_DOOR_RR = 0x40          // Rear-Right Door
    const val AREA_DOORS_ALL = 0x55        // All 4 passenger doors mask (0x01 or 0x04 or 0x10 or 0x40)

    // Window Area Identifiers (Property 0x33400301)
    const val AREA_WINDOW_FL = 0x010       // Front-Left Driver Window
    const val AREA_WINDOW_FR = 0x040       // Front-Right Passenger Window
    const val AREA_WINDOW_RL = 0x100       // Rear-Left Passenger Window
    const val AREA_WINDOW_RR = 0x400       // Rear-Right Passenger Window

    // Tire Area Identifiers (Property 0x37600211)
    const val AREA_TIRE_FL = 0x01          // Front-Left Tire
    const val AREA_TIRE_FR = 0x02          // Front-Right Tire
    const val AREA_TIRE_RL = 0x04          // Rear-Left Tire
    const val AREA_TIRE_RR = 0x08          // Rear-Right Tire

    // Defrost Area Identifiers (Property 0x33400103)
    const val AREA_DEFROST_FRONT = 1       // Front Windshield Defrost
    const val AREA_DEFROST_REAR = 2        // Rear Windshield & Mirror Defrost

    // Vehicle Telemetry Signals (Read)
    const val PROP_BATTERY_SOC = 0x3140028c        // Int: 0-100% (Area: AREA_SOC = 0x1b)
    const val PROP_REMAINING_RANGE = 0x314006c4    // Int: km (Area: 0)
    const val PROP_REMAINING_RANGE_EV_DTE = 0x31400501 // Int: EV DTE (alias vc_alias_e_dte / vc_alias_left_ev_dte, Area: 0)
    const val PROP_REMAINING_RANGE_DISP_DTE = 0x31600205 // Int: Display DTE (alias vc_alias_disp_dte, Area: 0)
    const val PROP_REMAINING_RANGE_RAW = 0x3140028d // Int: Fallback Raw DTE (Area: 0)
    const val PROP_ODOMETER = 0x31600204           // Raw reading in meters, scale divisor = 1000f -> km (Area: 0)
    const val ODOMETER_SCALE_DIVISOR = 1000f
    const val PROP_GEAR_SELECTION = 0x31400231     // Int: 1=P, 2=R, 3=N, 4=D (Area: 0)
    const val PROP_VEHICLE_SPEED_VHAL = 0x11600207 // Standard VHAL Float vehicle speed
    const val PROP_VEHICLE_SPEED_VC = 0x31600202   // OpenOS VirtualCar Float vehicle speed (alias vc_alias_vehicle_speed)
    const val PROP_EXTERIOR_TEMP = 0x35600403      // Float: °C (Area: 0)
    const val PROP_TIRE_PRESSURE = 0x37600211      // Float: Tire pressure in Bar (alias vc_alias_tire_pressure, Areas: 1, 2, 4, 8)
    const val PROP_TIRE_PRESSURE_LEGACY = 0x31410605 // Legacy integer tyre pressure ID

    // Trip & REEV Energy Telemetry (Read)
    const val PROP_THIS_TRIP_ELEC_AVG_CONSUMPTION = 0x314005a6 // Float: kWh/100km (alias vc_alias_this_journey_average_electric_consumption)
    const val PROP_THIS_TRIP_REEV_ELEC_AVG_POWER = 0x314005cf  // Float: Average electric power
    const val PROP_THIS_TRIP_OIL_AVG_CONSUMPTION = 0x314005ce  // Float: L/100km (alias vc_alias_this_journey_average_oil_consumption)
    const val PROP_THIS_TRIP_REEV_ELEC_DISTANCE = 0x31400590   // Float: Electric distance (km)
    const val PROP_THIS_TRIP_REEV_ELEC_TIME = 0x31400591       // Int: Electric driving time (min)
    const val PROP_THIS_TRIP_REEV_FUEL_DISTANCE = 0x314005ae   // Float: Fuel distance (km)
    const val PROP_THIS_TRIP_REEV_FUEL_TIME = 0x314005af       // Int: Fuel driving time (min)
    const val PROP_SUPPLEMENT_REEV_ELEC_AVG_POWER = 0x314005a3 // Float: Recharge elec avg power (fallback 0x31400594 / 0x3140058a)
    const val PROP_SUPPLEMENT_REEV_FUEL_AVG_FUEL = 0x31400592  // Float: Recharge fuel avg fuel
    const val PROP_SUPPLEMENT_REEV_ELEC_DISTANCE = 0x31400595  // Float: Recharge elec distance
    const val PROP_SUPPLEMENT_REEV_ELEC_TIME = 0x31400596      // Int: Recharge elec time
    const val PROP_SUPPLEMENT_REEV_FUEL_DISTANCE = 0x314005b0  // Float: Recharge fuel distance
    const val PROP_SUPPLEMENT_REEV_FUEL_TIME = 0x314005b1      // Int: Recharge fuel time
    const val PROP_EV_RECHARGE_ELEC_DRIVING_POWER = 0x314005a4 // Float: EV recharge elec driving power
    const val PROP_EV_RECHARGE_ELEC_ATTACHMENT_POWER = 0x314005a5 // Float: EV recharge elec attachment power

    // Climate Control Properties (Read/Write)
    const val PROP_HVAC_TEMP_SET = 0x35600105      // Float: 17.5 - 32.5 °C (Area 1: Driver, Area 4: Passenger)
    const val PROP_HVAC_POWER_ON = 0x35400101      // Int: 1=On, 2=Off (Area: 1)
    const val PROP_HVAC_AC_ON = 0x35400102         // Int: 1=On, 2=Off (Area: 1)
    const val PROP_HVAC_AUTO = 0x35400104          // Int: 1=Auto, 2=Manual (Area: 1)
    const val PROP_HVAC_FAN_DIRECTION = 0x35400107 // Int: Air vent blow direction (Area: 1)
    const val PROP_HVAC_RECIRC = 0x35400108        // Int: 2=Recirc, 1=Fresh air (Vendor Tri-State: 2=Recirc, 1=Fresh)
    const val PROP_HVAC_FAN_SPEED = 0x35400109     // Int: 1 - 8 (Area: 1)
    const val PROP_HVAC_GENERATOR = 0x3540010a     // Int: HVAC generator mode (Area: 1)
    const val PROP_HVAC_MAX_AC = 0x3540010b        // Int: 1=On, 2=Off (Area: 1)
    const val PROP_HVAC_DEFROST_REAR = 0x3540010c  // Int: 1=On, 2=Off (Area: 1)
    const val PROP_HVAC_SYNC = 0x3540010d          // Int: 1=Sync, 2=Dual (Area: 1)
    const val PROP_HVAC_DEFROST_FRONT = 0x33400103 // Int: 1=On, 2=Off (Area 1: Front defrost, Area 2: Rear defrost)
    const val PROP_HVAC_INTERNAL_TEMP = 0x38600112 // Float: Cabin internal temperature °C (Area: 1)
    const val PROP_CAR_POWER_ON = 0x31400201       // Int: Vehicle power status (1=On, 2=Off, Area: 1)
    const val PROP_AEB_SWITCH = 0x31400244         // Int: 1=On, 2=Off (Area: 0)
    const val PROP_DRIVE_MODE = 0x3140040d         // Int: 1=ECO, 2=COMFORT, 3=SPORT, 4=CUSTOM (Area: 0)

    // Comfort & Seats (Read/Write)
    const val PROP_SEAT_HEATING = 0x3540010f       // Int: 0=Off, 1=Low, 2=Med, 3=High (Area: 1=Driver, 4=Passenger)
    const val PROP_SEAT_HEATING_CPM = 0x1540050b   // Int: Seat heating via CarPropertyManager (Area: 1=Driver, 4=Passenger)
    const val PROP_SEAT_VENTILATION = 0x35400111   // Int: 0=Off, 1=Low, 2=Med, 3=High (Area: 1=Driver, 4=Passenger)
    const val PROP_SEAT_MASSAGE_TOGGLE = 0x31400b2f // Int: 1=On, 2=Off (Area: 0=Driver, 4=Passenger)
    const val PROP_SEAT_MASSAGE_MODE = 0x31400b31   // Int: Mode 1-3 (Area: 0=Driver, 4=Passenger)
    const val PROP_SEAT_MASSAGE_LEVEL = 0x31400b30  // Int: Level 1-8 (Area: 0=Driver, 4=Passenger)
    const val PROP_STEERING_WHEEL_HEAT = 0x314003eb // Int: 1=On, 2=Off (Area: 0)

    // Windows & Access (Read/Write)
    const val PROP_DOORS = 0x36400311             // Int: Door position / open state (Areas: FL=0x01, FR=0x04, RL=0x10, RR=0x40)
    const val PROP_WINDOW_MOVE = 0x33400301        // Int: 1=Open, 2=Close, 0=Stop (Area: 0x10, 0x40, 0x100, 0x400)
    const val PROP_WINDOW_LOCK = 0x31400303        // Int: 1=Locked, 0=Unlocked (Area: 0)
    const val PROP_SUNROOF_SHADE = 0x31400313      // Int: 1=Open, 2=Close, 3=Vent (Area: 0)
    const val PROP_TAILGATE = 0x31400314           // Int: 1=Open, 2=Close (Area: 0, alias vc_alias_door_trunk_pos)
    const val PROP_DOOR_LOCK = 0x314003eb          // Int: 1=Locked, 2=Unlocked (Area: 0)

    // Lighting & Air Quality
    const val PROP_AMBIENT_LIGHT = 0x3140039a      // Int: Color & Mode (1..6, Area: 0)
    const val PROP_AMBIENT_LIGHT_BRIGHTNESS = 0x3140039b // Int: 0-100% (Area: 0)
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
    const val SPEECH_SETTING_OUTSIDE_SPEAKER = "tinnove_voice_outofcar"

    // Vehicle Settings Service Constants
    const val VEHICLE_SETTINGS_SERVICE = "wt.vehiclesetting"
    const val VEHICLE_SETTINGS_DESCRIPTOR = "com.openos.settings.vehiclesettings.IVehicleSettingInterface"

    // Changan InCall OEM Navigation / AR-HUD Interconnect
    const val INCALL_SVR_MNG_SERVICE = "com.incall.SVR_MNG_SERVICE"
    const val INCALL_DOUBLE_INTERACTIVE_SERVICE = "com.incall.double.INTERACTIVE_SERVICE"
    const val INCALL_DESCRIPTOR_SVR_MANAGER = "com.incall.serversdk.server.ISvrManager"
    const val INCALL_DESCRIPTOR_INTERACTIVE_MANAGER = "com.incall.serversdk.interactive.IDouInteractiveManager"

    // InCall Transaction Codes (com.incall.serversdk.interactive.IDouInteractiveManager)
    const val INCALL_CMD_SWITCH_LVDS_FINISH = 0x01   // sendSwitchLVDSFinishEvent(int state)
    const val INCALL_CMD_CONTRA_NAVIGATE_EVENT = 0x02// sendContraNavigateEvent(int event)
    const val INCALL_CMD_GET_LOG_EVENT = 0x03        // sendGetLogEvent()
    const val INCALL_CMD_360_TRIG_EVENT = 0x04       // send360trigEvent(int event)
    const val INCALL_CMD_CUSTOM_KEY_EVENT = 0x05     // sendCustomKeyEvent(int keyCode, int keyAction)
    const val INCALL_CMD_SEND_LOCATION_INFO = 0x0d   // sendLocationInfo(String json)
    const val INCALL_CMD_SEND_WEATHER_TIME_INFO = 0x0e // sendWeatherAndTimeInfo(String json)
    const val INCALL_CMD_NAVIGATE_STATUS = 0x16      // sendNavigateStatus(int status: 1=Navigating, 2=Arrived, 0=Cleared)
    const val INCALL_CMD_NAVIGATE_CROSS_ROAD = 0x17  // sendNavigateCrossRoad(int crossRoad)
    const val INCALL_CMD_NAVIGATE_TURN_INFO = 0x18   // sendNavigateTurnInfo(int turnIcon, int turnDistMeters)
    const val INCALL_CMD_NAVIGATE_LANE_INFO = 0x19   // sendNavigateLaneInfo(String laneInfo)
    const val INCALL_CMD_NAVIGATE_ROAD_INFO = 0x1a   // sendNavigateRoadInfo(String nextRoad, String curRoad)
    const val INCALL_CMD_NAVIGATE_REMAIN_INFO = 0x1b // sendNavigateRemainInfo(int remainDistMeters, int remainTimeSec)
    const val INCALL_CMD_NAVIGATE_CAMERA_INFO = 0x1c // sendNavigateCameraInfo(String cameraInfo)
    const val INCALL_CMD_REGISTER_NAVIGATE_CALLBACK = 0x1d // registerNavigateCallback(INavigateCallback)
    const val INCALL_CMD_UNREGISTER_NAVIGATE_CALLBACK = 0x1e // unRegisterNavigateCallback(INavigateCallback)
    const val INCALL_CMD_SEND_AI_SMART_STATUS = 0x1f // sendAISmartStatus(int status)
    const val INCALL_CMD_SEND_AI_SMART_RESULT = 0x20 // sendAISmartResult(String result)
    const val INCALL_CMD_SEND_VOICE_STATUS = 0x23    // sendVoiceStatus(String status)
    const val INCALL_CMD_SEND_VOICE_RESULT = 0x24    // sendVoiceResult(String result)
    const val INCALL_CMD_SEND_MEDIA_SOURCE = 0x25    // sendMediaSource(String source)
    const val INCALL_CMD_SEND_MEDIA_PLAY_TIME = 0x26 // sendMediaPlayTime(int curTime, int totalTime, int state)
    const val INCALL_CMD_SEND_MEDIA_ALBUM = 0x27     // sendMediaAlbum(int type, String title, String artist)
    const val INCALL_CMD_REGISTER_MEDIA_CALLBACK = 0x28 // registerMediaCallback(IMediaCallback)
    const val INCALL_CMD_UNREGISTER_MEDIA_CALLBACK = 0x29 // unRegisterMediaCallback(IMediaCallback)
    const val INCALL_CMD_SEND_CALL_INFO = 0x2a       // sendCallInfo(String callInfo)
    const val INCALL_CMD_SEND_CALL_TIME = 0x2b       // sendCallTime(int seconds)
    const val INCALL_CMD_SEND_CALL_HEAD = 0x2c       // sendCallHead(String avatarUrl)
    const val INCALL_CMD_NAVIGATE_BACK_STATUS = 0x31 // sendNavigateBackStatus(int status)
    const val INCALL_CMD_NAVIGATE_PERCENT = 0x32     // sendNavigatePercent(int percent)
    const val INCALL_CMD_REQUEST_NAVI_FOCUS = 0x3f   // requestNaviFocus(String pkg, INaviFocusCallback)
    const val INCALL_CMD_ABANDON_NAVI_FOCUS = 0x40   // abandonNaviFocus(String pkg, INaviFocusCallback)
}
