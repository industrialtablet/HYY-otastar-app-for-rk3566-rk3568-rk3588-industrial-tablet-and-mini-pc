package cn.synzbtech.ota.core.network;


import android.util.Log;

import com.gnepux.wsgo.EventListener;

public class WebSocketEventListener implements EventListener {


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
        Log.d(TAG, "onMessage:"+text);
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
