package com.huang.lochy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dk.uartnfc.Card.CpuCard;
import com.dk.uartnfc.Card.DESFire;
import com.dk.uartnfc.Card.DeviceManagerCallback;
import com.dk.uartnfc.Card.FeliCa;
import com.dk.uartnfc.Card.Iso14443BIdCard;
import com.dk.uartnfc.Card.Iso14443bCard;
import com.dk.uartnfc.Card.Iso15693Card;
import com.dk.uartnfc.Card.Mifare;
import com.dk.uartnfc.Card.Ntag21x;
import com.dk.uartnfc.DKCloudID.DKCloudID;
import com.dk.uartnfc.DKCloudID.IDCard;
import com.dk.uartnfc.DKCloudID.IDCardData;
import com.dk.uartnfc.DeviceManager.DeviceManager;
import com.dk.uartnfc.DeviceManager.UartNfcDevice;
import com.dk.uartnfc.Exception.CardNoResponseException;
import com.dk.uartnfc.Exception.DKCloudIDException;
import com.dk.uartnfc.Exception.DeviceNoResponseException;
import com.dk.uartnfc.OTA.DialogUtils;
import com.dk.uartnfc.OTA.YModem;
import com.dk.uartnfc.Tool.StringTool;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    final static String TAG = "DKCloudID";

    final String[] botes = new String[]{"9600", "19200", "38400", "57600", "115200", "230400", "460800", "500000", "576000", "921600", "1000000", "1152000"};

    private TextView msgTextView;
    private Spinner spSerial;
    private EditText edInput;
    private Button btOpen;
    private MyTTS myTTS;
    static long time_start = 0;
    static long time_end = 0;

    static int cnt = 0;

    private UartNfcDevice uartNfcDevice;
    String selectSerialName;
    String selectBaudRate;

    private ProgressDialog readWriteDialog = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //???????????????
        myTTS = new MyTTS(MainActivity.this);

        uartNfcDevice = new UartNfcDevice();
        uartNfcDevice.setCallBack(deviceManagerCallback);

        time_start = System.currentTimeMillis();

        iniview();
        edInput.setText("aa020401");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                uartNfcDevice.serialManager.open("/dev/ttyUSB0", "115200");
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uartNfcDevice.destroy();
    }

    //?????????????????????
    private DeviceManagerCallback deviceManagerCallback = new DeviceManagerCallback() {
        //??????????????????
        @Override
        public void onReceiveRfnSearchCard(boolean blnIsSus, int cardType, byte[] bytCardSn, byte[] bytCarATS) {
            super.onReceiveRfnSearchCard(blnIsSus, cardType, bytCardSn, bytCarATS);
            System.out.println("Activity??????????????????????????????UID->" + StringTool.byteHexToSting(bytCardSn) + " ATS->" + StringTool.byteHexToSting(bytCarATS));

            final int cardTypeTemp = cardType;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    readWriteCardDemo(cardTypeTemp);
                }
            }).start();
        }

        //????????????????????????????????????
        @Override
        public void onReceiveSamVIdStart(byte[] initData) {
            super.onReceiveSamVIdStart(initData);

            Log.d(TAG, "????????????");
            logViewln(null);
            logViewln("????????????????????????????????????!");
            myTTS.speak("????????????????????????????????????");

            time_start = System.currentTimeMillis();
        }

        //??????????????????????????????
        @Override
        public void onReceiveSamVIdSchedule(int rate) {
            super.onReceiveSamVIdSchedule(rate);
            showReadWriteDialog("???????????????????????????,????????????????????????", rate);
            if (rate == 100) {
                time_end = System.currentTimeMillis();

                /**
                 * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                 */
                myTTS.speak("????????????");
            }
        }

        //??????????????????????????????
        @Override
        public void onReceiveSamVIdException(String msg) {
            super.onReceiveSamVIdException(msg);

            //??????????????????
            logViewln(msg);

            //?????????????????????????????????
            hidDialog();
        }

        //????????????????????????????????????
        @Override
        public void onReceiveIDCardData(IDCardData idCardData) {
            super.onReceiveIDCardData(idCardData);

            showIDMsg(idCardData);
        }

        //??????????????????
        @Override
        public void onReceiveCardLeave() {
            super.onReceiveCardLeave();
            Log.d(TAG, "???????????????");
            logViewln("???????????????");
        }
    };

    //??????IC?????????API????????????
    private synchronized boolean readWriteCardDemo(int cardType) {
        switch (cardType) {
            case DeviceManager.CARD_TYPE_ISO4443_A:   //??????A CPU???
                final CpuCard cpuCard = (CpuCard) uartNfcDevice.getCard();
                if (cpuCard != null) {
                    logViewln(null);

//                    try {
//                        byte[] atr = cpuCard.getAtr();
//                        logViewln("??????CPU???->UID:" + cpuCard.uidToString() + " ATR:" + StringTool.byteHexToSting(atr));
//
//                        //????????????????????????
//                        byte[] bytApduRtnData = cpuCard.transceive(SZTCard.getSelectMainFileCmdByte());
//                        if (bytApduRtnData.length <= 2) {
//                            System.out.println("?????????????????????????????????????????????");
//                            //???????????????????????????
//                            String cpuCardType;
//                            bytApduRtnData = cpuCard.transceive(FinancialCard.getSelectDepositCardPayFileCmdBytes());
//                            if (bytApduRtnData.length <= 2) {
//                                System.out.println("??????????????????????????????????????????");
//                                //???????????????????????????
//                                bytApduRtnData = cpuCard.transceive(FinancialCard.getSelectDebitCardPayFileCmdBytes());
//                                if (bytApduRtnData.length <= 2) {
//                                    logViewln("??????CPU??????");
//                                    return false;
//                                }
//                                else {
//                                    cpuCardType = "?????????";
//                                }
//                            }
//                            else {
//                                cpuCardType = "?????????";
//                            }
//
//                            bytApduRtnData = cpuCard.transceive(FinancialCard.getCardNumberCmdBytes());
//                            //?????????????????????
//                            String cardNumberString = FinancialCard.extractCardNumberFromeRturnBytes(bytApduRtnData);
//                            if (cardNumberString == null) {
//                                logViewln("??????CPU??????");
//                                return false;
//                            }
//                            logViewln("??????????????????" + cardNumberString);
//
//                            //???????????????
//                            System.out.println("??????APDU??????-???10???????????????");
//                            for (int i = 1; i <= 10; i++) {
//                                bytApduRtnData = cpuCard.transceive(FinancialCard.getTradingRecordCmdBytes((byte) i));
//                                logViewln(FinancialCard.extractTradingRecordFromeRturnBytes(bytApduRtnData));
//                            }
//                        }
//                        else {  //?????????????????????
//                            bytApduRtnData = cpuCard.transceive(SZTCard.getBalanceCmdByte());
//                            if (SZTCard.getBalance(bytApduRtnData) == null) {
//                                logViewln("??????CPU??????");
//                                System.out.println("??????CPU??????");
//                                return false;
//                            }
//                            else {
//                                logViewln("??????????????????" + SZTCard.getBalance(bytApduRtnData));
//                                System.out.println("?????????" + SZTCard.getBalance(bytApduRtnData));
//                                //???????????????
//                                System.out.println("??????APDU??????-???10???????????????");
//                                for (int i = 1; i <= 10; i++) {
//                                    bytApduRtnData = cpuCard.transceive(SZTCard.getTradeCmdByte((byte) i));
//                                    logViewln("\r\n" + SZTCard.getTrade(bytApduRtnData));
//                                }
//                            }
//                        }

//                        //?????????
//                        logViewln("??????1-?????????00A40400085943542E55534552");
//                        String rsp = cpuCard.transceive("00A40400085943542E55534552", 1000);
//                        logViewln("?????????" + rsp);
//                        if ( !rsp.contains("9000") ) {
//                            logViewln("??????2-?????????00A40400085041592E41505059 ");
//                            rsp = cpuCard.transceive("00A40400085041592E41505059 ", 1000);
//                            logViewln("?????????" + rsp);
//                        }
//
//                        logViewln("??????3-?????????C4FE000000 ");
//                        rsp = cpuCard.transceive("C4FE000000", 1000);
//                        logViewln("?????????" + rsp);
//
//                        logViewln("??????4-?????????00A4000002DDF1");
//                        rsp = cpuCard.transceive("00A4000002DDF1", 1000);
//                        logViewln("?????????" + rsp);
//
//                        logViewln("??????5-?????????00B0950000");
//                        rsp = cpuCard.transceive("00B0950000", 1000);
//                        logViewln("?????????" + rsp);
//
//                        logViewln("??????6-?????????00A4000002ADF3");
//                        rsp = cpuCard.transceive("00A4000002ADF3", 1000);
//                        logViewln("?????????" + rsp);
//
//                        logViewln("??????7-?????????0020000003123456");
//                        rsp = cpuCard.transceive("0020000003123456", 1000);
//                        logViewln("?????????" + rsp);
//
//                        logViewln("??????8-?????????805000020B0100000000000000000000");
//                        rsp = cpuCard.transceive("805000020B0100000000000000000000", 1000);
//                        logViewln("?????????" + rsp);

//                        //?????????
//                        logViewln("?????????????????????00A4040005D156000016");
//                        String rsp = cpuCard.transceive("00A4040005D156000016", 1000);
//                        logViewln("?????????" + rsp);

//                    //FM1208 CPU???APDU????????????????????????
//                    try {
//                        byte[] cmd = {(byte)0x80, (byte)0xe0, (byte)0x00, (byte)0xa0, (byte)0x07, (byte)0x28, (byte)0x04, (byte)0x00, (byte)0xf0, (byte)0xf5, (byte)0xff, (byte)0xf2};
//                        cpuCard.transceive(cmd);
//                        cmd = new byte[] {(byte)0x00, (byte)0xa4, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0x00, (byte)0xa0, (byte)0x00 };
//                        cpuCard.transceive(cmd);
//                        cmd  = new byte[] {(byte)0x00, (byte)0xd6, (byte)0x00, (byte)0x00, (byte)0xe0, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d
//                                , (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d
//                                , (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d
//                                , (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d
//                                , (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d
//                                , (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d
//                                , (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d, (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d
//                                , (byte)0xd4, (byte)0xe3, (byte)0x0c, (byte)0x76, (byte)0x4f, (byte)0xf4, (byte)0x4c, (byte)0x3d};
//                        cpuCard.transceive(cmd);
//                        cmd  = new byte[] {(byte)0x00, (byte)0xa4, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0x00, (byte)0xa0, (byte)0x00 };
//                        cpuCard.transceive(cmd);
//                        cmd  = new byte[] {(byte)0x00, (byte)0xb0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
//                        byte[] rsp = cpuCard.transceive(cmd);
//                        Log.d(TAG, "rsp=" + StringTool.byteHexToSting(rsp));
//                    }catch (CardNoResponseException e) {
//                        e.printStackTrace();
//                    }

                    //??????SIM???SEID??????
                    try {
                        byte[] cmd = {(byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x04, (byte)0x00, (byte)0x00};
                        logViewln("??????" + StringTool.byteHexToSting(cmd));
                        byte[] rsp = cpuCard.transceive(cmd);
                        logViewln("??????" + StringTool.byteHexToSting(rsp));

                        cmd = new byte[] {(byte)0x80, (byte)0xca, (byte)0x00, (byte)0x44, (byte)0x00};
                        logViewln("??????" + StringTool.byteHexToSting(cmd));
                        rsp = cpuCard.transceive(cmd);
                        logViewln("??????" + StringTool.byteHexToSting(rsp));

                        logViewln("???????????????" + cnt++);
                        logViewln("???????????????" + ((System.currentTimeMillis() - time_start) / 1000) + "???" );
                        cpuCard.close();
                    } catch (CardNoResponseException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case DeviceManager.CARD_TYPE_FELICA:  //??????FeliCa
                FeliCa feliCa = (FeliCa) uartNfcDevice.getCard();
                if (feliCa != null) {
                    logViewln("??????FeliCa->UID:" + feliCa.uidToString());
                }
                break;
            case DeviceManager.CARD_TYPE_ULTRALIGHT: //??????Ultralight???
                final Ntag21x ntag21x = (Ntag21x) uartNfcDevice.getCard();
                if (ntag21x != null) {
                    try {
                        logViewln("??????Ultralight???->UID:" + ntag21x.uidToString());

                        //??????????????????Demo,????????????????????????
                        byte[] writeBytes = new byte[100];
                        Arrays.fill(writeBytes, (byte) 0xAA);
                        logViewln("?????????100??????????????????0xAA");
                        boolean isSuc = ntag21x.longWrite((byte) 4, writeBytes);
                        if (isSuc) {
                            logViewln("??????????????????");
                            logViewln("?????????10???????????????");
                            byte[] readTempBytes = ntag21x.longRead((byte) 4, (byte) (100 / 4));
                            logViewln("???????????????\r\n" + StringTool.byteHexToSting(readTempBytes));
                        }
                        else {
                            logViewln("??????????????????");
                        }

                        //NDEF Demo

                    } catch (CardNoResponseException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case DeviceManager.CARD_TYPE_MIFARE:   //??????Mifare???
                final Mifare mifare = (Mifare) uartNfcDevice.getCard();
                if (mifare != null) {
                    logViewln(null);
                    logViewln("??????Mifare???->UID:" + mifare.uidToString());
                    Log.d(TAG, "??????Mifare???->UID:" + mifare.uidToString());

                    try {
                        //???????????????NFC???????????????????????????????????????
                        boolean status = mifare.setKey(Mifare.MIFARE_KEY_TYPE_A, Mifare.MIFARE_DEFAULT_KEY);
                        if (status) {
                            logViewln("??????????????????A???????????????");
                        }
                        else {
                            logViewln("??????????????????A???????????????");
                            break;
                        }
                        status = mifare.write(1, new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16});
                        if (status) {
                            logViewln("?????????01020304050607080910111213141516??????1??????");
                        }
                        else {
                            logViewln("?????????01020304050607080910111213141516??????1??????");
                        }

                        byte[] rspBytes = mifare.read(1);
                        logViewln("????????????1?????????" + StringTool.byteHexToSting(rspBytes));
                    } catch (CardNoResponseException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case DeviceManager.CARD_TYPE_ISO15693: //??????15693???
                final Iso15693Card iso15693Card = (Iso15693Card) uartNfcDevice.getCard();
                if (iso15693Card != null) {
                    logViewln(null);
                    logViewln("??????15693???->UID:" + iso15693Card.uidToString());
                    logViewln("??????0?????????");
                    try {
                        boolean status = iso15693Card.write(1, new byte[] {0x01, 0x01, 0x03, 0x04});
                        if (status) {
                            logViewln("?????????01020304??????1??????");
                        }
                        else {
                            logViewln("?????????01020304??????1??????");
                        }

                        byte[] rsp = iso15693Card.read(1);
                        logViewln("???1?????????" + StringTool.byteHexToSting(rsp));

                        rsp = iso15693Card.ReadMultiple(0, 10);
                        logViewln("???0-???10?????????" + StringTool.byteHexToSting(rsp));
                    } catch (CardNoResponseException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case DeviceManager.CARD_TYPE_DESFire:
                final DESFire desFire = (DESFire) uartNfcDevice.getCard();
                if (desFire != null) {
                    logViewln("??????DESFire???->UID:" + desFire.uidToString());
                }
                break;
            case DeviceManager.CARD_TYPE_ISO4443_B:
                final Iso14443bCard iso14443bCard = (Iso14443bCard) uartNfcDevice.getCard();
                if (iso14443bCard != null) {
                    logViewln("??????iso14443b???->UID:" + iso14443bCard.uidToString());
                }
                break;
        }

        return false;
    }

    private void iniview() {
        msgTextView = (TextView) findViewById(R.id.msgText);
        spSerial = (Spinner) findViewById(R.id.sp_serial);
        edInput = (EditText) findViewById(R.id.ed_input);
        btOpen = (Button) findViewById(R.id.bt_open);
        Button btSend = (Button) findViewById(R.id.bt_send);
        Spinner spBote = (Spinner) findViewById(R.id.sp_bote);
        Button btOTA = (Button) findViewById(R.id.bt_ota);

        readWriteDialog = new ProgressDialog(MainActivity.this);
        readWriteDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // ??????ProgressDialog ??????
        readWriteDialog.setTitle("?????????");
        // ??????ProgressDialog ????????????
        readWriteDialog.setMessage("????????????????????????");
        readWriteDialog.setMax(100);

        final List<String> ports = uartNfcDevice.serialManager.getAvailablePorts();  //?????????????????????
        Log.d(TAG, "???????????????????????????" + ports.toString());

        //?????????????????????????????????
        SpAdapter spAdapter = new SpAdapter(this);
        spAdapter.setDatas( ports.toArray(new String[ports.size()]) );
        spSerial.setAdapter(spAdapter);
        spSerial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectSerialName = ports.get(position);
                if ( uartNfcDevice.serialManager.isOpen() ) {
                    uartNfcDevice.serialManager.close();
                    uartNfcDevice.serialManager.open(selectSerialName, selectBaudRate);
                    updataSendBt();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //?????????????????????????????????
        SpAdapter spAdapter2 = new SpAdapter(this);
        spAdapter2.setDatas(botes);
        spBote.setAdapter(spAdapter2);
        spBote.setSelection(4);
        spBote.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectBaudRate = botes[position];
                if ( uartNfcDevice.serialManager.isOpen() ) {
                    uartNfcDevice.serialManager.close();
                    uartNfcDevice.serialManager.open(selectSerialName, selectBaudRate);
                    updataSendBt();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //????????????????????????
        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( uartNfcDevice.serialManager.isOpen() ) {
                    uartNfcDevice.serialManager.close();
                }
                else {
                    uartNfcDevice.serialManager.open(selectSerialName, selectBaudRate);
                    //serialManager.open("/dev/ttyS3", "115200");
                }

                updataSendBt();
            }
        });

        //????????????????????????
        btOTA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!uartNfcDevice.serialManager.isOpen()) {
                    Toast.makeText(getBaseContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //???????????????????????????
                        uartNfcDevice.serialManager.send(StringTool.hexStringToBytes("AA0124"));
                    }
                }).start();

                DialogUtils.select_file(MainActivity.this, new DialogUtils.DialogSelection() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                        if (files.length == 1) {

                            final String fileName = files[0];
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        new YModem(uartNfcDevice.serialManager).send(new File(fileName), new YModem.onReceiveScheduleListener() {
                                            @Override
                                            public void onReceiveSchedule(int rate) {  //??????????????????
                                                showReadWriteDialog("????????????", rate);
                                            }
                                        });

                                        logViewln( "????????????" );
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    finally {
                                        //?????????????????????????????????
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (readWriteDialog.isShowing()) {
                                                    readWriteDialog.dismiss();
                                                }
                                                readWriteDialog.setProgress(0);
                                            }
                                        });
                                    }
                                }
                            }).start();
                        }
                    }
                });
            }
        });

        //??????????????????
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edInput.getText().toString().length() > 0) {
                    if (uartNfcDevice.serialManager.isOpen()) {
                        uartNfcDevice.serialManager.send( StringTool.hexStringToBytes( edInput.getText().toString()) );
                        msgTextView.setText("");
                        refreshLogView("?????????" +  StringTool.byteHexToSting(StringTool.hexStringToBytes( edInput.getText().toString())) + "\r\n");
                    } else {
                        Toast.makeText(getBaseContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //??????????????????
    private void updataSendBt() {
        if ( uartNfcDevice.serialManager.isOpen() ) {
            btOpen.setText("????????????");
            Toast.makeText(getBaseContext(), "??????????????????", Toast.LENGTH_SHORT).show();
        }
        else {
            btOpen.setText("????????????");
            Toast.makeText(getBaseContext(), "??????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    //?????????????????????
    synchronized void refreshLogView(String msg){
        final String theMsg = msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgTextView.append(theMsg);
            }
        });
    }

    private void logViewln(String string) {
        final String msg = string;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (msg == null) {
                    msgTextView.setText("");
                    return;
                }

                if (msgTextView.length() > 1000) {
                    msgTextView.setText("");
                }
                msgTextView.append(msg + "\r\n");
                int offset = msgTextView.getLineCount() * msgTextView.getLineHeight();
                if(offset > msgTextView.getHeight()){
                    msgTextView.scrollTo(0,offset-msgTextView.getHeight());
                }
            }
        });
    }

    private void showIDMsg(IDCardData msg) {
        final IDCardData idCardData = msg ;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgTextView.setText("???????????????????????????:" + (time_end - time_start) + "ms\r\n" + idCardData.toString() + "\r\n");

                SpannableString ss = new SpannableString(msgTextView.getText().toString()+"[smile]");
                //??????????????????????????????
                Drawable d = new BitmapDrawable(idCardData.PhotoBmp);//Drawable.createFromPath("mnt/sdcard/photo.bmp");
                if (d != null) {
                    //????????????
                    d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                    //?????????????????????????????????????????????
                    ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
                    //????????????
                    ss.setSpan(span, msgTextView.getText().length(),msgTextView.getText().length()+"[smile]".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    msgTextView.setText(ss);
                    //msgTextView.setText("\r\n");
                    //Log.d(TAG, idCardData.PhotoBmp);
                }
            }
        });
    }

    private void showMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgTextView.setText(msg);
            }
        });
    }

    //???????????????
    private void showReadWriteDialog(String msg, int rate) {
        final int theRate = rate;
        final String theMsg = msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((theRate == 0) || (theRate == 100)) {
                    readWriteDialog.dismiss();
                    readWriteDialog.setProgress(0);
                } else {
                    readWriteDialog.setMessage(theMsg);
                    readWriteDialog.setProgress(theRate);
                    if (!readWriteDialog.isShowing()) {
                        readWriteDialog.show();
                    }
                }
            }
        });
    }

    //???????????????
    private void hidDialog() {
        //?????????????????????
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (readWriteDialog.isShowing()) {
                    readWriteDialog.dismiss();
                }
                readWriteDialog.setProgress(0);
            }
        });
    }
}
