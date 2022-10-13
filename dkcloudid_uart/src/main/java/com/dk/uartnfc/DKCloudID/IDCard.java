package com.dk.uartnfc.DKCloudID;

import android.util.Log;

import com.dk.uartnfc.Card.Iso14443BIdCard;
import com.dk.uartnfc.DeviceManager.Command;
import com.dk.uartnfc.Exception.CardNoResponseException;
import com.dk.uartnfc.Exception.DKCloudIDException;
import com.dk.uartnfc.Exception.DeviceNoResponseException;
import com.dk.uartnfc.Tool.StringTool;
import com.dk.uartnfc.Card.SamVIdCard;

public class IDCard {
    private final static int DEFAULT_RETRY_TIME = 3;
    private final static String TAG = "IDCard";
    SamVIdCard mSamVIdCard = null;
    DKCloudID dkCloudID = null;

    private onReceiveScheduleListener mOnReceiveScheduleListener;

    byte[] initData;

    private static IDCard instance = new IDCard();

    private IDCard() {}

//    private IDCard(SamVIdCard samVIdCard) {
//        this.mSamVIdCard = samVIdCard;
//    }

    //单例模式
    public static IDCard getInstance() {
        return instance;
    }

    //单例模式
    public static IDCard getInstance(SamVIdCard samVIdCard) {
        instance.mSamVIdCard = samVIdCard;
        return instance;
    }

    //进度回调
    public interface onReceiveScheduleListener{
        void onReceiveSchedule(int rate);
    }

    /**
     * 获取身份证数据，带失败重试
     * @param retryTime - 失败重试次数
     * @return 身份证数据
     * @throws DKCloudIDException 解析出错会进此异常
     */
    public IDCardData getIDCardData(int retryTime) throws DKCloudIDException, CardNoResponseException {
        return getIDCardData(retryTime, null);
    }

    /**
     * 获取身份证数据，带失败重试
     * @param retryTime - 失败重试次数
     * @param listener - 进度回调
     * @return 身份证数据
     * @throws DKCloudIDException 解析出错会进此异常
     */
    public IDCardData getIDCardData(int retryTime, onReceiveScheduleListener listener) throws DKCloudIDException, CardNoResponseException {
        DKCloudIDException errMsg = null;
        int cnt = 0;
        do {
            try {
                return getIDCardData(mSamVIdCard, listener);
            } catch (DKCloudIDException e) {   //服务器返回异常，重复retryTime次解析
//                e.printStackTrace();
                Log.e(TAG, e.getMessage());
                errMsg = e;
            } catch (CardNoResponseException e) {
                try {
                    mSamVIdCard.serialManager.sendWithReturn(Command.getCmdBytes(Command.PDU_DEACTIVIT));
                } catch (DeviceNoResponseException deviceNoResponseException) {
                    deviceNoResponseException.printStackTrace();
                }
                throw e;
            }

        }while ( cnt++ < retryTime );  //如果服务器返回异常则重复读retryTime次直到成功

        throw errMsg;
    }

    /**
     * 获取身份证数据，带失败重试
     * @param retryTime - 失败重试次数
     * @return 身份证数据
     * @throws DKCloudIDException 解析出错会进此异常
     */
    public IDCardData getIDCardData(Iso14443BIdCard iso14443BIdCard, int retryTime) throws DKCloudIDException, CardNoResponseException {
        return getIDCardData(iso14443BIdCard, retryTime, null);
    }

