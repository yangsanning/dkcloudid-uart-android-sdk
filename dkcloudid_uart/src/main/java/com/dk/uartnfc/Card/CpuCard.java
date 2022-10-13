package com.dk.uartnfc.Card;

import com.dk.uartnfc.DeviceManager.Command;
import com.dk.uartnfc.DeviceManager.DeviceManager;
import com.dk.uartnfc.Exception.CardNoResponseException;
import com.dk.uartnfc.Exception.DeviceNoResponseException;
import com.dk.uartnfc.Tool.StringTool;
import com.dk.uartnfc.UartManager.DKMessageDef;
import com.dk.uartnfc.UartManager.SerialManager;

import java.util.Arrays;

public class CpuCard extends Card {
    private static final int APDU_DEFAULT_TIMEOUT_MS       = 1000;

    public CpuCard(DeviceManager deviceManager) {
        super(deviceManager);
    }

    public CpuCard(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }

    /**
     * 获取CPU卡的ATR数据，同步阻塞方式
     * @return         返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] getAtr() throws CardNoResponseException {
        try {
            byte[] nfc_return_bytes = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.GET_ATR_CMD), 200);
            DKMessageDef msg = Command.getRspMsg(nfc_return_bytes);
            atr = msg.data;
            return atr;
        } catch (DeviceNoResponseException e) {
            e.printStackTrace();
            throw new CardNoResponseException("读取数据失败，请不要移动卡片");
        }
    }

    /**
     * cpu卡指令传输，同步阻塞方式
     * @param cmdStr   发送的数据, 16进制字符串
     * @return         返回的数据，16进制字符串
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public String transceive(String cmdStr) throws CardNoResponseException {
        return StringTool.byteHexToSting(transceive(StringTool.hexStringToBytes(cmdStr)));
    }

    /**
     * cpu卡指令传输，同步阻塞方式
     * @param data     发送的数据
     * @return         返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] transceive(byte[] data) throws CardNoResponseException {
        return transceive(data, APDU_DEFAULT_TIMEOUT_MS);
    }

    /**
     * cpu卡指令传输，同步阻塞方式
     * @param cmdStr   发送的数据, 16进制字符串
     * @param timeout  命令响应超时时间
     * @return         返回的数据，16进制字符串
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public String transceive(String cmdStr, int timeout) throws CardNoResponseException {
        return StringTool.byteHexToSting(transceive(StringTool.hexStringToBytes(cmdStr), timeout));
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
