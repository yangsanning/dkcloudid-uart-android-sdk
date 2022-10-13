package com.dk.uartnfc.DeviceManager;

import android.util.Log;

import com.dk.uartnfc.Exception.CardNoResponseException;
import com.dk.uartnfc.UartManager.DKMessageDef;

import java.util.Arrays;

public class Command {
    public static final String TAG = "Command";

    public static final byte FRAME_START_CODE              = (byte)0xAA;           /*帧头定义*/
    public static final byte SAM_V_FRAME_START_CODE        = (byte)0xBB;           /*扩展通讯协议帧头定义*/
    
    /*******************************************************************************
     *                                 通讯命令定义                                 *
     *******************************************************************************/
    public static final byte GET_UID                     = (byte)0x01;   /*获取UID*/
    public static final byte GET_PICC_TYPE               = (byte)0x02;   /*获取卡片类型*/
    public static final byte M1_SAVE_KEY_A               = (byte)0x03;   /*向模块写入需要验证的密钥(A密钥)*/
    public static final byte M1_READ_BLOCK               = (byte)0x04;   /*M1卡读块*/
    public static final byte M1_WRITE_BLOCK              = (byte)0x05;   /*M1卡写块*/
    public static final byte M1_VALUE_INIT               = (byte)0x06;   /*M1卡增减值初始化*/
    public static final byte M1_VALUE_ADD                = (byte)0x07;   /*M1卡增值*/
    public static final byte M1_VALUE_SUB                = (byte)0x08;   /*M1卡减值*/
    public static final byte UL_READ_BLOCK               = (byte)0x09;   /*UL卡读块*/
    public static final byte UL_WRITE_BLOCK              = (byte)0x0A;   /*UL卡写块*/
    public static final byte M1_SAVE_KEY_B               = (byte)0x0B;   /*向模块写入需要验证的密钥(B密钥)*/
    public static final byte M1_SET_KEY_TYPE             = (byte)0x0C;   /*设置模块使用密钥的类型*/
    public static final byte B_READ_BLOCK                = (byte)0x0D;   /*ISO14443-B读块*/
    public static final byte B_WRITE_BLOCK               = (byte)0x0E;   /*ISO14443-B写块*/
    public static final byte M1_READ_ALL                 = (byte)0x0F;   /*M1卡读所有扇区/块*/
    public static final byte UL_READ_ALL                 = (byte)0x10;   /*UL卡读所有扇区/块*/
    public static final byte B_READ_ALL                  = (byte)0x11;   /*ISO14443-B卡读所有块*/
    public static final byte EEROM_READ                  = (byte)0x12;   /*读EEROM指令*/
    public static final byte EEROM_WRITE                 = (byte)0x13;   /*写EEROM指令*/
    public static final byte B_ACTIVIT                   = (byte)0x14;   /*ISO14443-B 卡激活指令*/
    public static final byte A_ACTIVIT                   = (byte)0x15;   /*ISO14443-A 卡片激活指令*/
    public static final byte B_PDU_COM                   = (byte)0x16;   /*ISO14443-B pdu指令接口*/
    public static final byte A_PDU_COM                   = (byte)0x17;   /*ISO14443-A pdu指令接口*/
    public static final byte PDU_DEACTIVIT               = (byte)0x18;   /*卡片断电指令*/
    public static final byte SAM_ACTIVIT                 = (byte)0x19;   /*SAM卡激活指令*/
    public static final byte SAM_PDU_COM                 = (byte)0x1a;   /*SAM卡PDU指令接口*/
    public static final byte SAM_DEACTIVIT               = (byte)0x1b;   /*SAM卡去激活指令*/
    public static final byte UL_FAST_READ                = (byte)0x1c;   /*UL卡FAST_READ指令*/
    public static final byte UL_FAST_WRITE               = (byte)0x1d;   /*UL卡快速写指令*/
    public static final byte GET_FINANCIAL_NUM           = (byte)0x1e;   /*获取银行卡卡号*/
    public static final byte B_GET_DN_NUM                = (byte)0x1f;   /*获取DN码*/
    public static final byte SRI512_READ_COM             = (byte)0x20;   /*SRI512读块*/
    public static final byte SRI512_WRITE_COM            = (byte)0x21;   /*SRI512写块*/
    public static final byte NFC_P2P_EXCHANGE            = (byte)0x22;   /*P2P数据交互通道*/
    public static final byte GET_ATR_CMD                 = (byte)0x23;   /*获取ATR*/
    public static final byte ENTER_UPDATE_CMD            = (byte)0x24;   /*进入固件升级模式*/
    public static final byte UL_EXCHANGE_CMD             = (byte)0x25;   /*UL卡指令交互*/ 
    public static final byte B_GET_SAMV_DN               = (byte)0x26;   /*获取云解码DN*/ 
    
