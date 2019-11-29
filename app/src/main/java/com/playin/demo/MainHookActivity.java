package com.playin.demo;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.playin.hook.AutoContorl;
import com.playin.util.LogUtil;
import com.playin.util.SocketConnect;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

public class MainHookActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "AUDIO_HOOK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_hook);

        findViewById(R.id.btnAudioTrack).setOnClickListener(this);
        findViewById(R.id.btnMediaPlayer).setOnClickListener(this);
        findViewById(R.id.btnUpdate).setOnClickListener(this);

        AutoContorl.start(this);

//        SocketConnect.getInstance().startServer();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnAudioTrack) {
            audioTrackPaly();
        } else if (view.getId() == R.id.btnMediaPlayer) {
            mediaPlayerPlay();
        } else if (view.getId() == R.id.btnUpdate) {
            update();
        }
    }

    private void update() {
        LogUtil.e("-----------1111--------------");
        new Thread(new Runnable() {
            @Override
            public void run() {
//                cmd("pm install /sdcard/DancingRoad.apk");
//                cmd("pm install /sdcard/app-debug.apk");

                silentInstall("/sdcard/app-debug.apk");
            }
        }).start();
        LogUtil.e("-----------2222--------------");
    }

    private static void cmd(String cmd) {
        try {
            Process sh = Runtime.getRuntime().exec("su", null,null);
            OutputStream os = sh.getOutputStream();
            os.write(("/system/bin/" + cmd).getBytes("ASCII"));
            os.flush();
            os.close();
            sh.waitFor();
            LogUtil.e("-----------安装成功--------------");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("========>  失败");
        }
    }

    public static boolean silentInstall(String apkPath) {
        boolean result = false;
        DataOutputStream dataOutputStream = null;
        BufferedReader errorStream = null;
        BufferedReader successStream = null;
        Process process = null;
        try {
            // 申请 su 权限
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            // 执行 pm install 命令
            String command = "pm install -r " + apkPath + "\n";
            dataOutputStream.write(command.getBytes(Charset.forName("UTF-8")));
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
            errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorMsg = new StringBuilder();
            String line;
            while ((line = errorStream.readLine()) != null) {
                errorMsg.append(line);
            }
            LogUtil.e("silent install error message: " + errorMsg);
            StringBuilder successMsg = new StringBuilder();
            successStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // 读取命令执行结果
            while ((line = successStream.readLine()) != null) {
                successMsg.append(line);
            }
            LogUtil.e("silent install success message: " + successMsg);
            // 如果执行结果中包含 Failure 字样就认为是操作失败，否则就认为安装成功
            if (!(errorMsg.toString().contains("Failure") || successMsg.toString().contains("Failure"))) {
                result = true;
            }
        } catch (Exception e) {
            LogUtil.e(e.toString());
        } finally {
            try {
                if (process != null) {
                    process.destroy();
                }
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
                if (successStream != null) {
                    successStream.close();
                }
            } catch (Exception e) {
                // ignored
            }
        }
        return result;
    }

    public static boolean isRoot() {
        return new File("/system/bin/su").exists() || new File("/system/xbin/su").exists();
    }

    private void audioTrackPaly() {

        int streamType = 3;
        int sampleRateInHz = 24000;
        int channelConfig = 3;
        int audioFormat = 2;
        int bufferSizeInBytes = 8480;

//        streamType: 3  sampleRateInHz: 44100  channelConfig: 12  audioFormat: 2   bufferSizeInBytes: 14144


//        int streamType = 3;
//        int sampleRateInHz = 44100;
//        int channelConfig = 12;
//        int audioFormat = 2;
//        int bufferSizeInBytes = 14144;

        final AudioTrack audioTrack = new AudioTrack(streamType, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes, AudioTrack.MODE_STREAM);
        audioTrack.play();

        final byte[] buf = new byte[bufferSizeInBytes * 2];

        new Thread(new Runnable() {
            InputStream is;
            int count;

            @Override
            public void run() {
                try {
                    /*
//                    is = getAssets().open("timi-24000-2-16bit.pcm");
                    is = getAssets().open("helix_crush.pcm");
//                    is = getAssets().open("tiles_hop.pcm"); // 没有声音
//                    is = getAssets().open("magic_tiles3.pcm");
                    count = is.read(buf);
                    while (count > 0) {
                        LogUtil.e("读取到音频数据: " + Arrays.toString(buf));
                        audioTrack.write(buf, 0, count);
                        count = is.read(buf);
                    }

                    */


                    LogUtil.e("开始连接");


//                    Socket localSocket = new Socket("172.20.10.3", 55555);
                    Socket localSocket = new Socket("192.168.10.7", 55555);
//                    Socket localSocket = new Socket("54.152.254.32", 55555);

                    localSocket.setSoTimeout(0);
                    localSocket.setReceiveBufferSize(1024*30);
                    localSocket.setTcpNoDelay(true);

//                    LocalSocket localSocket = new LocalSocket();
//                    LocalSocketAddress address = new LocalSocketAddress("com.playin.audio.localsocket");
//                    localSocket.connect(address);

                    LogUtil.e("连接成功");
                    InputStream is = localSocket.getInputStream();
                    DataInputStream dis = new DataInputStream(is);


//                    ServerSocket server = new ServerSocket(55555);
//                    Socket localSocket = server.accept();
//                    LogUtil.e("连接成功");
//                    InputStream is = localSocket.getInputStream();
//                    DataInputStream dis = new DataInputStream(is);

                    byte[] lenBuf = new byte[4];
                    while (true) {
                        dis.readFully(lenBuf);
                        int length = bytesToInt(lenBuf);
                        int type = dis.read();
                        byte[] contentBuf = new byte[length - 1];
                        dis.readFully(contentBuf);
                        if (type == 0) {
                            LogUtil.e("读取到音频参数: " + new String(contentBuf));
                        } else {
                            LogUtil.e("读取到音频数据:  " + contentBuf.length + "  -----  " + Arrays.toString(contentBuf));
                            audioTrack.write(contentBuf, 0, contentBuf.length);
                        }
                    }

                } catch (Exception ex) {
                    LogUtil.e("连接异常1------->： " + ex);
                    ex.printStackTrace();
                } finally {
//                    try {
//                        is.close();
//                        audioTrack.stop();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }).start();
    }

    private int bytesToInt(byte[] bytes){
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private void mediaPlayerPlay() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource("http://www.ytmp3.cn/down/57799.mp3");
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
