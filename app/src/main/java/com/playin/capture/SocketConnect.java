package com.playin.capture;

import android.net.LocalServerSocket;
import android.net.LocalSocket;

import com.playin.util.LogUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketConnect implements Runnable {

    private LinkedBlockingQueue<byte[]> voiceQueue = new LinkedBlockingQueue(10);

    private static SocketConnect sInstance = new SocketConnect();
    public static SocketConnect getInstance() {
        return sInstance;
    }

    private List<Thread> mWriteThreads = new ArrayList<>();
    private Thread mCurThread;
    private boolean mRunning;


    private SocketConnect() {
    }

    public void sendData(byte[] buf) {
        voiceQueue.offer(buf);
    }

    public void startServer() {
        LogUtil.e("SocketConnect ----> startServer");

        mRunning = true;
        mCurThread = new Thread(this);
        mCurThread.start();
    }

    public void stopServer() {
        LogUtil.e("SocketConnect ----> stopServer");
        mRunning = false;
        mCurThread.interrupt();
        for (Thread thread: mWriteThreads) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        try {
            LogUtil.e("SocketConnect ----> 启动服务");
            LocalServerSocket server = new LocalServerSocket("com.playin.audio.localsocket");
            while (mRunning) {
                LocalSocket localSocket = server.accept();
                LogUtil.e("SocketConnect ----> client连接成功");
                Thread thread = new WriteThread(localSocket);
                thread.start();
                mWriteThreads.add(thread);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class WriteThread extends Thread {

        private LocalSocket localSocket;

        public WriteThread(LocalSocket localSocket) {
            this.localSocket = localSocket;
        }

        @Override
        public void run() {
            OutputStream os = null;
            try {
                os = localSocket.getOutputStream();
                while (!isInterrupted()) {
                    os.write(voiceQueue.take());
                }
            } catch (Exception e) {
                LogUtil.e("SocketConnect::WriteThread Exception: " + e);
            } finally {
                try {
                    os.close();
                    localSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
