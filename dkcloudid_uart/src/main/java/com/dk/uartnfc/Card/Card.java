package com.dk.uartnfc.Card;

import com.dk.uartnfc.DeviceManager.Command;
import com.dk.uartnfc.DeviceManager.DeviceManager;
import com.dk.uartnfc.Exception.CardNoResponseException;
import com.dk.uartnfc.Exception.DeviceNoResponseException;

public class Card {
    public final static int CAR_NO_RESPONSE_TIME_MS = 500;  //卡片无响应等等时间500ms

    public DeviceManager deviceManager;
    public byte[] uid;
    public byte[] atr;

    public Card(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    public Card(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        this.deviceManager = deviceManager;
        this.uid = uid;
        this.atr = atr;
    }

    public String uidToString() {
        if ( (uid == null) || (uid.length == 0) ) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (int i=0; i<uid.length; i++) {
            stringBuffer.append(String.format("%02x", uid[i]));
        }
        return stringBuffer.toString();
    }

    /**
     * 卡片掉电，同步阻塞方式
     * @return         true - 操作成功
     *                  false - 操作失败
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean close() throws CardNoResponseException {
        try {
            byte[] nfc_return_bytes = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.PDU_DEACTIVIT), CAR_NO_RESPONSE_TIME_MS);
            return Command.verify_ack(nfc_return_bytes);
        } catch (DeviceNoResponseException e) {
            e.printStackTrace();
            throw new CardNoResponseException("无响应");
        }
    }
}
