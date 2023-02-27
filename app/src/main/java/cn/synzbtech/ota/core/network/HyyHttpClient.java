package cn.synzbtech.ota.core.network;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import org.apache.commons.lang3.StringUtils;
import org.xutils.http.RequestParams;
import org.xutils.x;


import java.util.Map;

import cn.synzbtech.ota.AppConfig;


/**
 * http client is a network request utility class. This class implements both synchronous and asynchronous get and post methods.
 *
 * When you send a request, you first need to add appId and secretKey to the header. Please refer to the Readme file for appid and secretKey
 *
 * @author dennis@we-signage.com
 *
 */
public class HyyHttpClient {
    private static final String TAG ="HyyHttpClient";
    public String HOST = "http://192.168.1.11:8081/api/"; //Address of the host providing the service, http protocol

    public static String  APPID = "1629375064388136961";
    public static String SECRET_EY = "ZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKSVV6STFOaUo5LmV5SmhjSEJKWkNJNklqRTJNamt6TnpVd05qUXpPRGd4TXpZNU5qRWlMQ0oxYzJWeVRtRnRaU0k2SW1Ga2JXbHVJaXdpWlhod0lqb3lORFkyTWpJMk5qWXlMQ0oxYzJWeVNXUWlPaUl4SW4wLm5LVXBjZU9icXlzRFRZOElNb1E3TnRkeU5PYmxMRVZJUWRobHZzeERtalE=";
    private static HyyHttpClient mInstance;
    public static synchronized HyyHttpClient getInstance(){

        if(mInstance==null){
            mInstance = new HyyHttpClient();
            if(StringUtils.isNotEmpty(AppConfig.HOST)){
                mInstance.host(AppConfig.HOST);
            }
            if(StringUtils.isNotEmpty(AppConfig.APPID)){
                mInstance.appid(AppConfig.APPID);
            }
            if(StringUtils.isNotEmpty(AppConfig.SECRET_KEY)){
                mInstance.secretKey(AppConfig.SECRET_KEY);
            }
        }
        return mInstance;
    }

    private HyyHttpClient(){

    }

    public HyyHttpClient host(String host) {
        this.HOST = host;
        return this;
    }


    public HyyHttpClient appid(String appid){
        this.APPID = appid;
        return this;
    }

    public HyyHttpClient secretKey(String secretKey){
        this.SECRET_EY = secretKey;
        return this;
    }

    /**
     * Synchronous get request
     * @param uri
     * @param params
     * @return
     */
    public String get(String uri, Map<String, Object> params) {

        RequestParams requestParams = new RequestParams(HOST + uri);
        requestParams.addHeader("appId", APPID);
        requestParams.addHeader("secretKey", SECRET_EY);
        if(params!=null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                requestParams.addQueryStringParameter(entry.getKey(), entry.getValue());
            }
        }
        try {
            return x.http().postSync(requestParams, String.class);
        } catch (Throwable throwable) {
            Log.e(TAG, "get request failed", throwable);
        }
        return null;
    }


    /**
     * Synchronous post request
     * @param uri
     * @param params a pojo or map. if json = false, this field must is hashMap , else json=true this field is pojo or map object
     * @param json  if post json data, this value must assignment true.
     * @return
     */
    public String post(String uri, Object params, boolean json) {

        final RequestParams requestParams = new RequestParams();
        requestParams.setUri(HOST + uri);
        requestParams.addHeader("appId", APPID);
        requestParams.addHeader("secretKey", SECRET_EY);
        requestParams.addHeader("Connection", "close");
        if(json) {
            requestParams.addHeader("Content-Type", "application/json;charset=UTF-8");
            requestParams.setBodyContent(JSON.toJSONString(params));
        } else {
            if(params!=null) {
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) params).entrySet()) {
                    requestParams.addBodyParameter(entry.getKey(), entry.getValue());
                }
            }
        }
        try {
           return x.http().postSync(requestParams, String.class);
        } catch (Throwable e){
            Log.e(TAG, "post request failed", e);
        }
        return null;
    }

    /**
     *
     */
    public static class URI {
        public static final String PUSH_DEVICE_INFO = "device/save";
        public static final String GET_APK_VERSION = "apk/version";
        public static final String GET_OAT_VERSION = "ota/version";
        public static final String GET_UPDATE_VERSION = "update/version";
    }

}
