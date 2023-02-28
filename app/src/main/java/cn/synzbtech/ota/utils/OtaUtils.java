package cn.synzbtech.ota.utils;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.Activity;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.synzbtech.ota.service.MonitoringService;

public class OtaUtils {


    //安装更新包
    public static void install(Activity context, String apkPath) {
        File apkFile = new File(apkPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 将此段代码移到此，可正常安装
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri apkUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            apkUri = FileProvider.getUriForFile(context, "cn.synzbtech.ota.fileprovider", apkFile);
        } else {
            apkUri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        context.startActivityForResult(intent, 1234);
    }

    //ota升级 RK3288
    public static void otaUpgradeRK3288(Context context, String packPath) {
        Intent intent = new Intent("yf.action.SYSTEM_UPDATE");
        intent.putExtra("path", packPath);
        context.sendBroadcast(intent);
    }

    //ota升级 YF3566
    public static void otaUpgradeYF3566(Context context, String packPath) {
        //Log.e("smallstar", "otaUpgradeYF3566");
        Intent intent = new Intent("yf.action.SYSTEM_UPDATE");
        intent.putExtra("path", packPath);
        intent.setPackage("android.rockchip.update.service");
        context.sendBroadcast(intent);
    }

    /**
     * 判断服务是否开启
     *
     * @return
     */
    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (("").equals(ServiceName) || ServiceName == null)
            return false;
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);

        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断某个界面是否在前台
     *
     * @param context  Context
     * @param className 界面的类名
     * @return 是否在前台显示
     */
    public static boolean isActivityForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className))
            return false;
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(30);

        for (ActivityManager.RunningTaskInfo taskInfo : list) {
            if (taskInfo.topActivity.getShortClassName().contains(className)) { // 说明它已经启动了
                return true;
            }
        }
        return false;
    }

    public static void startService(Context context, Class<? extends Service> serviceClass) {
        if(OtaUtils.isServiceRunning(context, serviceClass.getName())) {
            Log.d("smallstar", serviceClass.getName()+" is already running .");
            return ;
        }
        Intent intent = new Intent(context, serviceClass);
        context.startService(intent);
    }

}
