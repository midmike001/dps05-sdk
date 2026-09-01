package com.deepal.sdk

/**
 * Vehicle hardware property IDs, area configurations, and transaction codes
 * for Changan Deepal S05 (Platform Model C857 / EPA OpenOS Platform).
 */
object DeepalS05Property {
    const val PLATFORM_ID = "deepal-s05"
    const val PLATFORM_LABEL = "Deepal S05"
    const val INTERNAL_CODE = "C857"

    // Services and Descriptors
    const val VIRTUALCAR_SERVICE = "virtualcar_service"
    const val VIRTUALCAR_PROPERTY_SERVICE = "virtualcar_property_service"
    const val DESCRIPTOR_VIRTUAL_CAR = "com.openos.virtualcar.IVirtualCar"
    const val DESCRIPTOR_PROPERTY = "com.openos.virtualcar.IVirturalCarProperty" // Changan OEM spelling

    // Vehicle Specifications & Boundaries
    const val BATTERY_CAPACITY_KWH = 56.12f
    const val TEMP_MIN_C = 17.5f           
    const val TEMP_MAX_C = 32.5f

    // Area IDs
    const val AREA_GLOBAL = 0
    const val AREA_DRIVER = 1
    const val AREA_PASSENGER = 4
    const val AREA_SOC = 0x1b              // 27: Battery State of Charge 
    const val AREA_DOORS_ALL = 0x0F
    const val AREA_WINDOW_FL = 0x010       // Front-Left Driver Window
    const val AREA_WINDOW_FR = 0x040       // Front-Right Passenger Window
    const val AREA_WINDOW_RL = 0x100       // Rear-Left Passenger Window 
    const val AREA_WINDOW_RR = 0x400       // Rear-Right Passenger Window

    // Vehicle Telemetry Signals (Read) - 
    const val PROP_BATTERY_SOC = 0x3140028c        // Int: 0-100% (Area: AREA_SOC = 0x1b)
    const val PROP_REMAINING_RANGE = 0x314006c4    // Int: km (Area: 0)
    const val PROP_ODOMETER = 0x31600204           // Raw reading in meters, scale divisor = 1000f -> km (Area: 0)
    const val ODOMETER_SCALE_DIVISOR = 1000f
    const val PROP_TIRE_PRESSURE = 0x31410605      // Tyre pressure (Scale: 4, Divisor: 3)
    const val PROP_GEAR_SELECTION = 0x31400231     // Int: 1=P, 2=R, 3=N, 4=D (Area: 0)
    const val PROP_VEHICLE_SPEED_VHAL = 0x11600207 // Standard VHAL Float vehicle speed
    const val PROP_EXTERIOR_TEMP = 0x35600403      // Float: °C

    // Climate Control Properties (Read/Write) 
    const val PROP_HVAC_TEMP_SET = 0x35600105      // Float: 17.5 - 32.5 °C (Area 1: Driver, Area 4: Passenger)
    const val PROP_HVAC_POWER_ON = 0x35400101      // Int: 1=On, 2=Off (Area: 1)
    const val PROP_HVAC_AC_ON = 0x35400102         // Int: 1=On, 2=Off (Area: 1)
    const val PROP_HVAC_AUTO = 0x35400104          // Int: 1=Auto, 2=Manual (Area: 1)
    const val PROP_HVAC_RECIRC = 0x35400108        // Int: 2=Recirc, 1=Fresh air (Vendor Tri-State Inverted)
    const val PROP_HVAC_FAN_SPEED = 0x35400109     // Int: 1 - 8 (Area: 1)
    const val PROP_HVAC_MAX_AC = 0x3540010b        // Int: 1=On, 2=Off (Area: 1)
    const val PROP_HVAC_SYNC = 0x3540010d          // Int: 1=Sync, 2=Dual (Area: 1)
    const val PROP_HVAC_DEFROST_FRONT = 0x33400103 // Int: 1=On, 2=Off (Area: 1)
    const val PROP_HVAC_DEFROST_REAR = 0x3540010c  // Int: 1=On, 2=Off (Area: 1)
    const val PROP_AEB_SWITCH = 0x31400244         // Int: 1=On, 2=Off (Area: 0)
    const val PROP_DRIVE_MODE = 0x3140040d         // Int: 1=ECO, 2=COMFORT, 3=SPORT (Area: 0)

