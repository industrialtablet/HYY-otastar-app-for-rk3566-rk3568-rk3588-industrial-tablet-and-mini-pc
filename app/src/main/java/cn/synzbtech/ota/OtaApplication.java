package cn.synzbtech.ota;

import android.app.Application;

import org.xutils.x;

import cn.synzbtech.ota.core.zookeeper.ZKUtils;
import cn.synzbtech.ota.utils.PreferenceUtils;

public class OtaApplication extends Application {

    private static OtaApplication mThis;

    public volatile static int apkUpgradeState = 0;

    public volatile static int packUpgradeState = 0;

    public static OtaApplication getApp() {
        return mThis;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mThis = this;
        apkUpgradeState = PreferenceUtils.getInstance().getApkUpgradeState();
        x.Ext.init(this);
        new Thread(()->{
            ZKUtils.getOnlineZK();
            ZKUtils.getUpgradeZk();
        });
    }

    @Override
    public void onTerminate() {
        ZKUtils.closeZk();
        super.onTerminate();
    }
}
