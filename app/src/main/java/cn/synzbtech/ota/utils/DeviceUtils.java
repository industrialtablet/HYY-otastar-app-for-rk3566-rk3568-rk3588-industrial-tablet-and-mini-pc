package cn.synzbtech.ota.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.youngfeel.yf_rk356x_api.YF_RK356x_API_Manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import cn.synzbtech.ota.OtaApplication;
import cn.synzbtech.ota.core.entity.DeviceInfo;

public class DeviceUtils {


    private static YF_RK356x_API_Manager apiManager;

    public synchronized static YF_RK356x_API_Manager getApiManager (Context context) {
        if(apiManager==null) {
            apiManager = new YF_RK356x_API_Manager(context);
        }
        return apiManager;
    }

    /**
     * Android 6.0 之前（不包括6.0）获取mac地址
     * 必须的权限 <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
     * @param context * @return
     */
    public static String getMacDefault(Context context) {
        String mac = "";
        if (context == null) {
            return mac;
        }

        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        Log.d("smallstar", "getMacDefault : " + mac);
        return mac;
    }

/**
 * android 7.0及以上 （3）通过busybox获取本地存储的mac地址
 *
 */
    /**
     * 根据busybox获取本地Mac
     *
     * @return
     */
    public static String getLocalMacAddressFromBusybox() {

        String result = "";
        String Mac = "";
        result = callCmd("busybox ifconfig", "HWaddr");
        // 如果返回的result == null，则说明网络不可取
        if (result == null) {
            return "";
        }
        // 对该行数据进行解析
        // 例如：eth0 Link encap:Ethernet HWaddr 00:16:E8:3E:DF:67
        if (result.length() != 0 && result.contains("HWaddr") == true) {
            Mac = result.substring(result.indexOf("HWaddr") + 6,
                    result.length() - 1);
            result = Mac;
        }
        Log.d("smallstar", "getLocalMacAddressFromBusybox : " + result);
        return result;
    }

    public static String callCmd(String cmd, String filter) {
        String result = "";
        String line = "";
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStreamReader is = new InputStreamReader(proc.getInputStream());
            BufferedReader br = new BufferedReader(is);
            while ((line = br.readLine()) != null
                    && line.contains(filter) == false) {
                result += line;
            }
            result = line;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Android 6.0-Android 7.0 获取mac地址
     */
    public static String getMacAddress() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            while (null != str) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();//去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        Log.d("smallstar", "getMacAddress : " + macSerial);
        return macSerial;
    }

