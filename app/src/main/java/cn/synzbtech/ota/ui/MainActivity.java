package cn.synzbtech.ota.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.common.util.MD5;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import cn.synzbtech.ota.OtaApplication;
import cn.synzbtech.ota.R;
import cn.synzbtech.ota.core.Api;
import cn.synzbtech.ota.core.ApiService;
import cn.synzbtech.ota.core.entity.ApkUpgradeMainEvent;
import cn.synzbtech.ota.core.entity.DeviceInfo;
import cn.synzbtech.ota.core.entity.PackUpgradeMainEvent;
import cn.synzbtech.ota.core.zookeeper.ZKUtils;
import cn.synzbtech.ota.service.CheckUpdateService;
import cn.synzbtech.ota.service.MonitoringService;
import cn.synzbtech.ota.utils.DeviceUtils;
import cn.synzbtech.ota.utils.FileUtils;
import cn.synzbtech.ota.utils.NotificationUtils;
import cn.synzbtech.ota.utils.OtaUtils;
import cn.synzbtech.ota.utils.PreferenceUtils;
import qiu.niorgai.StatusBarCompat;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQ_CODE = 100;
    private ProgressDialog progressDialog;
    private TextView tvDeviceModel;
    private TextView tvKernelVersion;
    private TextView tvAndroidVersion;
    private TextView tvAndroidVersionB;
    private TextView tvStorage;
    private TextView tvSerialNumber;
    private TextView tvEthMacAddress;
    private TextView tvWifiMacAddress;
    private TextView tvBuildDate;
    private TextView tvAppVersion;
    private TextView tvUpgradeHint;
    private TextView tvCpuId;
    private Button btnOtaUpgrade;
    private Button btnAppUpgrade;
    private Button getBtnAppUpgradeTest;
    private  Callback.Cancelable packDownloadHandler;
    private AlertDialog alertUpgradeDialog;

    private DeviceInfo deviceInfo;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("OTAINSTALL", "result code " + resultCode);
    }

    @Override
    protected void onDestroy() {

        ZKUtils.closeZk();
        super.onDestroy();
    }

    /**
     * apk事件处理
     * @param apkUpgradeMainEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void apkUpgradeEvent(ApkUpgradeMainEvent apkUpgradeMainEvent) {
        int status = apkUpgradeMainEvent.getStatus();
        switch (status) {
            case Api.DownloadState.READY:
                alertDownloadUpgrade("apk");
                break;
            case Api.DownloadState.CANCELED:
                if(packDownloadHandler!=null) { // 正在下载的时候才需要取消。
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setCancelable(false);
                    builder.setTitle("INFO").setMessage("Server canceled apk upgrade, You need cancel download?");
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            packDownloadHandler.cancel();

                            dialog.dismiss();
                        }
                    });
                    builder.show();

                } else if(alertUpgradeDialog!=null) {
                    alertUpgradeDialog.dismiss();
                    alertUpgradeDialog = null;
                }
                tvUpgradeHint.setText("Your system is update to date");
                break;
        }

    }

    /**
     * ota事件处理
     * @param packUpgradeMainEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void packUpgradeEvent(PackUpgradeMainEvent packUpgradeMainEvent) {
        int status = packUpgradeMainEvent.getStatus();

        switch (status) {

            case Api.DownloadState.CANCELED: //download canceled
                if(packDownloadHandler!=null) { // 正在下载的时候才需要取消。
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setCancelable(false);
                    builder.setTitle("INFO").setMessage("Server canceled device upgrade, You need cancel download?");
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            packDownloadHandler.cancel();

                            dialog.dismiss();
                        }
                    });
                    builder.show();
                } else if(alertUpgradeDialog!=null) {
                    alertUpgradeDialog.dismiss();
                    alertUpgradeDialog = null;
                }

                tvUpgradeHint.setText("Your system is update to date");

                break;
            case Api.DownloadState.FAILED:
                //下载失败
                tryAgainDownloadAlert("Upgrade package download failed, try again? ");
                break;
            case Api.DownloadState.READY:
                alertDownloadUpgrade("ota");
                break;
            case 2:
                break;
            default: // 等于3的时候
                String packSavePath = PreferenceUtils.getInstance().getPackSaveStorage();

                //安装之前校验ota升级包。防止传输丢字节。
                if(!validateUpgradePackage(packSavePath)) {

                    //文件校验失败。
                    tryAgainDownloadAlert("validate ota upgrade package file failed, try again download?");
                    return;
                }
                //移动安装目录
                String installPath = "/mnt/sdcard/update.zip";

                //判断是否有旧的更新包存在，如果有则删除成功后再设置移动判断变量为true，如果不存在直接设置移动判断变量为true
                File installFile = new File(installPath);
                boolean oldInstallFileDeleted ;
                boolean needMove = false;
                if(installFile.exists()) {
                    oldInstallFileDeleted = installFile.delete();
                    if(oldInstallFileDeleted) {
                        needMove = true;
                    }
                    //Log.d("smallstar", "Old package delete, the delete result is " + oldInstallFileDeleted);
                } else{
                    needMove = true;
                }
                if(needMove) {
                    int result = FileUtils.execRootCmdSilent("mv " + packSavePath + " " + installPath);
                    //Log.d("smallstar", "Move ota upgrade file " + packSavePath +" to /mnt/sdcard/update.zip" +", result -> " +  result);
                }
                //开始安装更新
                doUpdateOta(installPath);

                // 30秒后更新服务器完成更新安装状态
                sendDeviceOtaUpgradeCompleteInstallingState();
                break;
        }
    }

    private void doUpdateOta(String installPath) {
        //Log.d("smallstar", "Will install upgrade from file + " + installPath +" and the package is new ? " + needMove);
        if(android.os.Build.MODEL.equals("HK1RBOX X4S") || android.os.Build.MODEL.equals("ALL-S905X4")) {
            Log.e("smallstar", android.os.Build.MODEL);
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.droidlogic.updater");
            if (intent != null) {
                intent.putExtra("path", installPath);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this,"UPDATER app not installed", Toast.LENGTH_SHORT).show();
            }
        } else {
            OtaUtils.otaUpgradeYF3566(MainActivity.this, installPath);
        }
    }

    /**
     * 间隔一定时间后更新oat完成状态 30秒后
     */
    private void sendDeviceOtaUpgradeCompleteInstallingState() {
        new Timer("state change", false).schedule(new TimerTask() {
            @Override
            public void run() {
                ZKUtils.createOrUpdateNode("NONE");
            }
        }, 1000 * 30);
    }

    /**
     * 下载ota升级包重试提示
     * @param message
     */
    private void tryAgainDownloadAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Error").setMessage(message);
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            OtaApplication.packUpgradeState = Api.UpgradeState.CHECK;
            PreferenceUtils.getInstance().setPackUpgradeState(Api.UpgradeState.CHECK);
            dialog.dismiss();
        });
        builder.setPositiveButton("Try Again", (dialog, which) -> doDownloadUpgrade("ota"));
        builder.show();
    }

    /**
     * 校验ota升级包是否丢字节
     * @param installPath
     * @return
     */
    private boolean validateUpgradePackage(String installPath) {

        try {
            String url = PreferenceUtils.getInstance().getPackUpgradeUrl();
            String originMd5 = url.split(",")[1];

            String s = MD5.md5(new File(installPath));

            Log.d("smallstar", "origin md5 = " + originMd5+" download file md5 = " + s+" validate result : " + s.equalsIgnoreCase(originMd5) );

            return s.equalsIgnoreCase(originMd5);
        } catch (IOException e) {
            Log.e("smallstar", "validate ota upgrade package failed, md5 not valid", e);
        }
        return false;
    }


    private void alertDownloadUpgrade(final String type) {

        tvUpgradeHint.setText("Check & update your Android version");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String upgradeMessage = type + " Need to Upgrade, whether to proceed with the upgrade?";
        if (type.equals("ota")) {
            upgradeMessage = "\nDo you want to install new Firmware?\n\n\n" +
                    "Be sure that you have saved all important data, before update your Display.";
        }
        builder.setTitle("Upgrade").setMessage(upgradeMessage)
                .setCancelable(false)
                .setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this,"Start Upgrade", Toast.LENGTH_SHORT).show();
                        doDownloadUpgrade(type);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(type.equalsIgnoreCase("apk")) {
                            OtaApplication.apkUpgradeState = Api.UpgradeState.CHECK;
                            PreferenceUtils.getInstance().setApkUpgradeState(Api.UpgradeState.CHECK);
                        } else if(type.equalsIgnoreCase("ota")) {
                            OtaApplication.packUpgradeState = Api.UpgradeState.CHECK;
                            PreferenceUtils.getInstance().setPackUpgradeState(Api.UpgradeState.CHECK);
                        }
                    }
        });
        alertUpgradeDialog = builder.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarCompat.translucentStatusBar(this);
        StatusBarCompat.translucentStatusBar(this, true);
        setupViews();

        Log.e("smallstar", android.os.Build.MODEL);
        deviceInfo = DeviceUtils.collectDeviceInfo();

        OtaUtils.startService(this, MonitoringService.class);
        OtaUtils.startService(this, CheckUpdateService.class);

        setViewData();

        checkPermission();

        EventBus.getDefault().register(this);

        if(!NotificationUtils.isNotificationEnabled(this)) {
            NotificationUtils.requestNotify(this);
        }

        FileUtils.prepareSavePath();

        NotificationUtils.getInstance().cancelNotification();

        handleApkUpgrade();

        handlePackUpgrade();

        getBtnAppUpgradeTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //OtaUtils.otaUpgradeYF3566(MainActivity.this, "/mnt/sdcard/update.zip");
                //FileUtils.execRootCmdSilent("mv /storage/emulated/0/Android/data/update.zip /mnt/sdcard/update.zip");

                /* 测试启动 Amlogic 盒子的升级app 调用
                Log.e("smallstar", android.os.Build.MODEL);
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.droidlogic.updater");
                if (intent != null) {
                    intent.putExtra("path", "storage/emulated/0/update.zip");//
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this,"UPDATER app not installed", Toast.LENGTH_SHORT).show();
                }
                */

            }
        });
    }

    private void setViewData() {
        tvAppVersion.setText(deviceInfo.getAppVersion());
        tvDeviceModel.setText(deviceInfo.getDeviceModel());
        tvKernelVersion.setText(deviceInfo.getKernelVersion());
        tvAndroidVersion.setText(deviceInfo.getAndroidVersion());
        tvAndroidVersionB.setText(deviceInfo.getAndroidVersion());
        tvStorage.setText(deviceInfo.getStorage());
        tvSerialNumber.setText(deviceInfo.getSerialNumber());
        tvCpuId.setText(deviceInfo.getCpuId());
        tvEthMacAddress.setText(deviceInfo.getEthMacAddress());
        tvWifiMacAddress.setText(deviceInfo.getWifiMacAddress());
        tvBuildDate.setText(deviceInfo.getBuildDate());
    }

    private void setupViews() {
        tvAppVersion = findViewById(R.id.tvAppVersion);
        tvDeviceModel = findViewById(R.id.tvDeviceModel);
        tvKernelVersion = findViewById(R.id.tvKernelVersion);;
        tvAndroidVersion = findViewById(R.id.tvAndroidVersion);
        tvAndroidVersionB = findViewById(R.id.tvAndroidVersionB);
        tvStorage = findViewById(R.id.tvStorage);;
        tvSerialNumber = findViewById(R.id.tvSerialNumber);;
        tvEthMacAddress = findViewById(R.id.tvEthMacAddress);
        tvWifiMacAddress = findViewById(R.id.tvWifiMacAddress);
        tvBuildDate = findViewById(R.id.tvBuildDate);
        tvCpuId = findViewById(R.id.tvCpuId);
        btnAppUpgrade = findViewById(R.id.btnAppUpgrade);
        btnOtaUpgrade = findViewById(R.id.btnOtaUpgrade);
        tvUpgradeHint = findViewById(R.id.tv1);
        getBtnAppUpgradeTest = findViewById(R.id.btnOtaUpgradeTest);
    }

    private void handlePackUpgrade() {
        OtaApplication.packUpgradeState = PreferenceUtils.getInstance().getPackUpgradeState();
        if(OtaApplication.packUpgradeState == Api.UpgradeState.NEED_DOWNLOAD ) {
            alertDownloadUpgrade("ota");
        } else if(OtaApplication.packUpgradeState == Api.UpgradeState.DOWNLOAD_COMPLETE) {
            alertInstall("ota");
        } else if(OtaApplication.packUpgradeState == Api.UpgradeState.INSTALL_COMPLETE) {
            ApiService.getInstance().doLogUpgrade("ota");
        }
    }

    private void handleApkUpgrade() {
        OtaApplication.apkUpgradeState = PreferenceUtils.getInstance().getApkUpgradeState();
        Log.d("OTAMAIN", "on create apkUpgrade " + OtaApplication.apkUpgradeState );
        if(OtaApplication.apkUpgradeState == Api.UpgradeState.NEED_DOWNLOAD ) {
            alertDownloadUpgrade("apk");
        } else if(OtaApplication.apkUpgradeState == Api.UpgradeState.DOWNLOAD_COMPLETE) {
            alertInstall("apk");
        } else if(OtaApplication.apkUpgradeState == Api.UpgradeState.INSTALL_COMPLETE) {
            ApiService.getInstance().doLogUpgrade("apk");
        }
    }

    private void alertInstall(final String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示").setMessage(type + "更新包下载完成，是否安装？")
                .setPositiveButton("安装", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Log.d("smallstar", "upgrade type >> " + type);

                        if(type.equalsIgnoreCase("apk")) {
                            String apkPath = PreferenceUtils.getInstance().getApkSaveStorage();
                            OtaUtils.install(MainActivity.this, apkPath);
                            OtaApplication.apkUpgradeState = Api.UpgradeState.INSTALL_COMPLETE;
                            PreferenceUtils.getInstance().setApkUpgradeState(Api.UpgradeState.INSTALL_COMPLETE);

                        } else if(type.equalsIgnoreCase("ota")) {


                            String packPath = PreferenceUtils.getInstance().getPackSaveStorage();

                            Log.d("smallstar", "path: " + packPath);

                            OtaUtils.otaUpgradeYF3566(MainActivity.this, packPath);

                            OtaApplication.packUpgradeState = Api.UpgradeState.INSTALL_COMPLETE;
                            PreferenceUtils.getInstance().setPackUpgradeState(Api.UpgradeState.INSTALL_COMPLETE);
                        }
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(type.equalsIgnoreCase("apk")) {
                            OtaApplication.apkUpgradeState = Api.UpgradeState.CHECK;
                            PreferenceUtils.getInstance().setApkUpgradeState(Api.UpgradeState.CHECK);
                        } else if(type.equalsIgnoreCase("ota")) {
                            OtaApplication.packUpgradeState = Api.UpgradeState.CHECK;
                            PreferenceUtils.getInstance().setPackUpgradeState(Api.UpgradeState.CHECK);
                        }
                    }
                });
        builder.show();
    }

    //下载历史下载文件。
    private void downloadFileDeleteIfExist(File filePath) {

        if(filePath.exists()) {
            boolean delete = filePath.delete();
            Log.d("smallstar", "delete download history: " + filePath.getAbsolutePath()+" -> " + delete);
        }
    }
    private final static int MAX_DOWNLOAD_THREAD = RequestParams.MAX_FILE_LOAD_WORKER - 3;

    private final Executor executor = new PriorityExecutor(MAX_DOWNLOAD_THREAD, true);
    private void doDownloadUpgrade(final String type) {

        String url = "";
        String downloadFile = "";
        if(type.equalsIgnoreCase("apk")) {
            url =  PreferenceUtils.getInstance().getApkUpgradeUrl();
            downloadFile = FileUtils.getSaveFilePath(url);
        } else if(type.equalsIgnoreCase("ota")) {
            url = PreferenceUtils.getInstance().getPackUpgradeUrl();
            url = url.split(",")[0];
            downloadFile = "/storage/emulated/0/Android/data/update.zip";
            //downloadFile = FileUtils.getExtendStoragePath() +"/update.zip";
            Log.d("smallstar", "ota download path > " + downloadFile);
            //downloadFile = FileUtils.getSaveFilePath(url);
            ZKUtils.createOrUpdateNode("DOWNLOADING");
        }

        downloadFileDeleteIfExist(new File(downloadFile));

        RequestParams params = new RequestParams(Api.host + url);
        Log.d("OTAMAIN", "on create apkUpgrade url " + (Api.host + url) );
        params.setSaveFilePath(downloadFile);
        params.setAutoResume(true);
        params.setAutoRename(true);
        params.setExecutor(executor);

        params.setMaxRetryCount(5);

        packDownloadHandler = x.http().get(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {

                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                Log.d("smallstar", "upgrade type >> " + type);
                if (type.equalsIgnoreCase("apk")) {
                    OtaApplication.apkUpgradeState = Api.UpgradeState.DOWNLOAD_COMPLETE;
                    PreferenceUtils.getInstance().setApkUpgradeState(Api.UpgradeState.DOWNLOAD_COMPLETE);

                    PreferenceUtils.getInstance().setApkSaveStorage(result.getAbsolutePath());
                    OtaUtils.install(MainActivity.this, result.getAbsolutePath());

                    OtaApplication.apkUpgradeState = Api.UpgradeState.INSTALL_COMPLETE;
                    PreferenceUtils.getInstance().setApkUpgradeState(Api.UpgradeState.INSTALL_COMPLETE);

                } else if (type.equalsIgnoreCase("ota")) {
                    OtaApplication.packUpgradeState = Api.UpgradeState.DOWNLOAD_COMPLETE;
                    PreferenceUtils.getInstance().setPackUpgradeState(Api.UpgradeState.DOWNLOAD_COMPLETE);
                    PreferenceUtils.getInstance().setPackSaveStorage(result.getAbsolutePath());

                    OtaApplication.packUpgradeState = Api.UpgradeState.INSTALL_COMPLETE;
                    PreferenceUtils.getInstance().setPackUpgradeState(Api.UpgradeState.INSTALL_COMPLETE);

                }
                ApiService.getInstance().doLogUpgrade(type);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                //ZKUtils.removePackUpgradeNode(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }
                });

                EventBus.getDefault().post(new PackUpgradeMainEvent(-1));
                Log.d("OTAMAIN", "apk下载失败", ex);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                        }
                    }
                });
                packDownloadHandler = null;
                Log.d("OTAMAIN", "取消下载", cex);
            }

            @Override
            public void onFinished() {

            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog == null) {
                            progressDialog = new ProgressDialog(MainActivity.this);
                            progressDialog.setMax(100);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            progressDialog.setCancelable(false);
                            progressDialog.setTitle("Upgrade downloading...");
                            progressDialog.show();
                        }
                    }
                });
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                if (progressDialog != null) {
                    int progress = (int) ((current / (float) total) * 100);
                    progressDialog.setProgress(progress);
                }
            }
        });

    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

            Log.d("OTA", "not permission");

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
            }, PERMISSION_REQ_CODE);

            return false;
        } else {
            Log.d("OTA", "has permission");
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("OTA","onRequestPermissionsResult");
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            FileUtils.prepareSavePath();
        } else {
            Log.d("OTA","授权失败");
        }
    }
    private void alertError(String message,final boolean exit) {
       AlertDialog.Builder builder = new AlertDialog.Builder(this);
       builder.setTitle("提示").setMessage(message).setCancelable(false);
       builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               dialog.dismiss();
               if(exit) {
                    finish();
                }
           }
       }).show();
    }
}