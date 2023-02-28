package cn.synzbtech.ota.utils;

import android.os.Environment;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import cn.synzbtech.ota.OtaApplication;

public class FileUtils {

    public static void prepareSavePath() {

        //chmod -R 777 /storage/emulated/0/Android/data
        Log.d("smallstar", "prepareSavePath()");

        execRootCmdSilent("chmod -R 777 /storage/emulated/0/Android/data");
        execRootCmdSilent("chmod -R 777 /mnt/sdcard/");

        File root = OtaApplication.getApp().getFilesDir();

        File apkPath = new File(root, "apk");
        File packPath = new File(root, "pack");
        if(!apkPath.exists()) {
            boolean b = apkPath.mkdirs();
            Log.d("FileUtils", apkPath.getAbsolutePath() +  " apk path created " + b);
        }
        if(!packPath.exists()) {
            boolean b = packPath.mkdirs();
            Log.d("FileUtils", packPath.getAbsolutePath() + " pack path created " + b);
        }

        File mntPath = new File("/storage/emulated/0/Android/data");
        if(!mntPath.exists()) {
            boolean b = mntPath.mkdirs();
            Log.d("FileUtils", packPath.getAbsolutePath() + " mnt path created " + b);
        }
    }

    public static String getExtendStoragePath() {
        File ota = new File(Environment.getExternalStorageDirectory(), "ota");
        if(!ota.exists()) {
            boolean mkdirs = ota.mkdirs();
        }
        return ota.getAbsolutePath();
    }

    public static String getSaveFilePath(String filePath) {
        File root = OtaApplication.getApp().getFilesDir();
        File path = new File(root, filePath);
        return path.getAbsolutePath();
    }

    public static String getUrlFileName(String url) {
        return url.substring(url.lastIndexOf("/")+1);
    }

    public static int execRootCmdSilent(String cmd) {
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());
            Log.i("smallstar", cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


}
