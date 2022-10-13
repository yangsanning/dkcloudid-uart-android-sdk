package com.dk.uartnfc.DeviceManager;

import com.dk.uartnfc.DKCloudID.DKCloudID;
import com.dk.uartnfc.Exception.CardNoResponseException;
import com.dk.uartnfc.Exception.DeviceNoResponseException;

import static com.dk.uartnfc.Card.Card.CAR_NO_RESPONSE_TIME_MS;

public class UartNfcDevice extends DeviceManager{
    /**
     * 销毁资源
     */
    public void destroy() {
        //关闭串口
        if (serialManager != null) {
            serialManager.close();
        }

        //关闭服务器连接
        DKCloudID.Close();

        release();
    }

    /**
     * 打开蜂鸣器指令，同步阻塞方式
     * @param delayMs  打开蜂鸣器时间：0~0xffff，单位ms
     * @param n          蜂鸣器响多少声：0~255
     * @return           true - 操作成功
     *                    false - 操作失败
     * @throws DeviceNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean openBeep(int delayMs, int n) throws DeviceNoResponseException {
        try {
            byte[] nfc_return_bytes = serialManager.sendWithReturn(Command.getCmdBytes(Command.OPEN_BEEP_CMD, (byte)(delayMs / 10), new byte[]{(byte)n}), CAR_NO_RESPONSE_TIME_MS);
            return Command.verify_ack(nfc_return_bytes);
        } catch (DeviceNoResponseException e) {
            e.printStackTrace();
            throw new DeviceNoResponseException("无响应");
        }
    }
}
