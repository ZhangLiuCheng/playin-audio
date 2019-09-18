package com.playin.capture;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.IBinder;

import java.util.Arrays;
import java.util.List;

public class ProjectionService extends Service implements Runnable {

    private final IBinder mBinder = new LocalBinder();

    private final int sampleRateInHz = 44100;
    private final int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private int mBufferSizeInBytes;
    private AudioRecord mAudioRecord;
    private Thread mCurThread;

    private boolean mRecordState;
    private boolean mLogcatState;


    public interface ProjectionStateListener {
        void onStateChanged(boolean recordState, boolean logcatState);
    }

    private ProjectionStateListener mListener;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.e("ProjectionService ----> onCreate");
        startForeground(1, CommonUtil.getNotification(getApplicationContext()),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);

        SocketConnect.getInstance().startServer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.e("ProjectionService ----> onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SocketConnect.getInstance().stopServer();
        LogUtil.e("ProjectionService ----> onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void run() {
        final byte[] buffer = new byte[mBufferSizeInBytes];

        while (mRecordState) {
            if (mAudioRecord != null) {
                final int readCount = mAudioRecord.read(buffer, 0, mBufferSizeInBytes);
                if (mLogcatState) {
                    LogUtil.e(Arrays.toString(buffer));
                }
                SocketConnect.getInstance().sendData(buffer);
            }
        }
    }

    public class LocalBinder extends Binder {
        public ProjectionService getService() {
            return ProjectionService.this;
        }
    }

    public void setListener(ProjectionStateListener listener) {
        this.mListener = listener;
    }

    public boolean isRecording() {
        return mRecordState;
    }

    public boolean isLogcat() {
        return mLogcatState;
    }

    public void setLogcat(boolean logcat) {
        this.mLogcatState = logcat;
        if (null != mListener) mListener.onStateChanged(mRecordState, mLogcatState);
    }

    public void startRecord(MediaProjection mediaProjection) {
        stopRecord();
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        LogUtil.e("ProjectionService ----> BufferSizeInBytes: " + mBufferSizeInBytes);

        mAudioRecord = createAudioRecoder(mediaProjection);
        mAudioRecord.startRecording();
        mRecordState = true;
        mCurThread = new Thread(this);
        mCurThread.start();
        if (null != mListener) mListener.onStateChanged(mRecordState, mLogcatState);
    }

    public void stopRecord() {
        mRecordState = false;
        if (null != mCurThread) {
            mCurThread.interrupt();
        }
        if (null != mListener) mListener.onStateChanged(mRecordState, mLogcatState);
    }

    private AudioRecord createAudioRecoder(MediaProjection mediaProjection) {
        List<Integer> uids = CommonUtil.getAppUid(getApplicationContext());
        AudioPlaybackCaptureConfiguration.Builder builder = new AudioPlaybackCaptureConfiguration.Builder(mediaProjection);
        for (Integer uid : uids) {
            builder.addMatchingUid(uid);
            builder.addMatchingUsage(AudioAttributes.USAGE_UNKNOWN);
            builder.addMatchingUsage(AudioAttributes.USAGE_GAME);
            builder.addMatchingUsage(AudioAttributes.USAGE_MEDIA);
        }
        AudioRecord audioRecord = new AudioRecord.Builder()
                .setAudioPlaybackCaptureConfig(builder.build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRateInHz)
                        .setChannelMask(channelConfig)
                        .build())
                .setBufferSizeInBytes(mBufferSizeInBytes)
                .build();
        return audioRecord;
    }
}
