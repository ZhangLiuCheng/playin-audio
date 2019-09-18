package com.playin.demo;

import android.app.Application;
import android.content.Context;

public class PlayApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        AudioHook.init(this);
    }
}
