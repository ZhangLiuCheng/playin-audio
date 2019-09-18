package com.playin.demo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.playin.capture.ProjectionService;

public class MainCaptureActivity extends AppCompatActivity implements View.OnClickListener, ProjectionService.ProjectionStateListener {

    private static final int REQUEST_PERMISSIONS_AUDIO = 1001;
    private static final int REQUEST_MEDIA_PROJECTION = 1002;

    private MediaProjectionManager mMediaProjectionManager;
    private ProjectionService mProjectionService;
    private boolean mBound = false;

    private Button mRecordBtn;
    private Button mLogcatBtn;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ProjectionService.LocalBinder binder = (ProjectionService.LocalBinder) service;
            mProjectionService = binder.getService();
            mProjectionService.setListener(MainCaptureActivity.this);
            mBound = true;
            Log.e("TAG", "[playin] MainCaptureActivity ----> onServiceConnected");
            initView();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Log.e("TAG", "[playin] MainCaptureActivity ----> onServiceDisconnected");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_capture);

        Intent intent = new Intent(this, ProjectionService.class);
        startForegroundService(intent);

        initView();
    }

    private void initView() {
        mRecordBtn = findViewById(R.id.recordBtn);
        mLogcatBtn = findViewById(R.id.logcatBtn);
        mRecordBtn.setOnClickListener(this);
        mLogcatBtn.setOnClickListener(this);
        if (mBound) {
            mRecordBtn.setText(mProjectionService.isRecording() ? "关闭录音" :  "打开录音");
            mLogcatBtn.setText(mProjectionService.isLogcat() ? "关闭日志" :  "打开日志");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("TAG", "[playin] MainCaptureActivity ----> onStart");

        Intent intent = new Intent(this, ProjectionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("TAG", "[playin] MainCaptureActivity ----> onStop");
        if (mBound) {
            mProjectionService.setListener(null);
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onClick(View v) {
        if (!mBound || mProjectionService == null) return;
        if (v.getId() == R.id.recordBtn) {
            if (mProjectionService.isRecording()) {
                mProjectionService.stopRecord();
            } else {
                requestPermission();
            }
        } else if (v.getId() == R.id.logcatBtn) {
            mProjectionService.setLogcat(!mProjectionService.isLogcat());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestMediaProjection();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                        Toast.makeText(MainCaptureActivity.this, "需要到设置里面授权录音", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
                if (mBound) mProjectionService.startRecord(mediaProjection);
            } else {
                Toast.makeText(MainCaptureActivity.this, "需要授权", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_PERMISSIONS_AUDIO);
        } else {
            requestMediaProjection();
        }
    }

    private void requestMediaProjection() {
        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent intent = projectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, REQUEST_MEDIA_PROJECTION);
        mMediaProjectionManager = projectionManager;
    }

    @Override
    public void onStateChanged(boolean recordState, boolean logcatState) {
        initView();
    }
}
