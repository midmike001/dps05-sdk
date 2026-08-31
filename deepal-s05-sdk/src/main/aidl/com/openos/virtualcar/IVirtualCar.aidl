package com.openos.virtualcar;

interface IVirtualCar {
    int getVersion();
    IBinder getCarService(String serviceName);
}
