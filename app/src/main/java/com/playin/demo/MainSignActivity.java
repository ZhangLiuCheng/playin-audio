package com.playin.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.playin.capture.SocketConnect;
import com.playin.util.LogUtil;

import java.util.Arrays;

public class MainSignActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_PERMISSIONS_AUDIO = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sign);

        findViewById(R.id.startBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.startBtn) {
            requestPermission();
        }
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

            /*
            Visualizer mVisualizer = new Visualizer(0); // get output audio stream
            mVisualizer.setEnabled(false);
            mVisualizer.setCaptureSize(1024);
            mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    LogUtil.e("-----  " + Arrays.toString(waveform));
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {

                }
            }, Visualizer.getMaxCaptureRate(), true, false); // waveform not freq data
             */
        }
    }

    private void startRecord() {
//        int frequency = 44100;
//        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
//        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
//        int audioSource = MediaRecorder.AudioSource.REMOTE_SUBMIX;
//        final int minBufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
//        final AudioRecord audioRecord = new AudioRecord(audioSource, frequency,
//                channelConfiguration, audioEncoding, minBufferSize);
//        audioRecord.startRecording();

        final AudioRecord audioRecord = findAudioRecord();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    final byte[] buffer = new byte[1024];

                    while (true) {
                        final int readCount = audioRecord.read(buffer, 0, 1024);
                        LogUtil.e(Arrays.toString(buffer));
                    }
                }
            }
        }).start();
    }

    public AudioRecord findAudioRecord() {
        int[] mSampleRates = new int[] { 8000, 11025, 22050, 32000, 44100 };
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[]{AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_FLOAT}) {
                for (short channelConfig : new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO}) {
                    try {
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
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
