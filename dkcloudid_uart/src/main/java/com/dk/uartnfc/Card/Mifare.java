package com.dk.uartnfc.Card;

import com.dk.uartnfc.DeviceManager.Command;
import com.dk.uartnfc.DeviceManager.DeviceManager;
import com.dk.uartnfc.Exception.CardNoResponseException;
import com.dk.uartnfc.Exception.DeviceNoResponseException;
import com.dk.uartnfc.UartManager.DKMessageDef;

import java.util.Arrays;

/**
 * Created by Administrator on 2016/9/21.
 */
public class Mifare extends Card {
    //Mifare Key type
    public final static byte MIFARE_KEY_TYPE_A = 0x0A;
    public final static byte MIFARE_KEY_TYPE_B = 0x0B;

    public final static byte[] MIFARE_DEFAULT_KEY = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};

    public Mifare(DeviceManager deviceManager) {
        super(deviceManager);
    }

    public Mifare(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }

    /**
     * Mifare验证密钥，同步阻塞方式
     * @param bKeyType - 验证密码类型：MIFARE_KEY_TYPE_A 或者 MIFARE_KEY_TYPE_B
     * @param pKey - 验证用到的密钥，6个字节
     * @return         true：验证成功  false：验证失败
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean setKey(byte bKeyType, byte[] pKey) throws CardNoResponseException {
        if (bKeyType == MIFARE_KEY_TYPE_A) {
            try {
                byte[] rsp = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.M1_SAVE_KEY_A, pKey), 500);
                if ( !Command.verify_ack(rsp) ) {
                    return false;
                }

                rsp = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.M1_SET_KEY_TYPE, new byte[] {(byte)MIFARE_KEY_TYPE_A}), 500);
                return Command.verify_ack(rsp);
            } catch (DeviceNoResponseException e) {
                e.printStackTrace();
                throw new CardNoResponseException("读卡失败，请不要移动卡片");
            }
        }
        else {
            try {
                byte[] rsp = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.M1_SAVE_KEY_B, pKey), 500);
                if ( !Command.verify_ack(rsp) ) {
                    return false;
                }

                rsp = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.M1_SET_KEY_TYPE, new byte[] {(byte)MIFARE_KEY_TYPE_B}), 500);
                return Command.verify_ack(rsp);
            } catch (DeviceNoResponseException e) {
                e.printStackTrace();
                throw new CardNoResponseException("读卡失败，请不要移动卡片");
            }
        }
    }

    /**
     * Mifare卡读块，同步阻塞方式
     * @param addr     要读的地址
     * @return         读取到的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] read(int addr) throws CardNoResponseException {
        return read((byte)addr);
    }

    /**
     * Mifare卡读块，同步阻塞方式
     * @param addr     要读的地址
     * @return         读取到的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] read(byte addr) throws CardNoResponseException {
        try {
            byte[] rsp = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.M1_READ_BLOCK, new byte[] {addr}), 500);
            DKMessageDef msg = Command.getRspMsg(rsp);
            if ( (msg.data == null) || (msg.data.length < 16) || (msg.command != Command.M1_READ_BLOCK) ) {
                return null;
            }

            return Arrays.copyOfRange( msg.data, 1, msg.data.length );
        } catch (DeviceNoResponseException e) {
            e.printStackTrace();
            throw new CardNoResponseException("读卡失败，请不要移动卡片");
        }
    }

    /**
     * Mifare卡写块，同步阻塞方式
     * @param addr        要写的块的地址
     * @param writeData   要写的数据，必须16个字节
     * @return            true:写入成功   false：写入失败
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean write(int addr, byte writeData[]) throws CardNoResponseException {
        return write((byte)addr, writeData);
    }

    /**
     * Mifare卡写块，同步阻塞方式
     * @param addr        要写的块的地址
     * @param writeData   要写的数据，必须16个字节
     * @return            true:写入成功   false：写入失败
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean write(byte addr, byte writeData[]) throws CardNoResponseException {
        if ( writeData.length != 16 ) {
            throw new CardNoResponseException("写入长度必须等于16");
        }

        try {
            byte[] rsp = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.M1_WRITE_BLOCK, addr, writeData), 500);
            return Command.verify_ack(rsp);
        } catch (DeviceNoResponseException e) {
            e.printStackTrace();
            throw new CardNoResponseException("读卡失败，请不要移动卡片");
        }
    }
}
