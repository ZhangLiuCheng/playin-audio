package com.playin.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.playin.demo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CommonUtil {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Notification getNotification(Context context) {
        String CHANNEL_ONE_ID = "com.primedu.cn";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }
        Notification notification = new Notification.Builder(context).setChannelId(CHANNEL_ONE_ID)
                .setTicker("Nature")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("PlayInAudio")
                .setContentText("获取游戏声音")
                .getNotification();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
    }

    public static List<Integer> getAppUid(Context context) {
        List<Integer> uids = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                // 系统应用
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(packageInfo.packageName, 0);
                    uids.add(ai.uid);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return uids;
    }


    public static File copyAssetsFile(Context context, String fileName, File path) {
        InputStream is = null;
        FileOutputStream os = null;
        if (!path.exists()) path.mkdirs();
        File apkFile = new File(path + File.separator + "audio_hook.apk");
        if (apkFile.exists()) {
            return apkFile;
        }
        try {
            is = context.getAssets().open(fileName);
            apkFile.createNewFile();
            LogUtil.e("开始拷贝apk");
            os = new FileOutputStream(apkFile);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = is.read(buf)) > 0) {
                os.write(buf, 0, i);
            }
            LogUtil.e("拷贝apk完毕");
        } catch (Exception e) {
            apkFile.delete();
            return null;
        } finally {
            try {
                if (is != null) is.close();
                if (os != null) os.close();
            } catch (Exception ex) {
            }
        }
        return apkFile;
    }
}
