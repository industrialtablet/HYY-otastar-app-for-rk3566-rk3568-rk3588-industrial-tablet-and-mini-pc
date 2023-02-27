# otastar, The android client of ota uprade 

## introduction

OTA star is an android and multi-user OTA upgrade program, you will need to obtain the appropriate appid and key from our platform services. Configure in AppConfig to ensure that your device can normally request server upgrades and other resources. Please refer to how to obtain appid and secret key (How to obtain appid and secret key).

## How to obtain appid and secret key

// todo 

## How to configuration appid and secret key 

Open the AppConfig file in the cn/syzbtech/ota package, then set your Appid and secret key into the APPID field and SECRET_KEY field of this class

```
public class AppConfig {
    public static final String HOST = "";
    public static final String APPID="";
    public static final String SECRET_KEY="";
}
```

## How to install as system application

```
adb root
adb remount
adb push <the path of your apk > /system/app
```