    public static final byte EPASS_ACTIVIT               = (byte)0x30;   /*电子护照激活指令*/
    public static final byte EPASS_READ_FILE             = (byte)0x31;   /*电子护照读文件*/
    public static final byte SAM_V_INIT_COM              = (byte)0x32;   /*解析服务器开始请求解析命令*/
    public static final byte SAM_V_APDU_COM              = (byte)0x33;   /*解析服务器APDU命令*/
    public static final byte SAM_V_SUCCEED_COM           = (byte)0x34;   /*解析服务器解析完成命令*/
    public static final byte SAM_V_ERROR_COM             = (byte)0x35;   /*解析服务器解析错误命令*/
    public static final byte SAM_V_GET_AES_KEY_COM       = (byte)0x36;   /*获取明文解密的密钥*/
    public static final byte SAM_V_GET_DEVICE_ID         = (byte)0x37;   /*获取设备ID*/
    
    public static final byte AL_ISO15693_READ_SINGLE_BLOCK    = (byte)0x90;   /*ISO15693读单个块数据*/
    public static final byte AL_ISO15693_READ_MULTIPLE_BLOCK  = (byte)0x91;   /*ISO15693读单个块数据*/
    public static final byte AL_ISO15693_WRITE_SINGLE_BLOCK   = (byte)0x92;   /*ISO15693写单个块数据*/
    public static final byte AL_ISO15693_WRITE_MULTIPLE_BLOCK = (byte)0x93;   /*ISO15693写单个块数据*/
    public static final byte AL_ISO15693_LOCK_BLOCK           = (byte)0x94;   /*ISO15693锁块*/
    public static final byte AL_AUTO_SEARCH_CARD_SW           = (byte)0x95;   /*自动寻卡开关*/
    public static final byte AL_ISO15693_READ_ALL             = (byte)0x96;   /*ISO15693读所有块*/
    
    public static final byte CONFIG_UART_BAUD            = (byte)0xA0;   /*配置串口波特率指令*/
    public static final byte CONFIG_SYS_P                = (byte)0xA1;   /*配置系统参数*/
    public static final byte GET_CONFIG_SYS_P            = (byte)0xA2;   /*读取当前系统参数*/
    public static final byte SAVE_ACCOUNT_COM            = (byte)0xA3;   /*保存密钥信息*/
    public static final byte SAVE_ADDR_COM               = (byte)0xA4;   /*保存地址信息*/
    public static final byte GET_VERSION                 = (byte)0xB0;   /*获取软件版本号*/
    public static final byte GET_HW_VERSION              = (byte)0xB1;   /*获取硬件版本号*/
    public static final byte OPEN_BEEP_CMD               = (byte)0xB2;   /*开启蜂鸣器指令*/
    public static final byte GET_BEEP_CMD                = (byte)0xB3;   /*获取蜂鸣器*/
    public static final byte UART_RESEND                 = (byte)0xB4;   /*重发上一次的数据*/
    
    public static final byte EER_TAG_TYPE                = (byte)0xe0;   /*卡类型错误指令*/
    public static final byte EER_NO_FINE_TAG             = (byte)0xe1;   /*未寻到卡错误指令*/
    public static final byte EER_KEY_NO_AUTH             = (byte)0xe2;   /*密钥不匹配错误指令*/
    public static final byte EER_READ_BLOCK              = (byte)0xe3;   /*读块失败错误指令*/
    public static final byte EER_WRITE_BLOCK             = (byte)0xe4;   /*写块失败错误指令*/
    public static final byte EER_VALUE_INIT              = (byte)0xe5;   /*M1卡值初始化失败错误指令*/
    public static final byte EER_VALUE_ADD               = (byte)0xe6;   /*M1卡增值失败错误指令*/
    public static final byte EER_VALUE_SUB               = (byte)0xe7;   /*M1卡减值失败错误指令*/
    public static final byte EER_EEROM_OVER              = (byte)0xe8;   /*EEROM地址溢出错误*/
    public static final byte EER_EEROM_WRITE             = (byte)0xe9;   /*EEROM写失败错误*/
    public static final byte EER_NO_ACTIVIT              = (byte)0xea;   /*卡片未激活*/
    public static final byte EER_DETECT_MORE_CAR         = (byte)0xeb;   /*检测到多张卡*/
    public static final byte COM_ACK                     = (byte)0xfe;   /*ACK确认命令*/
    public static final byte COM_NACK                    = (byte)0xff;   /*NACK否认命令*/
    
    /*******************************************************************************
     *                                 卡片类型定义                                 *
     *******************************************************************************/
    public static final byte PICC_TYPE_M1                = (byte)1;      /*M1卡*/
    public static final byte PICC_TYPE_UL                = (byte)2;      /*UL卡*/
    public static final byte PICC_TYPE_B                 = (byte)3;      /*ISO14443-B卡*/
    public static final byte PICC_TYPE_ACPU              = (byte)4;      /*ISO14443-A CPU卡*/
    public static final byte PICC_TYPE_15693             = (byte)5;      /*ISO15693*/
    public static final byte PICC_TYPE_FELICA            = (byte)6;      /*FeliCa*/
    public static final byte PICC_TYPE_SRI512            = (byte)7;      /*SRI512*/
    public static final byte PICC_TYPE_COPY              = (byte)8;      /*复制卡*/
    public static final byte PICC_TYPE_DF                = (byte)9;      /*DESFire卡*/

