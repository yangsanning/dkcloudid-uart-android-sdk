package com.dk.uartnfc.Card;

import com.dk.uartnfc.DeviceManager.Command;
import com.dk.uartnfc.DeviceManager.DeviceManager;
import com.dk.uartnfc.Exception.CardNoResponseException;
import com.dk.uartnfc.Exception.DeviceNoResponseException;
import com.dk.uartnfc.UartManager.DKMessageDef;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by Administrator on 2016/9/21.
 */
public class Iso15693Card extends Card{
    public Iso15693Card(DeviceManager deviceManager) {
        super(deviceManager);
    }

    public Iso15693Card(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }

    /**
     * ISO15693锁住一个块，同步阻塞方式
     * @param addr        要锁的块的起始地址
     * @return            true:写入成功   false：写入失败
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean lockBlock(byte addr) throws CardNoResponseException {
        try {
            return Command.verify_ack( deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.AL_ISO15693_LOCK_BLOCK, new byte[] {addr}), 500) );
        } catch (DeviceNoResponseException e) {
            e.printStackTrace();
            throw new CardNoResponseException("读卡失败，请不要移动卡片");
        }
    }

    /**
     * ISO15693读单个块数据，同步阻塞方式
     * @param addr     要读的地址
     * @return         读取到的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] read(int addr) throws CardNoResponseException {
        return read((byte)addr);
    }

    /**
     * ISO15693读单个块数据，同步阻塞方式
     * @param addr     要读的地址
     * @return         读取到的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] read(byte addr) throws CardNoResponseException {
        try {
            byte[] rsp = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.AL_ISO15693_READ_SINGLE_BLOCK, new byte[] {addr}), 500);
            DKMessageDef msg = Command.getRspMsg(rsp);
            if ( (msg.data == null) || (msg.data.length < 5) || (msg.command != Command.AL_ISO15693_READ_SINGLE_BLOCK) ) {
                return null;
            }

            return Arrays.copyOfRange( msg.data, 1, msg.data.length );
        } catch (DeviceNoResponseException e) {
            e.printStackTrace();
            throw new CardNoResponseException("读卡失败，请不要移动卡片");
        }
    }

    /**
     * ISO15693读多个块数据指令，同步阻塞方式
     * @param addr     要读的块的起始地址
     * @param number   要读块的数量,必须大于0
     * @return         读取到的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] ReadMultiple(int addr, int number) throws CardNoResponseException {
        return ReadMultiple((byte)addr, (byte)number);
    }

    /**
     * ISO15693读多个块数据指令，同步阻塞方式
     * @param addr     要读的块的起始地址
     * @param number   要读块的数量,必须大于0
     * @return         读取到的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] ReadMultiple(byte addr, byte number) throws CardNoResponseException {
        try {
            byte[] rsp = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.AL_ISO15693_READ_MULTIPLE_BLOCK, addr, new byte[] {number}), 500);
            DKMessageDef msg = Command.getRspMsg(rsp);
            if ( (msg.data == null) || (msg.data.length < 5) || (msg.command != Command.AL_ISO15693_READ_MULTIPLE_BLOCK) ) {
                return null;
            }

            return Arrays.copyOfRange( msg.data, 1, msg.data.length );
        } catch (DeviceNoResponseException e) {
            e.printStackTrace();
            throw new CardNoResponseException("读卡失败，请不要移动卡片");
        }
    }

    /**
     * ISO15693写一个块，同步阻塞方式
     * @param addr        要写的块的地址
     * @param writeData   要写的数据，必须4个字节
     * @return            true:写入成功   false：写入失败
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean write(int addr, byte writeData[]) throws CardNoResponseException {
        return write((byte)addr, writeData);
    }

    /**
     * ISO15693写一个块，同步阻塞方式
     * @param addr        要写的块的地址
     * @param writeData   要写的数据，必须4个字节
     * @return            true:写入成功   false：写入失败
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean write(byte addr, byte writeData[]) throws CardNoResponseException {
        try {
            byte[] rsp = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.AL_ISO15693_WRITE_SINGLE_BLOCK, addr, writeData), 500);
            return Command.verify_ack(rsp);
        } catch (DeviceNoResponseException e) {
            e.printStackTrace();
            throw new CardNoResponseException("读卡失败，请不要移动卡片");
        }
    }

    /**
     * ISO15693写多个块，同步阻塞方式
     * @param addr        要写的块的地址
     * @param number      要写的块数量,必须大于0
     * @param writeData   要写的数据，必须4个字节
     * @return            true:写入成功   false：写入失败
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public boolean writeMultiple(byte addr, byte number, byte writeData[]) throws CardNoResponseException {
        try {
            byte[] rsp = deviceManager.serialManager.sendWithReturn(Command.getCmdBytes(Command.AL_ISO15693_WRITE_MULTIPLE_BLOCK, addr, number, writeData), 500);
            return Command.verify_ack(rsp);
        } catch (DeviceNoResponseException e) {
            e.printStackTrace();
            throw new CardNoResponseException("读卡失败，请不要移动卡片");
        }
    }

    /**
     * ISO15693指令通道，同步阻塞方式
     * @param data     发送的数据
     * @return         返回的数据
     * @throws CardNoResponseException
     *                  操作无响应时会抛出异常
     */
    public byte[] transceive(byte[] data) throws CardNoResponseException {
        return null;   //接口开发中
    }
}
