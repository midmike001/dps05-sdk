package com.openos.virtualcar;

interface IVirturalCarProperty {
    int getVersion();
    boolean setProperty(int flag, int group, int propId, int areaId, int reserved, long timestamp, String className, inout byte[] value);
    byte[] getProperty(int propId, int areaId);
}
