package cn.synzbtech.ota.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import org.xutils.http.RequestParams;
import org.xutils.x;

import cn.synzbtech.ota.core.Api;
import cn.synzbtech.ota.core.DeviceInfoWrapper;
import cn.synzbtech.ota.core.entity.DeviceEntity;
import cn.synzbtech.ota.core.entity.DeviceInfo;
import cn.synzbtech.ota.core.entity.ResultWrapper;
import cn.synzbtech.ota.core.network.HyyHttpClient;
import cn.synzbtech.ota.utils.DeviceUtils;
import okhttp3.MediaType;

/**
 * 监控检测服务。保持和服务器的链接，并每个60秒上报设备信息。需要上报的信息参考{@see DeviceInfo}
 * @author hyy owen (2106755124@qq.com)
 */
public class MonitoringService extends Service {

    private static final String TAG = "MonitoringService";

    private static final int HEARTBEAT_INTERVAL = 1000 * 60;

    private MonitoringThread monitoringThread;

    private class MonitoringThread extends Thread {
        @Override
        public void run() {
            while(!isInterrupted()) {

                DeviceInfo deviceInfo = DeviceUtils.collectDeviceInfo();
                Log.d(TAG, "device info >>> ");
                Log.d(TAG, deviceInfo.toString());
                DeviceInfoWrapper.deviceInfo = deviceInfo;

                pushDeviceInfoToServer(deviceInfo);

                try {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
    private void pushDeviceInfoToServer(DeviceInfo deviceInfo) {
        String cpuSerialNo = DeviceUtils.getCPUSerial();
        if(TextUtils.isEmpty(cpuSerialNo)) {
            //alertError("无法获取设备CPU序列号，程序将退出", true);
            return;
        }

        String wifiMac = DeviceUtils.getMac(this).trim();

        if(TextUtils.isEmpty(wifiMac)) {
            //alertError("无法获取设备MAC地址，程序将退出", true);
            return;
        }

        Log.d("OTA", "cpu serial no >> " + cpuSerialNo);
        Log.d("OTA", "mac >> " + wifiMac);

        String mac = DeviceUtils.yfgetEthMacAddress().trim();

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        DeviceEntity deviceEntity = new DeviceEntity();
        deviceEntity.setCpu(cpuSerialNo);
        deviceEntity.setMac(mac);
        deviceEntity.setWifiMac(wifiMac);
        deviceEntity.setDetail(DeviceInfoWrapper.deviceInfo);

        String responseJSON = HyyHttpClient.getInstance().post(HyyHttpClient.URI.PUSH_DEVICE_INFO, deviceEntity, true);
        if(responseJSON!=null) {
            Log.d(TAG, "push device info success >>> ");
            Log.d(TAG, responseJSON);
        } else{
            Log.d(TAG, "push device info failed >>>");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(monitoringThread==null || !monitoringThread.isAlive()) {
            monitoringThread = new MonitoringThread();
            monitoringThread.start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(monitoringThread!=null){
            monitoringThread.interrupt();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
