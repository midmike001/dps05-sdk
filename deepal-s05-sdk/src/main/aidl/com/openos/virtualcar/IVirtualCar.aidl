package com.openos.virtualcar;

import android.os.IBinder;

interface IVirtualCar {
    void setVirtualCarServiceHelper(in IBinder helper);
    IBinder getVirtualCarService(String serviceName);
    int getVirtualCarConnectionType();
}
