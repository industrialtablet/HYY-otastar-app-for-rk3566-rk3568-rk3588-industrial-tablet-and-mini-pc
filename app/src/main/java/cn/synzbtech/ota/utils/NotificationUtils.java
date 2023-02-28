package cn.synzbtech.ota.utils;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cn.synzbtech.ota.OtaApplication;
import cn.synzbtech.ota.R;
import cn.synzbtech.ota.ui.MainActivity;

public class NotificationUtils {

    private static NotificationUtils handle;

    private String channel_id="myChannelId";
    private NotificationManager notificationManager;

    public static synchronized NotificationUtils getInstance() {
        if(handle==null)  {
            handle = new NotificationUtils();
            handle.createNotificationChannel(OtaApplication.getApp());
        }
        return handle;
    }

    private void createNotificationChannel(Context context){
        //Android8.0(API26)以上需要调用下列方法，但低版本由于支持库旧，不支持调用
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            String channel_name = "myChannelName";
            NotificationChannel channel = new NotificationChannel(channel_id, channel_name, importance);
            String description = "this is myChannel's description";
            channel.setDescription(description);
            notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }else{
            notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

    }


    public void cancelNotification() {
        notificationManager.cancel(10086);
    }

    public void sendNotification(String title, String text, String url){

        Intent intent = new Intent(OtaApplication.getApp(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(OtaApplication.getApp(), 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(OtaApplication.getApp(), channel_id)
                .setContentTitle(title)
                .setContentText(text)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(OtaApplication.getApp().getResources(), R.mipmap.ic_launcher))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .build();

        notificationManager.notify(10086,notification);
    }


    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isNotificationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ///< 8.0手机以上
            if (((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).getImportance() == NotificationManager.IMPORTANCE_NONE) {
                return false;
            }
        }

        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;

        Class appOpsClass = null;
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                    String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);

            int value = (Integer) opPostNotificationValue.get(Integer.class);
            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 通知权限申请
     * @param context
     */
    public static void requestNotify(Context context) {
        /**
         * 跳到通知栏设置界面
         * @param context
         */
        Intent localIntent = new Intent();
        ///< 直接跳转到应用通知设置的代码
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            localIntent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            localIntent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        }
        else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            localIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            localIntent.putExtra("app_package", context.getPackageName());
            localIntent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            localIntent.addCategory(Intent.CATEGORY_DEFAULT);
            localIntent.setData(Uri.parse("package:" + context.getPackageName()));
        } else {
            ///< 4.4以下没有从app跳转到应用通知设置页面的Action，可考虑跳转到应用详情页面,
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 9) {
                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
            } else if (Build.VERSION.SDK_INT <= 8) {
                localIntent.setAction(Intent.ACTION_VIEW);
                localIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
                localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
            }
        }
        context.startActivity(localIntent);
    }


}
