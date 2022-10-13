package com.dk.uartnfc.Card;

import com.dk.uartnfc.DKCloudID.IDCardData;

/**
 * Created by lochy on 16/1/19.
 */
public abstract class DeviceManagerCallback {
    //非接寻卡回调
    public void onReceiveRfnSearchCard(boolean blnIsSus, int cardType, byte[] bytCardSn, byte[] bytCarATS) {}

    //身份证开始请求云解析回调
    public void onReceiveSamVIdStart(byte[] initData) {}

    //身份证云解析进度回调
    public void onReceiveSamVIdSchedule(int rate) {}

    //身份证云解析异常回调
    public void onReceiveSamVIdException(String msg) {}

    //身份证云解析明文结果回调
    public void onReceiveIDCardData(IDCardData idCardData) {}

    //卡片离开回调
    public void onReceiveCardLeave() {}

    //收到ACK响应回调
    public void onReceiveACK() {}

    //收到NACK响应回调
    public void onReceiveNACK() {}
}
