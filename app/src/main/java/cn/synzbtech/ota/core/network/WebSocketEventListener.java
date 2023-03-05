package cn.synzbtech.ota.core.network;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.gnepux.wsgo.EventListener;
import com.gnepux.wsgo.WsGo;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import cn.synzbtech.ota.AppConfig;
import cn.synzbtech.ota.OtaApplication;
import cn.synzbtech.ota.core.Api;
import cn.synzbtech.ota.core.Constants;
import cn.synzbtech.ota.core.DeviceInfoWrapper;
import cn.synzbtech.ota.core.entity.ApkUpgradeMainEvent;
import cn.synzbtech.ota.core.entity.ApkVersion;
import cn.synzbtech.ota.core.entity.OtaVersion;
import cn.synzbtech.ota.core.entity.PackUpgradeMainEvent;
import cn.synzbtech.ota.core.entity.ResultWrapper;
import cn.synzbtech.ota.core.entity.UpdateVersion;
import cn.synzbtech.ota.service.CheckUpdateService;
import cn.synzbtech.ota.ui.MainActivity;
import cn.synzbtech.ota.utils.NotificationUtils;
import cn.synzbtech.ota.utils.OtaUtils;
import cn.synzbtech.ota.utils.PreferenceUtils;
import lombok.Setter;

public class WebSocketEventListener implements EventListener {


    private Context mContext;

    private UpdateCheckWorker updateCheckWorker;

    public WebSocketEventListener(Context context){
        this.mContext = context;
        this.updateCheckWorker = new UpdateCheckWorker();
    }
    private static final String TAG = "WebSocketEventListener";
    @Override
    public void onConnect() {
        Log.d(TAG, "onConnect");
    }
    @Override
    public void onDisConnect(Throwable throwable) {
        Log.d(TAG, "onDisConnect");
    }

    @Override
    public void onClose(int code, String reason) {
        Log.d(TAG, "onClose,code="+code+",reason:"+reason);
    }

    @Override
    public void onMessage(String text) {
        Log.d(TAG, "onMessage : " + text);
        MessageBody messageBody = JSON.parseObject(text, MessageBody.class);
        switch (messageBody.getCommand()) {

            case Constants.COMMEND_UPDATE_OTA_NOTIFY: {
                String url = ack(Constants.COMMEND_UPDATE_OTA_ACK, messageBody.getData());
                if(url!=null) {
                    this.updateCheckWorker.doGetOtaUpgradeVersion(url);
                }
                break;
            }
            case Constants.COMMEND_UPDATE_APK_NOTIFY: {
                String url = ack(Constants.COMMEND_UPDATE_APK_ACK, messageBody.getData());

                if(url!=null) {
                    this.updateCheckWorker.doGetApkUpgradeVersion(url);
                }
                break;
            }
        }
    }