    // Comfort & Seats (Read/Write) 
    const val PROP_SEAT_HEATING = 0x3540010f       // Int: 0=Off, 1=Low, 2=Med, 3=High (Area: 1=Driver, 4=Passenger)
    const val PROP_SEAT_VENTILATION = 0x35400111   // Int: 0=Off, 1=Low, 2=Med, 3=High (Area: 1=Driver, 4=Passenger)
    const val PROP_SEAT_MASSAGE_TOGGLE = 0x31400b2f // Int: 1=On, 2=Off (Area: 0=Driver, 4=Passenger)
    const val PROP_SEAT_MASSAGE_MODE = 0x31400b31   // Int: Mode 1-3 (Area: 0=Driver, 4=Passenger)
    const val PROP_SEAT_MASSAGE_LEVEL = 0x31400b30  // Int: Level 1-8 (Area: 0=Driver, 4=Passenger)
    const val PROP_STEERING_WHEEL_HEAT = 0x314003eb // Int: 1=On, 2=Off (Area: 0)

    // Windows & Access (Read/Write)
    const val PROP_WINDOW_MOVE = 0x33400301        // Int: 1=Open, 2=Close, 0=Stop (Area: 0x10, 0x40, 0x100, 0x400)
    const val PROP_WINDOW_LOCK = 0x31400303        // Int: 1=Locked, 0=Unlocked (Area: 0)
    const val PROP_SUNROOF_SHADE = 0x31400313      // Int: 1=Open, 2=Close, 3=Vent (Area: 0)
    const val PROP_TAILGATE = 0x3140040d           // Int: 1=Open, 2=Close (Area: 0)
    const val PROP_DOOR_LOCK = 0x314003eb          // Int: 1=Locked, 2=Unlocked (Area: 0)

    // Lighting & Air Quality
    const val PROP_AMBIENT_LIGHT = 0x3140039a      // Int: Color & Mode (1..6, Area: 0)
    const val PROP_AMBIENT_LIGHT_BRIGHTNESS = 0x3140039b // Int: 0-100% (Area: 0)
    const val PROP_AIR_PURIFIER = 0x35400122        // Int: 1=On, 2=Off (Area: 0)

    // Intelligent EV & Body Automation
    const val PROP_BATTERY_PRECONDITIONING = 0x314006c6 // Int: 1=Preheating for Fast Charging, 2=Off (Area: 0)
    const val PROP_RAIN_SENSOR_STATE = 0x31400277       // Int: 1=No Rain, 2=Light Rain, 3=Heavy Rain (Area: 0)

    // Changan InCall OEM Navigation / AR-HUD Interconnect
    const val INCALL_SVR_MNG_SERVICE = "com.incall.SVR_MNG_SERVICE"
    const val INCALL_DOUBLE_INTERACTIVE_SERVICE = "com.incall.double.INTERACTIVE_SERVICE"
    const val INCALL_DESCRIPTOR_SVR_MANAGER = "com.incall.serversdk.server.ISvrManager"
    const val INCALL_DESCRIPTOR_INTERACTIVE_MANAGER = "com.incall.serversdk.interactive.IDouInteractiveManager"

    // InCall Transaction Codes
    const val INCALL_CMD_NAVIGATE_STATUS = 0x16      // sendNavigateStatus(int status)
    const val INCALL_CMD_NAVIGATE_TURN_INFO = 0x18   // sendNavigateTurnInfo(int turnIcon, int turnDist)
    const val INCALL_CMD_NAVIGATE_ROAD_INFO = 0x1a   // sendNavigateRoadInfo(String nextRoad, String curRoad)
    const val INCALL_CMD_NAVIGATE_REMAIN_INFO = 0x1b // sendNavigateRemainInfo(int remainDist, int remainTime)
    const val INCALL_CMD_REGISTER_CALLBACK = 0x1d    // registerNavigateCallback(INavigateCallback)
    const val INCALL_CMD_UNREGISTER_CALLBACK = 0x1e  // unRegisterNavigateCallback(INavigateCallback)
    const val INCALL_CMD_REQUEST_NAVI_FOCUS = 0x3f   // requestNaviFocus(String pkg, INaviFocusCallback)
    const val INCALL_CMD_ABANDON_NAVI_FOCUS = 0x40   // abandonNaviFocus(String pkg, INaviFocusCallback)
}
