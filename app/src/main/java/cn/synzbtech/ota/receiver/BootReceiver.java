package cn.synzbtech.ota.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import cn.synzbtech.ota.service.WatchdogService;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG ="WATCHDOG BROAD";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent bootServiceIntent = new Intent(context, WatchdogService.class);
            Log.d(TAG, "onReceive BOOT_COMPLETED!! ");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(bootServiceIntent);
            } else {
                context.startService(bootServiceIntent);
            }
            //context.startService(bootServiceIntent);//startForegroundService
        }
    }
}