package com.dk.uartnfc.Card;

import com.dk.uartnfc.DeviceManager.DeviceManager;

/**
 * Created by Administrator on 2016/9/21.
 */
public class FeliCa extends Card{
    public FeliCa(DeviceManager deviceManager) {
        super(deviceManager);
    }

    public FeliCa(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }
}
