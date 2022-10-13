package com.dk.uartnfc.Card;

import com.dk.uartnfc.DeviceManager.Command;
import com.dk.uartnfc.DeviceManager.DeviceManager;
import com.dk.uartnfc.Exception.CardNoResponseException;
import com.dk.uartnfc.Exception.DeviceNoResponseException;
import com.dk.uartnfc.UartManager.DKMessageDef;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/9/19.
 */
public class Ultralight extends Card{
    final static byte  UL_GET_VERSION_CMD = (byte)0x60;
    final static byte  UL_READ_CMD = (byte)0x30;
    final static byte  UL_FAST_READ_CMD = (byte)0x3A;
    final static byte  UL_WRITE_CMD = (byte)0xA2;
    final static byte  UL_READ_CNT_CMD = (byte)0x39;
    final static byte  UL_PWD_AUTH_CMD = (byte)0x1B;

    public final static int   UL_MAX_FAST_READ_BLOCK_NUM = 4;
    public final static int  LONG_READ_MAX_NUMBER = 0x30;

    public Ultralight(DeviceManager deviceManager) {
        super(deviceManager);
    }

    public Ultralight(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }

    /**
     * 读取卡片版本，同步阻塞方式
     * @return         返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] getVersion() throws CardNoResponseException {
        return null;
    }

    /**
     * 读单个块数据，同步阻塞方式
     * @param addr     要读的地址
     * @return         读取到的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] read(byte addr) throws CardNoResponseException {
//        try {
//            byte[] rsp = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.UL_READ_BLOCK, new byte[] {addr}), 500);
//            DKMessageDef msg = Command.getRspMsg(rsp);
//            if ( (msg.data == null) || (msg.data.length < 5) || (msg.command != Command.UL_READ_BLOCK) ) {
//                return null;
//            }
//
//            return Arrays.copyOfRange( msg.data, 1, msg.data.length );
//        } catch (DeviceNoResponseException e) {
//            e.printStackTrace();
//            throw new CardNoResponseException("读卡失败，请不要移动卡片");
//        }

        byte[] cmdByte = {UL_READ_CMD, addr};
        byte[] rsp = transceive(cmdByte);
        if ( rsp.length != 16 ) {
            throw new CardNoResponseException("读卡命令执行失败");
        }

        return rsp;
    }

    /**
     * 快速读，同步阻塞方式
     * @param startAddress     要读起始地址
     * @param endAddress       要读的结束地址
     * @return                 读取到的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] fastRead(byte startAddress, byte endAddress) throws CardNoResponseException {
        return null;   //接口开发中
    }

    /**
     * 快速读，同步阻塞方式
     * @param startAddress     要读起始地址
     * @param number           要读的块数量（一个块4 byte）， 0 < number < 0x3f
     * @return                 读取到的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] longReadSingle(byte startAddress, int number) throws CardNoResponseException {
        int endAddress = (startAddress & 0xff) + number;

        try {
            byte[] rsp = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.UL_FAST_READ, startAddress, new byte[] {(byte)endAddress}), 500);
            DKMessageDef msg = Command.getRspMsg(rsp);
            if ( (msg.data == null) || (msg.data.length < 5) || (msg.command != Command.UL_FAST_READ) ) {
                throw new CardNoResponseException("longReadSingle读卡失败，请不要移动卡片");
            }

            return Arrays.copyOfRange( msg.data, 1, msg.data.length );
        } catch (DeviceNoResponseException e) {
            e.printStackTrace();
            throw new CardNoResponseException("读卡失败，请不要移动卡片");
        }
    }

    /**
     * 写一个块，同步阻塞方式
     * @param addr        要写的块的地址
     * @param writeData   要写的数据，必须4个字节
     * @return            true:写入成功   false：写入失败
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean write(byte addr, byte writeData[]) throws CardNoResponseException {
        if ( writeData.length != 4 ) {
            throw new CardNoResponseException("写入长度必须等于16");
        }

        try {
            byte[] rsp = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.UL_WRITE_BLOCK, addr, writeData), 500);
            return Command.verify_ack(rsp);
        } catch (DeviceNoResponseException e) {
            e.printStackTrace();
            throw new CardNoResponseException("读卡失败，请不要移动卡片");
        }
    }

    /**
     * 写一个块，同步阻塞方式
     * @param startAddress   要写的块的起始地址
     * @param data        要写的数据，必须小于0x3f字节
     * @return            true:写入成功   false：写入失败
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean longWriteSingle(byte startAddress, byte[] data) throws CardNoResponseException {
        try {
            byte[] rsp = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.UL_FAST_WRITE, startAddress, data), 500);
            return Command.verify_ack(rsp);
        } catch (DeviceNoResponseException e) {
            e.printStackTrace();
            throw new CardNoResponseException("读卡失败，请不要移动卡片");
        }
    }

    /**
     * 读次数，同步阻塞方式
     * @return            返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] readCnt() throws CardNoResponseException {
        return null;   //接口开发中
    }

    /**
     * 验证密码，同步阻塞方式
     * @param password    要验证的密码，4 Bytes
     * @return            true:验证成功  false:验证失败
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean pwdAuth(byte[] password) throws CardNoResponseException {
        if (password.length != 4) {
            throw new CardNoResponseException("密钥必须4字节");
        }

        byte[] cmdByte = {UL_PWD_AUTH_CMD, password[0], password[1], password[2], password[3]};
        byte[] rsp = transceive(cmdByte);
        if (rsp.length == 2) {
            return true;
        }

        return false;
    }

    /**
     * 指令传输通道，同步阻塞方式
     * @param data     发送的数据
     * @return         返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] transceive(byte[] data) throws CardNoResponseException {
        if ( (data == null) || (data.length == 0) ) {
            throw new CardNoResponseException("数据不能为null");
        }

        try {
            byte[] nfc_return_bytes = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.UL_EXCHANGE_CMD, data), 500);
            DKMessageDef msg = Command.getRspMsg(nfc_return_bytes);
            if ( (msg.data == null) || (msg.command != Command.UL_EXCHANGE_CMD) ) {
                return null;
            }
            return msg.data;
        } catch (DeviceNoResponseException e) {
            e.printStackTrace();
            throw new CardNoResponseException("读取数据失败，请不要移动卡片");
        }
    }
}
