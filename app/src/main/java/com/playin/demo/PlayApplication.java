package com.playin.demo;

import android.app.Application;
import android.content.Context;

import com.playin.hook.PlayInject;
import com.playin.util.LogUtil;

import java.io.File;
import java.io.IOException;

public class PlayApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        PlayInject.init(this);

        File file = getFilesDir();
        File f = new File(file, "helix_crush.pcm");
        LogUtil.e("======>  " + f.getAbsolutePath());
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
