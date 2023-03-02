# OTAstar

# 		——Best ota uprade application for your android



## 1. introduction

OTAstar is an android and multi-user OTA upgrade program. You will need obtain the appropriate appid and key from our platform services. Configure in AppConfig to ensure that your device can normally request server upgrades and other resources. Please refer to how to obtain appid and secret key (How to obtain appid and secret key).

## 2. Supported Devices

RK3566 RK3568 RK3588 S905x4

## 3. Obtain appid and secret key

// todo 

## 4. Configuration appid and secret key 

Open the AppConfig file in the cn/syzbtech/ota package, then set your Appid and secret key into the APPID field and SECRET_KEY field of this class

```
public class AppConfig {
    public static final String HOST = "";
    public static final String APPID="";
    public static final String SECRET_KEY="";
}
```

## 5. Install as system application

```
adb root
adb remount
adb push <the path of your apk > /system/app
```

