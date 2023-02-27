package cn.synzbtech.ota.core;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import org.greenrobot.eventbus.EventBus;
import org.xutils.http.RequestParams;
import org.xutils.x;

import cn.synzbtech.ota.OtaApplication;
import cn.synzbtech.ota.utils.PreferenceUtils;
import cn.synzbtech.ota.core.entity.PackUpgradeMainEvent;
import cn.synzbtech.ota.core.entity.ResultWrapper;
import cn.synzbtech.ota.core.zookeeper.ZKUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ApiService {

    public static ApiService mInstance = new ApiService();

    public static ApiService getInstance() {
        return mInstance;
    }

    public void doLogUpgrade(final String type) {

        Observable.create((ObservableOnSubscribe<Long>) emitter -> {

            String url = "";
            if(type.equalsIgnoreCase("apk")) {
                url = "api/apk/upgrade/log";
            } else if(type.equalsIgnoreCase("ota")) {
                url = "api/pack/upgrade/log";
            }

            RequestParams params = new RequestParams(Api.host + url);
            String wifiMac = PreferenceUtils.getInstance().getDeviceMac();
            params.addBodyParameter("wifiMac", wifiMac);
            try {

                String responseJSON = x.http().postSync(params, String.class);
                ResultWrapper<Long> resultWrapper = JSON.parseObject(responseJSON, new TypeReference<ResultWrapper<Long>>(){}.getType());
                Log.d("OTAMAIN", ""+ resultWrapper);

                if(type.equalsIgnoreCase("apk")) {
                    OtaApplication.apkUpgradeState = Api.UpgradeState.CHECK;
                    PreferenceUtils.getInstance().setApkUpgradeState(Api.UpgradeState.CHECK);

                } else if(type.equalsIgnoreCase("ota")){

                    OtaApplication.packUpgradeState = Api.UpgradeState.CHECK;
                    PreferenceUtils.getInstance().setPackUpgradeState(Api.UpgradeState.CHECK);
                    EventBus.getDefault().post(new PackUpgradeMainEvent(Api.DownloadState.SUCCESS));
                    ZKUtils.createOrUpdateNode("INSTALLING");
                }

            } catch (Throwable e) {

                /*
                if(type.equalsIgnoreCase("apk")) {
                    OtaApplication.apkUpgradeState = Api.UpgradeState.CHECK;
                    PreferenceHandle.getInstance().setApkUpgradeState(Api.UpgradeState.CHECK);
                } else if(type.equalsIgnoreCase("ota")) {
                    OtaApplication.packUpgradeState = Api.UpgradeState.CHECK;
                    PreferenceHandle.getInstance().setPackUpgradeState(Api.UpgradeState.CHECK);
                }*/

                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Long aLong) {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        });
    }
}