    public final static byte  CARD_TYPE_NO_DEFINE = 0x00;               //卡片类型：未定义
    public final static byte  CARD_TYPE_ISO4443_A = PICC_TYPE_ACPU;     //卡片类型ISO14443-A
    public final static byte  CARD_TYPE_ISO4443_B = PICC_TYPE_B;        //卡片类型ISO14443-B
    public final static byte  CARD_TYPE_FELICA = PICC_TYPE_FELICA;      //卡片类型Felica
    public final static byte  CARD_TYPE_MIFARE = PICC_TYPE_M1;          //卡片类型Mifare卡
    public final static byte  CARD_TYPE_ISO15693 = PICC_TYPE_15693;     //卡片类型iso15693卡
    public final static byte  CARD_TYPE_ULTRALIGHT = PICC_TYPE_UL;      //RF_TYPE_MF
    public final static byte  CARD_TYPE_DESFire = PICC_TYPE_DF;         //DESFire卡
    public final static byte  CARD_TYPE_SAMV_ID = (byte)0xa3;           //身份证

    public static byte[] getCmdBytes(byte cmd, byte[] data) {
        byte[] bytes = new byte[data.length + 3];
        int cmdLen = data.length + 1;
        bytes[0] = FRAME_START_CODE;
        bytes[1] = (byte)(cmdLen & 0x00ff);
        bytes[2] = cmd;
        System.arraycopy(data, 0, bytes, 3, data.length);

        return bytes;
    }

    public static byte[] getCmdBytes(byte cmd, byte addr, byte[] data) {
        byte[] bytes = new byte[data.length + 4];
        int cmdLen = data.length + 2;
        bytes[0] = FRAME_START_CODE;
        bytes[1] = (byte)(cmdLen & 0x00ff);
        bytes[2] = cmd;
        bytes[3] = addr;
        System.arraycopy(data, 0, bytes, 4, data.length);

        return bytes;
    }

    public static byte[] getCmdBytes(byte cmd, byte addr, byte param, byte[] data) {
        byte[] bytes = new byte[data.length + 5];
        int cmdLen = data.length + 3;
        bytes[0] = FRAME_START_CODE;
        bytes[1] = (byte)(cmdLen & 0x00ff);
        bytes[2] = cmd;
        bytes[3] = addr;
        bytes[4] = param;
        System.arraycopy(data, 0, bytes, 5, data.length);

        return bytes;
    }

    public static byte[] getCmdBytes(byte cmd) {
        byte[] bytes = new byte[3];
        bytes[0] = FRAME_START_CODE;
        bytes[1] = (byte)0x01;
        bytes[2] = cmd;

        return bytes;
    }

    public static boolean verify_ack(byte[] data) {
        if ( (data == null) || (data.length != 3) ) {
            return false;
        }

        if ( data[0] != FRAME_START_CODE ) {
            return false;
        }

        if (data[2] != COM_ACK) {
            return false;
        }

        return true;
    }

    public static DKMessageDef getRspMsg(byte[] data) throws CardNoResponseException {
        //数据完整性判断
        if ( (data == null) || (data.length < 3) ) {
            throw new CardNoResponseException("数据为空或者数据长度过小");
        }

        //帧头判断
        if ( (data[0] != FRAME_START_CODE) && (data[0] != SAM_V_FRAME_START_CODE) ) {
            throw new CardNoResponseException("帧头错误");
        }

        DKMessageDef dkMessageDef = new DKMessageDef(false);
        int index = 0;
        dkMessageDef.start = data[index++];

        if (dkMessageDef.start == FRAME_START_CODE) {
            dkMessageDef.len = data[index++] & 0xff;
            if ( (dkMessageDef.len + 2) != data.length ) {
                throw new CardNoResponseException("长度字节错误");
            }
        }
        else if (dkMessageDef.start == SAM_V_FRAME_START_CODE) {
            dkMessageDef.len = ((data[index] & 0xff) << 8) | (data[index + 1] & 0xff);
            index += 2;
            if ( (dkMessageDef.len + 4) != data.length ) {
                throw new CardNoResponseException("长度字节错误");
            }

            //和校验
            byte bcc_sum = 0;
            for ( int i=0; i<data.length - 1; i++ ) {
                bcc_sum ^= data[i];
            }
            if ( bcc_sum != data[data.length - 1] ) {
                throw new CardNoResponseException( "和校验失败" );
            }

            dkMessageDef.bcc = data[data.length - 1];
        }

        dkMessageDef.command = data[index++];
        if (dkMessageDef.len == 1) {
            dkMessageDef.dataLen = 0;
            return dkMessageDef;
        }

        if (dkMessageDef.start == FRAME_START_CODE) {
            dkMessageDef.data = Arrays.copyOfRange( data, index, data.length );
        }
        else if (dkMessageDef.start == SAM_V_FRAME_START_CODE) {
            dkMessageDef.data = Arrays.copyOfRange( data, index, data.length - 1 );
        }

        dkMessageDef.dataLen = dkMessageDef.data.length;

        return dkMessageDef;
    }
}