    /**
     * Android 7.0之后获取Mac地址
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET"></uses-permission>
     * @return
     */
    public static String getMacFromHardware() {
        String mac="";
        try {
            List<NetworkInterface> all =
                    Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0"))
                    continue;
                byte [] macBytes = nif.getHardwareAddress();
                if (macBytes == null) return "";
                StringBuilder res1 = new StringBuilder();
                for (Byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (!TextUtils.isEmpty(res1)) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                mac = res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("smallstar", "getMacFromHardware : " + mac);
        return mac;
    }

    /**
     * 获取mac地址（适配所有Android版本）
     * @return
     */
    public static String getMac(Context context) {
        String mac = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getMacDefault(context);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMacAddress();
        } else {
            mac = getLocalMacAddressFromBusybox();
        }
        if(TextUtils.isEmpty(mac)) {
            mac = getMacFromHardware();
        }
        Log.e("smallstar", "mac = " + mac);
        return mac==null?"":mac;
    }


    /**
     * 获取CPU序列号
     *
     * @return CPU序列号(16位) 读取失败为"0000000000000000"
     */
    public static String getCPUSerial() {
        String str = "", strCPU = "", cpuAddress = "0000000000000000";



        try {
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.e("smallstar", "Build.getSerial() = " + Build.getSerial());
                return Build.getSerial();
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e("smallstar", "Build.SERIAL = " + Build.SERIAL);
                return Build.SERIAL;
            }
            */
            // 读取CPU信息
            Process pp = Runtime. getRuntime().exec("cat /proc/cpuinfo");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            // 查找CPU序列号
            for ( int i = 1; i < 100; i++) {
                str = input.readLine();
                if (str != null) {
                    // 查找到序列号所在行
                    if (str.indexOf( "Serial") > -1) {
                        // 提取序列号
                        strCPU = str.substring(str.indexOf(":" ) + 1, str.length());
                        // 去空格
                        cpuAddress = strCPU.trim();
                        break;
                    }
                } else {
                    // 文件结尾
                    break;
                }
            }
        } catch (Exception ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        Log.e("smallstar", "cpuinfo Serial = " + cpuAddress);
        return cpuAddress;
    }

    public static String yfgetAndroidDeviceModel() {
        String model = getApiManager(OtaApplication.getApp()).yfgetAndroidDeviceModel();
        return model==null?"":model;
    }

    //获取系统内部存储信息。
    public static String yfgetInternalStorageMemory() {
        String storage = getApiManager(OtaApplication.getApp()).yfgetInternalStorageMemory();
        return storage==null?"":storage;
    }

    //获取 android 版本
    public static String yfgetAndroidVersion() {
        String version =  getApiManager(OtaApplication.getApp()).yfgetAndroidVersion();
        return version==null?"":version;
    }

    //获取内核版本
    public static String yfgetKernelVersion() {
        String version =  getApiManager(OtaApplication.getApp()).yfgetKernelVersion();
        return version==null?"":version;
    }

    //获取设备序列号
    public static String yfgetSerialNumber() {
        String number = getApiManager(OtaApplication.getApp()).yfgetSerialNumber();
        return number==null?"":number;
    }

    //编译日期，生成日期
    public static String yfgetBuildDate() {
        String buildDate =  getApiManager(OtaApplication.getApp()).yfgetBuildDate();
        return buildDate==null?"":buildDate;
    }

    //获取eth mac地址
    public static String yfgetEthMacAddress() {
        String mac = getApiManager(OtaApplication.getApp()).yfgetEthMacAddress();
        return mac==null?"":mac;
    }

    //buildNumber
    public static String getBuildNumber() {
        String builderNumber = getApiManager(OtaApplication.getApp()).yfgetFirmwareVersion();
        return builderNumber==null?"":builderNumber;
    }

    // finger print
    public static String getFingerPrint() {
        return Build.FINGERPRINT==null?"":Build.FINGERPRINT;
    }

    //
    public static String getResolutionRatio(Context context) {
        //YF_RK356x_API_Manager apiManager = getApiManager(OtaApplication.getApp());
        //return apiManager.yfgetScreenWidth() +"x" + apiManager.yfgetScreenHeight();
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getRealMetrics(dm);

        int PanelWidth;
        int PanelHeight;
        PanelWidth = dm.widthPixels;
        PanelHeight = dm.heightPixels;

        String status = PanelWidth+ "X" + PanelHeight;
        return status;
    }


    public static String getAppVersion() {
        String version = "";
        try {
            PackageManager packageManager = OtaApplication.getApp().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(OtaApplication.getApp().getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (Exception e) {
            Log.d("smallstar", "get version name failed");
        }
        return version;
    }


    public static DeviceInfo collectDeviceInfo() {
        DeviceInfo deviceDetail = new DeviceInfo();
        deviceDetail.setDeviceModel(DeviceUtils.yfgetAndroidDeviceModel());
        deviceDetail.setKernelVersion(DeviceUtils.yfgetKernelVersion());
        deviceDetail.setAndroidVersion(DeviceUtils.yfgetAndroidVersion());
        deviceDetail.setStorage(DeviceUtils.yfgetInternalStorageMemory());
        deviceDetail.setSerialNumber(DeviceUtils.yfgetSerialNumber());
        deviceDetail.setBuildDate(DeviceUtils.yfgetBuildDate());
        deviceDetail.setEthMacAddress(DeviceUtils.yfgetEthMacAddress().trim());
        deviceDetail.setWifiMacAddress(DeviceUtils.getMac(OtaApplication.getApp()).trim());
        deviceDetail.setCpuId(DeviceUtils.getCPUSerial());
        deviceDetail.setBuildNumber(DeviceUtils.getBuildNumber());
        deviceDetail.setResolutionRatio(DeviceUtils.getResolutionRatio(OtaApplication.getApp()));
        deviceDetail.setAppVersion(DeviceUtils.getAppVersion());
        return deviceDetail;
    }

}
