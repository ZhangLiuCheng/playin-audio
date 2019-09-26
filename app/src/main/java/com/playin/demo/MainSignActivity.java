package com.playin.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.playin.util.LogUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

public class MainSignActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_PERMISSIONS_AUDIO = 1001;

    private final Handler handler = new Handler();
    private TextView mInfoTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sign);

        findViewById(R.id.playBtn).setOnClickListener(this);
        findViewById(R.id.startBtn).setOnClickListener(this);
        mInfoTv = findViewById(R.id.data);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.playBtn) {
            audioTrackPaly();
//            mediaPlayerPlay();
        } else if (view.getId() == R.id.startBtn) {
            requestPermission();
        }
    }

    private void audioTrackPaly() {
        int streamType = 3;
        int sampleRateInHz = 24000;
        int channelConfig = 3;
        int audioFormat = 2;
        int bufferSizeInBytes = 8480;
        final AudioTrack audioTrack = new AudioTrack(streamType, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes, AudioTrack.MODE_STREAM);
        audioTrack.play();

        final byte[] buf = new byte[bufferSizeInBytes * 2];

        new Thread(new Runnable() {
            InputStream is = null;
            int count;
            @Override
            public void run() {
                try {
                    is = getAssets().open("timi-24000-2-16bit.pcm");
                    count = is.read(buf);
                    while (count > 0) {
//                        LogUtil.e("读取到音频数据: " + Arrays.toString(buf));
                        audioTrack.write(buf, 0, count);
                        count = is.read(buf);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        is.close();
                        audioTrack.stop();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecord();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                        Toast.makeText(MainSignActivity.this, "需要到设置里面授权录音", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_PERMISSIONS_AUDIO);
        } else {
            startRecord();
        }
    }

    private void startRecord() {

        final AudioRecord audioRecord = findAudioRecord();
        int bufferSize = 1024;

//        8000  ---  channelConfig 16 --- audioFormat 3  ---- bufferSize 640
//        int frequence = 8000;
//        int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
//        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
//        int bufferSize = AudioRecord.getMinBufferSize(frequence, channelConfig, audioEncoding);
//        final AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.REMOTE_SUBMIX, frequence, channelConfig, audioEncoding, bufferSize);

        if (audioRecord == null || audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Toast.makeText(this, "audioRecord为null", Toast.LENGTH_SHORT).show();
            return;
        }
        final short[] buffer = new short[bufferSize];
        audioRecord.startRecording();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    while (true) {
                        final int readCount = audioRecord.read(buffer, 0, buffer.length);
                        LogUtil.e("录制声音数据长度: " + readCount);
                        if (readCount == 0) {
                            continue;
                        }
                        LogUtil.e("录制声音数据: " + System.currentTimeMillis() + Arrays.toString(buffer));
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mInfoTv.setText("录制声音数据: " + System.currentTimeMillis() + Arrays.toString(buffer));
                            }
                        });
                    }
                }
            }
        }).start();
    }

    public AudioRecord findAudioRecord() {
        int[] mSampleRates = new int[] { 8000, 11025, 22050, 32000, 44100 };
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[]{AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_FLOAT}) {
                for (short channelConfig : new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO}) {
                    try {
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            LogUtil.e("rate: " + rate + "  ---  channelConfig " + channelConfig + " --- audioFormat " + audioFormat + "  ---- bufferSize " + bufferSize);
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.REMOTE_SUBMIX, rate, channelConfig, audioFormat, bufferSize);
                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                                return recorder;
                            } else
                                recorder.release();
                        }
                    } catch (Exception e) {
                        Log.e("SoundMeter", rate + " Exception, keep trying.", e);
                    }
                }
            }
        }
        return null;
    }
}