    /**
     * 获取身份证数据，带失败重试
     * @param retryTime - 失败重试次数
     * @param listener - 进度回调
     * @return 身份证数据
     * @throws DKCloudIDException 解析出错会进此异常
     */
    public IDCardData getIDCardData(Iso14443BIdCard iso14443BIdCard, int retryTime, onReceiveScheduleListener listener) throws DKCloudIDException, CardNoResponseException {
        DKCloudIDException errMsg = null;
        int cnt = 0;
        do {
            try {
                return getIDCardData(iso14443BIdCard, listener);
            } catch (DKCloudIDException e) {   //服务器返回异常，重复retryTime次解析
//                e.printStackTrace();
                Log.e(TAG, e.getMessage());
                errMsg = e;
            } catch (CardNoResponseException e) {
                try {
                    iso14443BIdCard.close();
                } catch (CardNoResponseException e1) {
                    e1.printStackTrace();
                }
                throw e;
            }
        }while ( cnt++ < retryTime );  //如果服务器返回异常则重复读retryTime次直到成功

        throw errMsg;
    }

    /**
     * 获取身份证数据
     * @return 身份证数据
     * @throws DKCloudIDException 解析出错会进此异常
     */
    public IDCardData getIDCardData() throws DKCloudIDException, CardNoResponseException {
        return getIDCardData(DEFAULT_RETRY_TIME, null);
    }

    /**
     * 获取身份证数据，带进度回调
     * @param listener - 进度回调
     * @return 身份证数据
     * @throws DKCloudIDException 解析出错会进此异常
     */
    public IDCardData getIDCardData(onReceiveScheduleListener listener) throws DKCloudIDException, CardNoResponseException {
        return getIDCardData(DEFAULT_RETRY_TIME, listener);
    }

    /**
     * 获取身份证数据
     * @param iso14443BIdCard - 获取到的B类型的tag
     * @return 身份证数据
     * @throws DKCloudIDException 解析出错会进此异常
     */
    public IDCardData getIDCardData(Iso14443BIdCard iso14443BIdCard) throws DKCloudIDException, CardNoResponseException {
        return getIDCardData(iso14443BIdCard, null);
    }

    /**
     * 获取身份证数据
     * @param samVIdCard - 获取到的B类型的tag
     * @return 身份证数据
     * @throws DKCloudIDException 解析出错会进此异常
     */
    public IDCardData getIDCardData(SamVIdCard samVIdCard) throws DKCloudIDException, CardNoResponseException {
        return getIDCardData(samVIdCard, null);
    }

    /**
     * 获取身份证数据，带进度回调
     * @param iso14443BIdCard - 获取到的身份证类型的tag
     * @param listener - 进度回调
     * @return 身份证数据
     * @throws DKCloudIDException 解析出错会进此异常
     */
    public IDCardData getIDCardData(Iso14443BIdCard iso14443BIdCard, onReceiveScheduleListener listener) throws DKCloudIDException, CardNoResponseException {
        mSamVIdCard = new SamVIdCard(iso14443BIdCard.deviceManager, iso14443BIdCard.getSamVInitData());
        return getIDCardData(mSamVIdCard, listener);
    }

