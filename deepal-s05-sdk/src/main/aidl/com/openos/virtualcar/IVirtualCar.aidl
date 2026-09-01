package com.openos.virtualcar;

import android.os.IBinder;

interface IVirtualCar {
    int getVersion();
    IBinder getCarService(String serviceName);
}
