package cn.synzbtech.ota.core.zookeeper;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.client.ZooKeeperSaslClient;
import org.apache.zookeeper.data.Stat;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import cn.synzbtech.ota.core.Api;
import cn.synzbtech.ota.core.DeviceInfoWrapper;
import cn.synzbtech.ota.core.entity.ResultWrapper;

/**
 * @author: 马士兵教育
 * @create: 2019-09-20 20:08
 */
public class ZKUtils {

    private static final String TAG = "smallstar";


    private static ZooKeeper onlineZk;

    private static ZooKeeper upgradeZk;

    private static DefaultWatch watch = new DefaultWatch();

    private static CountDownLatch onLineZkInit =  new CountDownLatch(1);

    private static CountDownLatch upgradeZkInit = new CountDownLatch(1);

    public synchronized static ZooKeeper getUpgradeZk(){

        if(upgradeZk ==null || !upgradeZk.getState().isConnected()) {
            watch.setCc(upgradeZkInit);
            try {
                System.setProperty(ZooKeeperSaslClient.ENABLE_CLIENT_SASL_KEY, "false");
                Log.d("ZooUtils", "Zookeeper Sasl Client >>> " + ZooKeeperSaslClient.isEnabled());
                upgradeZk = new ZooKeeper(Api.zkHost + "/pack_upgrade", 10 * 1000, watch);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                upgradeZkInit.await();
            } catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException");
            }
        }
        return upgradeZk;
    }


    public synchronized static ZooKeeper getOnlineZK(){

        if(onlineZk ==null || !onlineZk.getState().isConnected()) {
            watch.setCc(onLineZkInit);
            try {
                System.setProperty(ZooKeeperSaslClient.ENABLE_CLIENT_SASL_KEY, "false");
                Log.d("ZooUtils", "Zookeeper Sasl Client >>> " + ZooKeeperSaslClient.isEnabled());
                onlineZk = new ZooKeeper(Api.zkHost + "/online", 10 * 1000, watch);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                onLineZkInit.await();
            } catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException");
            }
        }
        return onlineZk;
    }

    public synchronized static void closeZk() {
        try {
            if(onlineZk !=null) {
                onlineZk.close();
                onlineZk = null;
            }
            if(upgradeZk!=null) {
                upgradeZk.close();
                onlineZk = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "zookeeper close error ", e);
        }
    }

    public static void keepAlive(String cpuId){

        try {
            ZooKeeper zk = ZKUtils.getOnlineZK();

            Stat stat = new Stat();
            boolean exist = false;
            try {
                byte[] d = zk.getData("/" + cpuId, false, stat);
                Log.d(TAG, ">>> "+ new String(d));
                exist = d!=null;
            } catch (Exception e) {
                Log.e(TAG,"get online status error ", e);
            }
            if(exist) {
               return;
            }
            String result = zk.create("/" + cpuId, cpuId.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            Log.d(TAG, "device on line success >> " + result);
        } catch (Exception e) {
            Log.e(TAG, "online error", e);
        }
    }

    public static void createOnlineNode() {

            new Thread(() -> {

                try {
                    ZooKeeper zk = ZKUtils.getOnlineZK();
                    String cpuId = DeviceInfoWrapper.deviceInfo.getCpuId();
                    Stat stat = new Stat();
                    boolean exist = false;
                    try {
                        byte[] d = zk.getData("/" + cpuId, false, stat);
                        Log.d(TAG, ">>> "+ new String(d));
                        exist = d!=null;
                    } catch (Exception e) {
                        Log.d(TAG,"device not online now, will be create online node ");
                    }
                    if(exist) {
                        try {
                            zk.delete("/" + cpuId, -1);
                        }catch (Exception e) {
                            Log.e(TAG, "remove online statue");
                        }
                    }
                    String result = zk.create("/" + cpuId, cpuId.getBytes(),
                            ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                    Log.d(TAG, "device on line success >> " + result);
                } catch (Exception e) {
                    Log.e(TAG, "online error", e);
                }
            }).start();


    }

    public static void removePackUpgradeNode(final boolean doInstall) {

            new Thread(() -> {
                ZooKeeper zk = ZKUtils.getUpgradeZk();

                String cpuId = DeviceInfoWrapper.deviceInfo.getCpuId();
                Stat stat = new Stat();
                stat.setVersion(-1);

                boolean exist = false;
                byte[] d = null;
                try {
                    d = zk.getData("/" + cpuId, false, stat);
                    exist = d!=null;
                } catch (Exception e) {
                    Log.d(TAG, ">>>> getData Error >>>> ");
                    e.printStackTrace();
                }

                if(exist) {
                    try {
                        zk.delete("/" + cpuId, -1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

    }

    public static void createOrUpdateNode(final String data) {
        new Thread(()->{
            try {
                String cpuId = DeviceInfoWrapper.deviceInfo.getCpuId();
                String url = Api.host + "api/set/upgrade/status/" + cpuId + "/" + data;
                Log.d(TAG, " url "+ url);
                RequestParams params = new RequestParams(url);
                String responseJSON = x.http().getSync(params, String.class);
                Log.d(TAG, ""+ responseJSON);
                ResultWrapper<Long> resultWrapper = JSON.parseObject(responseJSON, new TypeReference<ResultWrapper<Long>>() {
                }.getType());
                Log.d(TAG, ""+ resultWrapper);
            } catch (Throwable e) {
                Log.e(TAG, "createPackUpgradeNode error", e);
            }
        }).start();
    }
}
