package com.dk.uartnfc.OTA;

/**
 * Created by asirotinkin on 11.11.2014.
 */
public interface CRC {
    int getCRCLength();

    long calcCRC(byte[] block);
}
