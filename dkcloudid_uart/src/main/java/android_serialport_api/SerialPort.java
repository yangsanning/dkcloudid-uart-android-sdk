package android_serialport_api;
/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {

    public SerialPort(String filePath, int baudRate, int flag) throws SecurityException, IOException {
        this(new File(filePath), baudRate, flag);
    }

    // 修改为安全性串口工具类: 2021/1/7
    public SerialPort(File file, int baudRate, int flag) throws SecurityException, IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath() + " not found port err");
        }
        if (!file.canRead() || !file.canWrite()) {
            try {
                Process process = Runtime.getRuntime().exec("su");
                String s = String.format("chmod 666 %s\nexit\n", file.getAbsolutePath());
                OutputStream outputStream = process.getOutputStream();
                outputStream.write(s.getBytes());
                outputStream.flush();
                outputStream.close();
                sleep(500); //目的为了不block main thread
                if (/*process.waitFor() != 0 ||*/ !file.canRead() || !file.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                throw new SecurityException();
            }
        }

        mFd = open(file.getAbsolutePath(), baudRate, flag);
        if (mFd == null) {
            Log.e("SerialPort", "native open returns null");
            throw new IOException();
        } else {
            mFileInputStream = new FileInputStream(mFd);
            mFileOutputStream = new FileOutputStream(mFd);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static native FileDescriptor open(String s, int i, int j);

    public native void close();

    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    private static final String TAG = "SerialPort";
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    static {
        System.loadLibrary("serial_port");
    }

    /**************************************modify start********************************************/
    public void closeStream() {
        try {
            if (null != mFileOutputStream) {
                mFileOutputStream.close();
                mFileOutputStream = null;
            }
            if (null != mFileInputStream) {
                mFileInputStream.close();
                mFileInputStream = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**************************************modify end********************************************/
}