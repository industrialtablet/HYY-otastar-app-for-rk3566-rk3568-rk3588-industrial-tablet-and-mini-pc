package cn.synzbtech.ota;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import cn.synzbtech.ota.core.network.HyyHttpClient;

public class HyyHttpClientTests {


    @Test
    public void testHttpClientGet() {
        HyyHttpClient hyyHttpClient = HyyHttpClient.getInstance();
        Map<String, Object> params = new HashMap<>();
        params.put("username", "owen");
        String response = hyyHttpClient.get("get-test?id=1", params);
        System.err.println(response);
    }

    @Test
    public void testHttpClientPost(){
        HyyHttpClient hyyHttpClient = HyyHttpClient.getInstance();
        Map<String, Object> params = new HashMap<>();
        params.put("username", "owen");
        String response = hyyHttpClient.post("post-test?id=1", params, false);
        System.err.println(response);
    }

    @Test
    public void testHttpClientPostJSON(){
        HyyHttpClient hyyHttpClient = HyyHttpClient.getInstance();
        Map<String, Object> params = new HashMap<>();
        params.put("username", "owen");
        params.put("id", 1);
        String response = hyyHttpClient.post("post-test", params, true);
        System.err.println(response);
    }
}