    /**
     * 获取身份证数据，带进度回调
     * @param samVIdCard - 获取到的B类型的tag
     * @param listener - 进度回调
     * @return 身份证数据
     * @throws DKCloudIDException 解析出错会进此异常
     */
    public synchronized IDCardData getIDCardData(SamVIdCard samVIdCard, onReceiveScheduleListener listener) throws DKCloudIDException, CardNoResponseException {
        if (samVIdCard == null) {
            throw new DKCloudIDException("参数“SamVIdCard”为null");
        }

        byte[] msgReturnBytes;
        int sendByteLen = 0;
        //boolean returnFlag = false;
        int rate = 5;

        mOnReceiveScheduleListener = listener;

        try {
            msgReturnBytes = samVIdCard.getSamVInitData();
            initData = msgReturnBytes;

            dkCloudID = new DKCloudID();
            if ( !dkCloudID.isConnected() ) {
                throw new DKCloudIDException("服务器连接失败");
            }
            Log.d(TAG, "向服务器发送数据：" + StringTool.byteHexToSting(msgReturnBytes));
            sendByteLen += msgReturnBytes.length;
            byte[] cloudReturnByte = dkCloudID.dkCloudTcpDataExchange(msgReturnBytes);
            Log.d(TAG, "接收到服务器数据：" + StringTool.byteHexToSting(cloudReturnByte));

            Log.d(TAG, "正在解析:1%");
            int schedule = 1;
            if ( (cloudReturnByte != null) && (cloudReturnByte.length >= 2)
                    && ((cloudReturnByte[0] == 0x03) || (cloudReturnByte[0] == 0x04)) ) {
                if ( mOnReceiveScheduleListener != null ) {
                    mOnReceiveScheduleListener.onReceiveSchedule(schedule);
                }
            }

            while (true) {
                if ( (cloudReturnByte == null) || (cloudReturnByte.length < 2)
                        || ((cloudReturnByte[0] != 0x03) && (cloudReturnByte[0] != 0x04)) ) {
                    if ( (cloudReturnByte == null) || (cloudReturnByte.length == 0) ) {
                        throw new DKCloudIDException("服务器返回数据为空");
                    }
                    else if (cloudReturnByte[0] == 0x05) {
                        throw new DKCloudIDException("解析失败, 请重新读卡");
                    }
                    else if (cloudReturnByte[0] == 0x06) {
                        throw new DKCloudIDException("该设备未授权, 请提供IMEI联系商家获取授权商家\r\n");
                    }
                    else if (cloudReturnByte[0] == 0x07) {
                        throw new DKCloudIDException("该设备已被禁用, 请联系商家");
                    }
                    else if (cloudReturnByte[0] == 0x08) {
                        throw new DKCloudIDException("该账号已被禁用, 请联系商家");
                    }
                    else if (cloudReturnByte[0] == 0x09) {
                        throw new DKCloudIDException("余额不足, 请联系商家充值\r\n");
                    }
                    else {
                        throw new DKCloudIDException("未知错误");
                    }
                }
                else if ((cloudReturnByte[0] == 0x04) && (cloudReturnByte.length > 300)) {
                    byte[] decrypted = new byte[cloudReturnByte.length - 3];
                    System.arraycopy(cloudReturnByte, 3, decrypted, 0, decrypted.length);

                    if (schedule != rate) {
                        if (mOnReceiveScheduleListener != null) {
                            mOnReceiveScheduleListener.onReceiveSchedule((100));
                        }
                    }

                    final IDCardData idCardData = new IDCardData(decrypted);
                    Log.d(TAG, "解析成功：" + idCardData.toString());
                    return idCardData;
                }

                msgReturnBytes = samVIdCard.transceive(cloudReturnByte);
                if (msgReturnBytes.length == 2) {
                    throw new CardNoResponseException("解析出错：" + String.format("%d", ((msgReturnBytes[0] & 0xff) << 8) | (msgReturnBytes[1] & 0xff) ));
                }

                if ( (msgReturnBytes.length > 0) && (msgReturnBytes[0] == (byte)0xB3) ) {
                    sendByteLen += msgReturnBytes.length;
                    if ( (sendByteLen == 1590) || (sendByteLen == 1617) ) {
                        rate = 4;
                    }
                }
                Log.d(TAG, String.format("正在解析%%%d ", (int)((++schedule) * 100 / rate)) + sendByteLen);
                if (mOnReceiveScheduleListener != null) {
                    mOnReceiveScheduleListener.onReceiveSchedule((int) (schedule * 100 / rate));
                }

                Log.d(TAG, "向服务器发送数据：" + StringTool.byteHexToSting(msgReturnBytes));
                cloudReturnByte = dkCloudID.dkCloudTcpDataExchange(msgReturnBytes);
                Log.d(TAG, "接收到服务器数据：" + StringTool.byteHexToSting(cloudReturnByte));
            }
        } catch (CardNoResponseException e) {
            throw e;
        }
//        finally {
//            if (dkCloudID != null) {
//                dkCloudID.Close();
//            }
//        }
    }
}
