package com.dk.uartnfc.OTA;

import android.os.Build;
import android.util.Log;

import com.dk.uartnfc.OTA.eventbus.PostEventBus;
import com.dk.uartnfc.UartManager.SerialManager;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * YModem.<br/>
 * Block 0 contain minimal file information (only filename)<br/>
 * <p>
 * Created by Anton Sirotinkin (aesirot@mail.ru), Moscow 2014<br/>
 * I hope you will find this program useful.<br/>
 * You are free to use/modify the code for any purpose, but please leave a reference to me.<br/>
 */
public class YModem {
    private Modem modem;

    private onReceiveScheduleListener mOnReceiveScheduleListener;

    public YModem(SerialManager manager) {
        this.modem = new Modem(manager);
    }

    //进度回调
    public interface onReceiveScheduleListener{
        void onReceiveSchedule(int rate);
    }

    /**
     * Send a file.<br/>
     * <p>
     * This method support correct thread interruption, when thread is interrupted "cancel of transmission" will be send.
     * So you can move long transmission to other thread and interrupt it according to your algorithm.
     *
     * @param file
     * @throws IOException
     */
    public void send(File file, onReceiveScheduleListener l) throws IOException {
        mOnReceiveScheduleListener = l;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try (DataInputStream dataStream = new DataInputStream(new FileInputStream(file))) {
                Timer timer = new Timer(Modem.WAIT_FOR_RECEIVER_TIMEOUT).start();
                boolean useCRC16 = modem.waitReceiverRequest(timer);
                CRC crc;
                if (useCRC16) {
                    crc = new CRC16();
                }
                else {
                    crc = new CRC8();
                }

                if ( mOnReceiveScheduleListener != null ) {
                    mOnReceiveScheduleListener.onReceiveSchedule(1);
                }

                //send block 0
                String fileNameString = file.getName() + (char) 0 + /*getFileSizes(file)*/ file.length() + ' ';
                int packCount = (int) (Math.ceil((double) file.length() / 1024));
                Log.i("gh0st", "fileNameString:" + fileNameString + "packCount:" + (packCount + 1));
                Constants.sCountPro = packCount + 1;
                byte[] fileNameBytes = Arrays.copyOf(fileNameString.getBytes(), 128);
                modem.sendBlock(0, Arrays.copyOf(fileNameBytes, 128), 128, crc);

                modem.waitReceiverRequest(timer);
                //send data
                byte[] block = new byte[1024];
                //modem.sendDataBlocks(dataStream, 1, crc, block);
                int dataLength;
                int blockNumber = 1;
                while ((dataLength = dataStream.read(block)) != -1) {
                    //PostEventBus.post("send data ..." + dataLength);
                    PostEventBus.post("...");
                    modem.sendBlock(blockNumber++, block, dataLength, crc);

                    int progress = (int)(((float)block.length * blockNumber) / file.length() * 100);
                    if ( mOnReceiveScheduleListener != null ) {
                        mOnReceiveScheduleListener.onReceiveSchedule(progress);
                    }
                }
                PostEventBus.post("\nwrite file finish ...");

                modem.sendEOT();
                modem.sendEOT();
                modem.sendBlock(0, new byte[128], 128, crc);
                if ( mOnReceiveScheduleListener != null ) {
                    mOnReceiveScheduleListener.onReceiveSchedule(100);
                }
            }
        }
    }

    /**
     * Send files in batch mode.<br/>
     * <p>
     * This method support correct thread interruption, when thread is interrupted "cancel of transmission" will be send.
     * So you can move long transmission to other thread and interrupt it according to your algorithm.
     *
     * @param files
     * @throws IOException
     */
    public void batchSend(File... files) throws IOException {
        for (File file : files) {
            send(file, null);
        }
        sendBatchStop();
    }

    private void sendBatchStop() throws IOException {
        Timer timer = new Timer(Modem.WAIT_FOR_RECEIVER_TIMEOUT).start();
        boolean useCRC16 = modem.waitReceiverRequest(timer);
        CRC crc;
        if (useCRC16)
            crc = new CRC16();
        else
            crc = new CRC8();

        //send block 0
        byte[] bytes = new byte[128];
        modem.sendBlock(0, bytes, bytes.length, crc);
    }
}
