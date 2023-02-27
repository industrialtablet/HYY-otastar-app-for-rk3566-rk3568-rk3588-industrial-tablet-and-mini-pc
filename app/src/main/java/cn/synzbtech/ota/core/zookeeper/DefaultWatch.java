package cn.synzbtech.ota.core.zookeeper;

import android.util.Log;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

/**
 * @author: 马士兵教育
 * @create: 2019-09-20 20:12
 */
public class DefaultWatch  implements Watcher {

    CountDownLatch cc ;

    public void setCc(CountDownLatch cc) {
        this.cc = cc;
    }

    @Override
    public void process(WatchedEvent event) {

        System.out.println(event.toString());

        switch (event.getState()) {
            case Unknown:
                Log.d("ZooUtils", ">>>> Unknown >>>> ");
                break;
            case Disconnected:
                Log.d("ZooUtils", ">>>> Disconnected >>>> ");
                break;
            case NoSyncConnected:
                Log.d("ZooUtils", ">>>> NoSyncConnected >>>> ");
                break;
            case SyncConnected:
                Log.d("ZooUtils", ">>>> SyncConnected >>>> ");
                cc.countDown();
                break;
            case AuthFailed:
                Log.d("ZooUtils", ">>>> AuthFailed >>>> ");
                break;
            case ConnectedReadOnly:
                Log.d("ZooUtils", ">>>> ConnectedReadOnly >>>> ");
                break;
            case SaslAuthenticated:
                Log.d("ZooUtils", ">>>> SaslAuthenticated >>>> ");
                break;
            case Expired:
                Log.d("ZooUtils", ">>>> Expired >>>> ");
                break;
        }


    }
}
