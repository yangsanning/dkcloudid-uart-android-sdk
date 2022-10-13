package com.dk.uartnfc.Card;

import com.dk.uartnfc.DeviceManager.Command;
import com.dk.uartnfc.DeviceManager.DeviceManager;
import com.dk.uartnfc.Exception.CardNoResponseException;
import com.dk.uartnfc.Exception.DeviceNoResponseException;
import com.dk.uartnfc.UartManager.DKMessageDef;

import java.util.Arrays;

public class Iso14443bCard extends Card {
    private static final int APDU_DEFAULT_TIMEOUT_MS       = 1000;

    public Iso14443bCard(DeviceManager deviceManager) {
        super(deviceManager);
    }

    public Iso14443bCard(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }

    /**
     * cpu卡指令传输，同步阻塞方式
     * @param data     发送的数据
     * @param timeout  命令响应超时时间
     * @return         返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] transceive(byte[] data, int timeout) throws CardNoResponseException {
        synchronized(this) {
            if ( (data == null) || (data.length == 0) ) {
                throw new CardNoResponseException("数据不能为null");
            }

            try {
                byte[] nfc_return_bytes = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.A_PDU_COM, data), timeout);
                return verify_apdu_cmd_return(nfc_return_bytes);
            } catch (DeviceNoResponseException e) {
                e.printStackTrace();
                throw new CardNoResponseException("读取数据失败，请不要移动卡片");
            }
        }
    }

    public static byte[] verify_apdu_cmd_return(byte[] data)  throws CardNoResponseException{
        DKMessageDef msg = Command.getRspMsg(data);
        return msg.data;
    }
}
