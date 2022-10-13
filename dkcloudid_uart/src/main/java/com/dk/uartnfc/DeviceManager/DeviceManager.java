package com.dk.uartnfc.DeviceManager;

import android.content.Context;
import android.util.Log;

import com.dk.uartnfc.Card.CpuCard;
import com.dk.uartnfc.Card.DESFire;
import com.dk.uartnfc.Card.DeviceManagerCallback;
import com.dk.uartnfc.Card.FeliCa;
import com.dk.uartnfc.Card.Iso14443BIdCard;
import com.dk.uartnfc.Card.Iso14443bCard;
import com.dk.uartnfc.Card.Iso15693Card;
import com.dk.uartnfc.Card.Mifare;
import com.dk.uartnfc.Card.Ntag21x;
import com.dk.uartnfc.Card.Ultralight;
import com.dk.uartnfc.DKCloudID.IDCard;
import com.dk.uartnfc.DKCloudID.IDCardData;
import com.dk.uartnfc.Exception.CardNoResponseException;
import com.dk.uartnfc.Exception.DKCloudIDException;
import com.dk.uartnfc.Tool.StringTool;
import com.dk.uartnfc.UartManager.DKMessageDef;
import com.dk.uartnfc.UartManager.SerialManager;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DeviceManager {
    private final static String TAG = "DeviceManager";
    public SerialManager serialManager;

    public final static byte  CARD_TYPE_NO_DEFINE = 0x00;                       //卡片类型：未定义
    public final static byte  CARD_TYPE_ISO4443_A = Command.PICC_TYPE_ACPU;     //卡片类型ISO14443-A
    public final static byte  CARD_TYPE_ISO4443_B = Command.PICC_TYPE_B;        //卡片类型ISO14443-B
    public final static byte  CARD_TYPE_FELICA = Command.PICC_TYPE_FELICA;      //卡片类型Felica
    public final static byte  CARD_TYPE_MIFARE = Command.PICC_TYPE_M1;          //卡片类型Mifare卡
    public final static byte  CARD_TYPE_ISO15693 = Command.PICC_TYPE_15693;     //卡片类型iso15693卡
    public final static byte  CARD_TYPE_ULTRALIGHT = Command.PICC_TYPE_UL;      //RF_TYPE_MF
    public final static byte  CARD_TYPE_DESFire = Command.PICC_TYPE_DF;         //DESFire卡
    public final static byte  CARD_TYPE_SAMV_ID = (byte)0xa3;                   //身份证

    private DeviceManagerCallback mDeviceManagerCallback = null;
    private static final Semaphore semaphore = new Semaphore(1);

    public CpuCard cpuCard;
    public Iso14443bCard iso14443bCard;
    public DESFire desFire;
    public Iso15693Card iso15693Card;
    public Mifare mifare;
    public Ntag21x ntag21x;
    public Ultralight ultralight;
    public FeliCa feliCa;
    public Iso14443BIdCard iso14443BIdCard;
    public int mCardType;

    public DeviceManager() {
        //串口初始化
        serialManager = new SerialManager();

        serialManager.setOnReceiveDataListener(new SerialManager.onReceiveDataListener() {
            @Override
            public void OnReceiverData(String portNumberString, byte[] data) {
                try {
                    Log.d(TAG, portNumberString + "接收(" + data.length + ")：" + StringTool.byteHexToSting(data) + "\r\n");
                    DKMessageDef dkMessageDef = Command.getRspMsg(data);
                    switch (dkMessageDef.command) {
                        case Command.GET_UID:
                            mCardType = dkMessageDef.data[0];
                            byte[] uidBytes = Arrays.copyOfRange( dkMessageDef.data, 1, dkMessageDef.data.length );
                            byte[] atrBytes = null;

                            switch (mCardType) {
                                case Command.CARD_TYPE_ISO4443_A:
                                    cpuCard = new CpuCard(DeviceManager.this, uidBytes, atrBytes);
                                    break;

                                case Command.CARD_TYPE_ISO4443_B:
                                    iso14443bCard = new Iso14443bCard(DeviceManager.this, uidBytes, atrBytes);
                                    break;

                                case Command.CARD_TYPE_FELICA:
                                    feliCa = new FeliCa(DeviceManager.this, uidBytes, atrBytes);
                                    break;

                                case Command.CARD_TYPE_MIFARE:
                                    mifare = new Mifare(DeviceManager.this, uidBytes, atrBytes);
                                    break;

                                case Command.CARD_TYPE_ISO15693:
                                    iso15693Card = new Iso15693Card(DeviceManager.this, uidBytes, atrBytes);
                                    break;

                                case Command.CARD_TYPE_ULTRALIGHT:
                                    ntag21x = new Ntag21x(DeviceManager.this, uidBytes, atrBytes);
                                    break;

                                case Command.CARD_TYPE_DESFire:
                                    desFire = new DESFire(DeviceManager.this, uidBytes, atrBytes);
                                    break;

                                default:
                                    break;
                            }

                            if (mDeviceManagerCallback != null) {
                                mDeviceManagerCallback.onReceiveRfnSearchCard(true, mCardType, uidBytes, null);
                            }
                            break;

                        case Command.SAM_V_INIT_COM:
                            mCardType = Command.CARD_TYPE_SAMV_ID;
                            iso14443BIdCard = new Iso14443BIdCard(DeviceManager.this);
                            iso14443BIdCard.setInitData(dkMessageDef.data);

                            //如果上次解析还未完成，则抛弃新刷的身份证
                            try {
                                if ( !semaphore.tryAcquire(0, TimeUnit.MILLISECONDS) ) {
                                    Log.d(TAG, "上次解析还未完成，抛弃本次刷的身份证");
                                    break;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                Log.d(TAG, "上次解析还未完成，抛弃本次刷的身份证");
                                break;
                            }

                            if (mDeviceManagerCallback != null) {
                                mDeviceManagerCallback.onReceiveSamVIdStart(dkMessageDef.data);
                            }

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        IDCardData idCardData = startDkcloudid(iso14443BIdCard);
                                        if ( (idCardData != null) && (mDeviceManagerCallback != null) ) {
                                            if ( idCardData.PhotoBmp == null ) {
                                                mDeviceManagerCallback.onReceiveSamVIdException("明文数据异常");
                                            }
                                            else {
                                                mDeviceManagerCallback.onReceiveIDCardData(idCardData);
                                            }
                                        }
                                    }catch (Exception e) {
                                        e.printStackTrace();
                                        if (mDeviceManagerCallback != null) {
                                            mDeviceManagerCallback.onReceiveSamVIdException(e.getMessage());
                                        }
                                    }

                                    semaphore.release();
                                }
                            }).start();
                            break;

                        case Command.COM_ACK:
                            if (mDeviceManagerCallback != null) {
                                mDeviceManagerCallback.onReceiveACK();
                            }
                            break;

                        case Command.COM_NACK:
                            if (mDeviceManagerCallback != null) {
                                mDeviceManagerCallback.onReceiveNACK();
                            }
                            break;

                        case Command.EER_NO_ACTIVIT:
                            if (mDeviceManagerCallback != null) {
                                mDeviceManagerCallback.onReceiveCardLeave();
                            }
                            break;

                        default:
                            break;
                    }
                } catch (CardNoResponseException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    public  Object getCard() {
        switch (mCardType) {
            case Command.CARD_TYPE_ISO4443_A:
                return cpuCard;
            case Command.CARD_TYPE_ISO4443_B:
                return iso14443bCard;
            case Command.CARD_TYPE_FELICA:
                return feliCa;
            case Command.CARD_TYPE_MIFARE:
                return mifare;
            case Command.CARD_TYPE_ISO15693:
                return iso15693Card;
            case Command.CARD_TYPE_ULTRALIGHT:
                return ntag21x;
            case Command.CARD_TYPE_DESFire:
                return desFire;
            case Command.CARD_TYPE_SAMV_ID:
                return iso14443BIdCard;
            default:
                return null;
        }
    }

    public void setCallBack(DeviceManagerCallback callBack) {
        mDeviceManagerCallback = callBack;
    }

    //云解码流程
    private synchronized IDCardData startDkcloudid(Iso14443BIdCard card) {
        if (card == null) {
            Log.e(TAG, "未找到身份证");
            if (mDeviceManagerCallback != null) {
                mDeviceManagerCallback.onReceiveSamVIdException("未找到身份证");
            }
            return null;
        }

        try {
            /**
             * 获取身份证数据，带进度回调，如果不需要进度回调可以去掉进度回调参数或者传入null
             * 注意：此方法为同步阻塞方式，需要一定时间才能返回身份证数据，期间身份证不能离开读卡器！
             */
            return IDCard.getInstance().getIDCardData(card, 5, new IDCard.onReceiveScheduleListener() {
                @Override
                public void onReceiveSchedule(int rate) {  //读取进度回调
                    if (mDeviceManagerCallback != null) {
                        mDeviceManagerCallback.onReceiveSamVIdSchedule(rate);
                    }
                }
            });
        } catch (DKCloudIDException e) {   //服务器返回异常，重复5次解析
//            e.printStackTrace();
            if (mDeviceManagerCallback != null) {
                mDeviceManagerCallback.onReceiveSamVIdException(e.getMessage());
            }
        } catch (CardNoResponseException e) {    //卡片读取异常，直接退出，需要重新读卡
//            e.printStackTrace();
            if (mDeviceManagerCallback != null) {
                mDeviceManagerCallback.onReceiveSamVIdException(e.getMessage());
            }
        }

        return null;
    }

    public void release() {
        semaphore.release();
    }
}
