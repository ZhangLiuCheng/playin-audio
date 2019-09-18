package com.playin.demo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

public class MainHookActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "AUDIO_HOOK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_hook);

        findViewById(R.id.btnAudioTrack).setOnClickListener(this);
        findViewById(R.id.btnMediaPlayer).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnAudioTrack) {
            audioTrackPaly();
        } else if (view.getId() == R.id.btnMediaPlayer) {
            mediaPlayerPlay();
        }
    }

    private void audioTrackPaly() {
        int outputBufferSize = AudioTrack.getMinBufferSize(24000, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        final AudioTrack audioTrack = new AudioTrack(AudioManager.USE_DEFAULT_STREAM_TYPE, 24000,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, outputBufferSize, AudioTrack.MODE_STREAM);
        audioTrack.play();

        new Thread(new Runnable() {
            byte[] buf = new byte[1024];
            InputStream is;
            int count;

            @Override
            public void run() {
                try {
                    is = getAssets().open("timi-24000-2-16bit.pcm");
                    count = is.read(buf);
                    while (count > 0) {
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
}