    private String ack(int command, Object msgObj){
        JSONObject obj = (JSONObject) msgObj;
        String url = obj.getString("url");
        Long upgradeRunId = obj.getLong("upgradeRunId");
        String appid = obj.getString("appid");
        String localAppId = TextUtils.isEmpty(AppConfig.APPID)?HyyHttpClient.APPID:AppConfig.APPID;
        if(!appid.equalsIgnoreCase(localAppId)){
            Log.d(TAG, "The appid does not match ");
            return null;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("upgradeRunId", upgradeRunId);
        MessageBody ack = new MessageBody(command, dataMap);
        WsGo.getInstance().send(JSON.toJSONString(ack));
        return url;
    }

    @Setter
    private class UpdateCheckWorker {

        private int apkState = 0; // app apk 升级状态

        private int packState = 0; // 系统 ota 升级状态

        /**
         * If the server cancels the apk upgrade, you need to use this method to check again and again
         *
         * @return
         */
        private ApkVersion getApkVersion() {

            if (DeviceInfoWrapper.deviceInfo == null) {
                Log.d(TAG, "device info is null,can not get apk version");
                return null;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("wifiMac", DeviceInfoWrapper.deviceInfo.getWifiMacAddress());
            String responseJSON = HyyHttpClient.getInstance().post(HyyHttpClient.URI.GET_APK_VERSION, params, false);
            if (StringUtils.isEmpty(responseJSON)) {
                Log.d(TAG, "apk >>> request apk version failed");
                return null;
            }

            ResultWrapper<ApkVersion> resultWrapper = JSON.parseObject(responseJSON, new TypeReference<ResultWrapper<ApkVersion>>() {
            }.getType());
            Log.d(TAG, "apk >>> response code " + resultWrapper.getCode() + " message " + resultWrapper.getMsg());
            return resultWrapper.getData();
        }

        /**
         * If the server cancels the apk upgrade, you need to use this method to check again and again
         *
         * @return
         */
        private OtaVersion getOtaVersion() {
            if (DeviceInfoWrapper.deviceInfo == null) {
                Log.d(TAG, "device info is null,can not get apk version");

                return null;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("wifiMac", DeviceInfoWrapper.deviceInfo.getWifiMacAddress());
            String responseJSON = HyyHttpClient.getInstance().post(HyyHttpClient.URI.GET_OAT_VERSION, params, false);
            if (StringUtils.isEmpty(responseJSON)) {
                Log.d(TAG, "ota >>> request ota version failed");
                return null;
            }

            ResultWrapper<OtaVersion> resultWrapper = JSON.parseObject(responseJSON, new TypeReference<ResultWrapper<OtaVersion>>() {
            }.getType());
            Log.d(TAG, "ota >>> response code " + resultWrapper.getCode() + " message " + resultWrapper.getMsg());
            return resultWrapper.getData();
        }

        /**
         * Merge apk and ota upgrade check requests. Reduce the number of requests to the server.
         * However, checking for cancellation still requires a separate request for apk {@see getApkVersion} or ota {@see getOtaVersion}
         *
         * @return
         */
        private UpdateVersion getUpdateVersion() {
            if (DeviceInfoWrapper.deviceInfo == null) {
                Log.d(TAG, "device info is null,can not get apk version");
                return null;
            }
            Map<String, Object> params = new HashMap<>();
            params.put("wifiMac", DeviceInfoWrapper.deviceInfo.getWifiMacAddress());
            String responseJSON = HyyHttpClient.getInstance().post(HyyHttpClient.URI.GET_UPDATE_VERSION, params, false);
            if (StringUtils.isEmpty(responseJSON)) {
                Log.d(TAG, "check apk and ota >>> request ota version failed");
                return null;
            }

            ResultWrapper<UpdateVersion> resultWrapper = JSON.parseObject(responseJSON, new TypeReference<ResultWrapper<UpdateVersion>>() {
            }.getType());
            Log.d(TAG, "check apk and ota >>> response code " + resultWrapper);
            return resultWrapper.getData();
        }

        /**
         * if device receiver ota update notification, run getOtaVersion method to obtain download url again.
         * get url after use event bus send PackUpgradeMainEvent to MainActivity.
         */
        private void doGetOtaUpgradeVersion(String url){

            Log.d(TAG, "ota upgrade state : "+ OtaApplication.packUpgradeState);

            // if apk is update now. ota do not upgrade operation.
            if(OtaApplication.apkUpgradeState != Api.UpgradeState.CHECK) {
                return;
            }

            // Prevent receiving repeated ota update notifications
            if (OtaApplication.packUpgradeState != Api.UpgradeState.CHECK) {
                return;
            }

            //通知下载ota包
            OtaApplication.packUpgradeState = Api.UpgradeState.NEED_DOWNLOAD;
            Log.d(TAG, "pack upgrade url " + url);
            PreferenceUtils.getInstance().setPackUpgradeState(Api.UpgradeState.NEED_DOWNLOAD);
            // 设置OTA下载地址,格式：url,md5
            PreferenceUtils.getInstance().setPackUpgradeUrl(url);

            boolean mainForeground = OtaUtils.isActivityForeground(mContext, MainActivity.class.getSimpleName());
            if (mainForeground) {
                EventBus.getDefault().post(new PackUpgradeMainEvent(Api.DownloadState.READY));
            } else {
                NotificationUtils.getInstance().sendNotification("GO Update OTA", "There is a new OTA version, please go to upgrade", "");
            }
        }

        /**
         * if device receiver apk update notification, run getApk method to obtain download url again.
         * get url after use event bus send ApkUpgradeMainEvent to MainActivity.
         */
        private void doGetApkUpgradeVersion(String url){

            Log.d(TAG, "apk upgrade url " + url);

            // if ota is update now. apk do not upgrade operation.
            if(OtaApplication.packUpgradeState != Api.UpgradeState.CHECK) {
                return;
            }

            // Prevent receiving repeated apk update notifications
            if (OtaApplication.apkUpgradeState != Api.UpgradeState.CHECK) {
                return;
            }

            //通知下载 apk 包
            OtaApplication.apkUpgradeState = Api.UpgradeState.NEED_DOWNLOAD;
            PreferenceUtils.getInstance().setApkUpgradeState(Api.UpgradeState.NEED_DOWNLOAD);
            PreferenceUtils.getInstance().setApkUpgradeUrl(url);

            boolean mainForeground = OtaUtils.isActivityForeground(mContext, MainActivity.class.getSimpleName());
            if (mainForeground) {
                EventBus.getDefault().post(new ApkUpgradeMainEvent(Api.DownloadState.READY));
            } else {
                NotificationUtils.getInstance().sendNotification("GO Update APK", "There is a new APK version, please go to upgrade", "");
            }
        }


        @Deprecated
        private void doGetUpdateVersion() {
            Log.d(TAG, "start get upgrade. is apk upgrade " + OtaApplication.apkUpgradeState + ", is pack upgrade " + OtaApplication.packUpgradeState);

            if (OtaApplication.apkUpgradeState != Api.UpgradeState.CHECK) {
                if (OtaApplication.apkUpgradeState == Api.UpgradeState.NEED_DOWNLOAD) {
                    ApkVersion apkVersion = getApkVersion();
                    if (apkVersion == null) { // 取消了。通知现在停止，具体停止请查看 MainActivity packUpgradeEvent 方法为-2的分支
                        OtaApplication.apkUpgradeState = Api.UpgradeState.CHECK;
                        PreferenceUtils.getInstance().setApkUpgradeState(Api.UpgradeState.CHECK);
                        EventBus.getDefault().post(new ApkUpgradeMainEvent(Api.DownloadState.CANCELED));
                    }


                } else if (OtaApplication.apkUpgradeState >= Api.UpgradeState.DOWNLOAD_COMPLETE) {
                    apkState++;
                    if (apkState >= 3) {
                        OtaApplication.apkUpgradeState = Api.UpgradeState.CHECK;
                        PreferenceUtils.getInstance().setApkUpgradeState(Api.UpgradeState.CHECK);
                    }
                }
                return;
            }

            if (OtaApplication.packUpgradeState != Api.UpgradeState.CHECK) {

                //如果正在下载
                if (OtaApplication.packUpgradeState == Api.UpgradeState.NEED_DOWNLOAD) {
                    //获取是否取消了下载。
                    OtaVersion packVersion = getOtaVersion();
                    if (packVersion == null) {// 取消了。通知现在停止，具体停止请查看 MainActivity packUpgradeEvent 方法为-2的分支
                        OtaApplication.packUpgradeState = Api.UpgradeState.CHECK;
                        PreferenceUtils.getInstance().setPackUpgradeState(Api.UpgradeState.CHECK);
                        EventBus.getDefault().post(new PackUpgradeMainEvent(Api.DownloadState.CANCELED));
                    }

                } else if (OtaApplication.packUpgradeState >= Api.UpgradeState.DOWNLOAD_COMPLETE) {
                    packState++;
                }

                if (packState >= 3) {
                    OtaApplication.packUpgradeState = Api.UpgradeState.CHECK;
                    PreferenceUtils.getInstance().setPackUpgradeState(Api.UpgradeState.CHECK);
                }
                return;
            }

            UpdateVersion updateVersion = getUpdateVersion();
            if (updateVersion == null) {
                return;
            }
            ApkVersion apkVersion = updateVersion.getApkVersion();
            if (apkVersion != null) {
                OtaApplication.apkUpgradeState = Api.UpgradeState.NEED_DOWNLOAD;
                PreferenceUtils.getInstance().setApkUpgradeState(Api.UpgradeState.NEED_DOWNLOAD);
                PreferenceUtils.getInstance().setApkUpgradeUrl(apkVersion.getUrl());

                Log.d(TAG, "apk upgrade version " + apkVersion.getUrl());

                boolean mainForeground = OtaUtils.isActivityForeground(mContext, MainActivity.class.getSimpleName());
                if (mainForeground) {
                    EventBus.getDefault().post(new ApkUpgradeMainEvent());
                } else {
                    NotificationUtils.getInstance().sendNotification("升级通知", "有版本，请前往升级", "");
                }
            } else {
                Log.d(TAG, "No apk upgrade required");
            }

            if (OtaApplication.apkUpgradeState != Api.UpgradeState.CHECK) {
                return;
            }

            OtaVersion packVersion = updateVersion.getOtaVersion();
            if (packVersion != null) {
                //下载pack包
                OtaApplication.packUpgradeState = Api.UpgradeState.NEED_DOWNLOAD;
                Log.d(TAG, "pack upgrade version " + packVersion.getUrl());
                PreferenceUtils.getInstance().setPackUpgradeState(Api.UpgradeState.NEED_DOWNLOAD);
                // 设置OTA下载地址,格式：url,md5
                PreferenceUtils.getInstance().setPackUpgradeUrl(packVersion.getUrl() + "," + packVersion.getMd5());

                boolean mainForeground = OtaUtils.isActivityForeground(mContext, MainActivity.class.getSimpleName());
                if (mainForeground) {
                    EventBus.getDefault().post(new PackUpgradeMainEvent());
                } else {
                    NotificationUtils.getInstance().sendNotification("升级通知", "有新OTA版本，请前往升级", "");
                }
            } else {
                Log.d(TAG, "No ota upgrade required");
            }
        }
    }

    @Override
    public void onReconnect(long retryCount, long delayMillSec) {
        Log.d(TAG, "onReconnect, retryCount="+retryCount+",delayMillSec="+delayMillSec);
    }

    @Override
    public void onSend(String text, boolean success) {
        Log.d(TAG, "onSend, text:"+text+",success:"+success);
    }
}
