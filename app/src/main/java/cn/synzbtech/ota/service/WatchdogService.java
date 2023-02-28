package cn.synzbtech.ota.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

import cn.synzbtech.ota.ui.MainActivity;
import cn.synzbtech.ota.R;
import cn.synzbtech.ota.utils.DeviceUtils;

import org.apache.log4j.PropertyConfigurator;

public class WatchdogService extends Service {
    private static final String TAG ="WATCHDOG SERVICE";
    private Timer timer = null;
    private  int count=0;
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind !!");
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate !!");
        PropertyConfigurator.configure("config/log4j.properties");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        String id = "1";
        String name = "channel_name_1";
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.setSound(null, null);
            notificationManager.createNotificationChannel(mChannel);
            notification = new Notification.Builder(this)
                    .setChannelId(id)
                    .setContentTitle("OTA Service")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_launcher_background).build();
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle("OTA Service")
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_launcher_background).setColor(Color.parseColor("#0972EE"));
            notification = notificationBuilder.build();
        }
        startForeground(1, notification);


        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                count++;
                String cpuSerial = DeviceUtils.getCPUSerial();
                //ZKUtils.keepAlive(cpuSerial);
                Log.d(TAG, "timer 200ms !!count="+count);
            }
        }, 1000,1000);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart !!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy !!");
    }
}