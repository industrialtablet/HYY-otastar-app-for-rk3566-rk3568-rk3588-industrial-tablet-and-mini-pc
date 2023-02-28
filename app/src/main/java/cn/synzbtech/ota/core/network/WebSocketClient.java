package cn.synzbtech.ota.core.network;

import android.util.Log;

import com.gnepux.wsgo.WsConfig;
import com.gnepux.wsgo.WsGo;
import com.gnepux.wsgo.jwebsocket.JWebSocket;

import java.util.HashMap;
public class WebSocketClient {

    private static final String WS_URL = "ws://192.168.1.11:8081/otastar/";

    public static void connect(String appid, String cpuId) {

        Log.d("WebSocketClient", "device will connect ws, appid="+appid+", cpuId="+cpuId);

        WsConfig config = new WsConfig.Builder()
                .debugMode(true)    // true to print log
                .setUrl(WS_URL + appid + "/" + cpuId)    // ws url
                .setHttpHeaders(new HashMap<>())
                .setConnectTimeout(10 * 1000L)  // connect timeout
                .setReadTimeout(10 * 1000L)     // read timeout
                .setWriteTimeout(10 * 1000L)    // write timeout
                .setPingInterval(10 * 1000L)    // initial ping interval
                .setWebSocket(JWebSocket.create()) // websocket client
                .setRetryStrategy(retryCount -> 3000)    // retry count and delay time strategy
                .setEventListener(new WebSocketEventListener()) // event listener
                .build();

        WsGo.init(config);

        WsGo.getInstance().connect();
    }
}
