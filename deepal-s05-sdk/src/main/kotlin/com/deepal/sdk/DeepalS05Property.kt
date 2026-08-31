package com.deepal.sdk

/**
 * Vehicle hardware property IDs, area configurations, and transaction codes
 * for Changan Deepal S05 (Platform Model C857, EPA platform).
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

    // Vehicle Specifications
    const val BATTERY_CAPACITY_KWH = 56.12f
    const val TEMP_MIN_C = 17.5f
    const val TEMP_MAX_C = 32.5f

    // Area IDs
    const val AREA_GLOBAL = 0
    const val AREA_DRIVER = 1
    const val AREA_PASSENGER = 4
    const val AREA_DOORS_ALL = 0x0F
    const val AREA_WINDOW_FL = 0x010
    const val AREA_WINDOW_FR = 0x040
    const val AREA_WINDOW_RL = 0x100
    const val AREA_WINDOW_RR = 0x400

    // Vehicle Telemetry Signals (Read)
    const val PROP_VEHICLE_SPEED = 0x31600204     // Float (m/s or km/h)
    const val PROP_GEAR_SELECTION = 0x3140028c     // Int: 1=P, 2=R, 3=N, 4=D
    const val PROP_BATTERY_SOC = 0x314006c4        // Int: 0-100%
    const val PROP_REMAINING_RANGE = 0x31410605    // Int: km
    const val PROP_ODOMETER = 0x31600202           // Float: km
    const val PROP_EXTERIOR_TEMP = 0x35600403      // Float: °C

    // Climate Control Properties (Read/Write)
    const val PROP_HVAC_TEMP_SET = 0x35600105      // Float: 17.5 - 32.5 °C
    const val PROP_HVAC_POWER_ON = 0x3540010b      // Int: 1=On, 2=Off
    const val PROP_HVAC_AC_ON = 0x35400104         // Int: 1=On, 2=Off
    const val PROP_HVAC_DEFROST_FRONT = 0x33400103 // Int: 1=On, 2=Off
    const val PROP_HVAC_DEFROST_REAR = 0x3540010c  // Int: 1=On, 2=Off
    const val PROP_HVAC_FAN_SPEED = 0x35400109     // Int: 1 - 8
    const val PROP_HVAC_RECIRC = 0x35400108        // Int: 1=Recirc, 2=Fresh air
    const val PROP_HVAC_AUTO = 0x35400101          // Int: 1=Auto, 2=Manual
    const val PROP_HVAC_SYNC = 0x3540010d          // Int: 1=Sync, 2=Dual
    const val PROP_AEB_SWITCH = 0x31400244         // Int: 1=On, 2=Off
    const val PROP_DRIVE_MODE = 0x3140028e         // Int: 1=ECO, 2=COMFORT, 3=SPORT

    // Comfort & Seats (Read/Write)
    const val PROP_SEAT_HEATING = 0x3540010f       // Int: 0=Off, 1=Low, 2=Med, 3=High (Area: 1=Driver, 4=Passenger)
    const val PROP_SEAT_VENTILATION = 0x35400111   // Int: 0=Off, 1=Low, 2=Med, 3=High (Area: 1=Driver, 4=Passenger)
    const val PROP_SEAT_MASSAGE_TOGGLE = 0x31400b2f // Int: 1=On, 2=Off (Area: 0=Driver, 4=Passenger)
    const val PROP_SEAT_MASSAGE_MODE = 0x31400b31   // Int: Mode 1-3
    const val PROP_SEAT_MASSAGE_LEVEL = 0x31400b30  // Int: Level 1-3
    const val PROP_STEERING_WHEEL_HEAT = 0x314003eb // Int: 1=On, 2=Off

    // Windows & Access (Read/Write)
    const val PROP_WINDOW_MOVE = 0x33400301        // Int: 1=Open, 2=Close, 0=Stop
    const val PROP_SUNROOF_SHADE = 0x31400313      // Int: 1=Open, 2=Close, 0=Stop
    const val PROP_TAILGATE = 0x3140040d           // Int: 1=Open, 2=Close
    const val PROP_DOOR_LOCK = 0x15400505          // Int: 1=Locked, 2=Unlocked

    // Lighting & Air Quality
    const val PROP_AMBIENT_LIGHT = 0x3140039a      // Int: Color & Mode packed
    const val PROP_AMBIENT_LIGHT_BRIGHTNESS = 0x3140039b // Int: 0-100%
    const val PROP_AIR_PURIFIER = 0x35400122        // Int: 1=On, 2=Off

    // Intelligent EV & Body Automation
    const val PROP_BATTERY_PRECONDITIONING = 0x314006c6 // Int: 1=Preheating for Fast Charging, 2=Off
    const val PROP_RAIN_SENSOR_STATE = 0x31400277       // Int: 1=No Rain, 2=Light Rain, 3=Heavy Rain

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
