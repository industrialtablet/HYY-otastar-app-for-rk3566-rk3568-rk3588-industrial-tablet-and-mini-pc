package cn.synzbtech.ota.core.network;


import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gnepux.wsgo.EventListener;
import com.gnepux.wsgo.WsGo;

import java.util.HashMap;
import java.util.Map;

import cn.synzbtech.ota.core.Constants;

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
        Log.d(TAG, "onMessage : " + text);
        MessageBody messageBody = JSON.parseObject(text, MessageBody.class);
        switch (messageBody.getCommand()) {

            case Constants.COMMEND_UPDATE_OTA_NOTIFY: {
                JSONObject obj = (JSONObject) messageBody.getData();
                String url = obj.getString("url");
                Long upgradeRunId = obj.getLong("upgradeRunId");
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("upgradeRunId", upgradeRunId);
                MessageBody ack = new MessageBody(Constants.COMMEND_UPDATE_OTA_ACK, dataMap);
                WsGo.getInstance().send(JSON.toJSONString(ack));
                break;
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
