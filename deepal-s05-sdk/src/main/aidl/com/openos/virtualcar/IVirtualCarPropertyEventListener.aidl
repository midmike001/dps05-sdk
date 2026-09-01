package com.openos.virtualcar;

import com.openos.virtualcar.entity.VirtualCarValue;
import java.util.List;

interface IVirtualCarPropertyEventListener {
    void onEventList(in List<VirtualCarValue> list);
    void onChangeEvent(in VirtualCarValue value);
    void onErrorEvent(int propId, int errorCode);
}
