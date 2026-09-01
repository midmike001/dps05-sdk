package com.openos.virtualcar;

import com.openos.virtualcar.entity.VirtualCarValue;
import com.openos.virtualcar.entity.VirtualPropertyConfig;
import com.openos.virtualcar.IVirtualCarPropertyEventListener;
import java.util.List;
import java.util.Map;

interface IVirturalCarProperty {
    int setValue(in VirtualCarValue value);
    VirtualCarValue getValue(int propId, int areaId);
    boolean isSupport(int propId, int areaId);
    void register(in int[] propIds, in IVirtualCarPropertyEventListener listener);
    void unRegister(in int[] propIds, in IVirtualCarPropertyEventListener listener);
    boolean isConnected();
    List<VirtualPropertyConfig> getPropertyConfigList();
    VirtualPropertyConfig getPropertyConfig(int propId);
    boolean reportConcern(in Map concernMap, in Map params, in IVirtualCarPropertyEventListener listener);
    boolean reportUnConcern();
}
