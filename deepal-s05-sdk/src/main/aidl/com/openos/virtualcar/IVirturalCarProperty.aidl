package com.openos.virtualcar;

interface IVirturalCarProperty {
    int getVersion();
    int setProperty(int flag, int group, int propId, int areaId, int reserved, long timestamp, String className);
    int getProperty(int propId, int areaId);
}
