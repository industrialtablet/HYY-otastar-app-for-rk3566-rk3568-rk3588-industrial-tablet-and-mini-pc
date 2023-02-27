package cn.synzbtech.ota.utils;

import android.content.Context;
import android.content.SharedPreferences;

import cn.synzbtech.ota.OtaApplication;

public class PreferenceUtils {

    private static PreferenceUtils mInstance ;
    private static SharedPreferences sharedPreferences;
    public static synchronized PreferenceUtils getInstance() {
        if(mInstance==null) {
            mInstance = new PreferenceUtils();
            sharedPreferences = OtaApplication.getApp().getSharedPreferences("ota_pref", Context.MODE_PRIVATE);
        }
        return mInstance;
    }

    public void setApkUpgradeState(int state) {
        sharedPreferences.edit().putInt("apk_upgrade_state", state).apply();
    }
    public int getApkUpgradeState() {
        return sharedPreferences.getInt("apk_upgrade_state", 0);
    }

    public void setApkUpgradeUrl(String url) {
        sharedPreferences.edit().putString("apk_upgrade_url", url).apply();
    }
    public String getApkUpgradeUrl() {
        return sharedPreferences.getString("apk_upgrade_url", "");
    }


    public void setApkSaveStorage(String storage) {
        sharedPreferences.edit().putString("apk_save_storage", storage).apply();
    }
    public String getApkSaveStorage() {
        return sharedPreferences.getString("apk_save_storage", "");
    }

    public void setDeviceMac(String mac) {
        sharedPreferences.edit().putString("device_mac", mac).apply();
    }
    public String getDeviceMac() {
        return sharedPreferences.getString("device_mac", "");
    }

    public void setPackUpgradeState(int state) {
        sharedPreferences.edit().putInt("pack_upgrade_state", state).apply();
    }
    public int getPackUpgradeState() {
        return sharedPreferences.getInt("pack_upgrade_state", 0);
    }

    public void setPackUpgradeUrl(String url) {
        sharedPreferences.edit().putString("pack_upgrade_url", url).apply();
    }
    public String getPackUpgradeUrl() {
        return sharedPreferences.getString("pack_upgrade_url", "");
    }

    public void setPackSaveStorage(String storage) {
        sharedPreferences.edit().putString("pack_save_storage", storage).apply();
    }
    public String getPackSaveStorage() {
        return sharedPreferences.getString("pack_save_storage", "");
    }
}
