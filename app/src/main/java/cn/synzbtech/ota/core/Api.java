package cn.synzbtech.ota.core;

public class Api {

    //服务地址
    public static final String host = "http://192.168.1.11:8081/";
    public static final String zkHost = "192.168.1.11:2181/ota/device";
    public static class URL {
        public static final String GET_APK_VERSION = "api/apk/version";
        public static final String GET_OAT_VERSION = "api/pack/version";
    }
    public static class UpgradeState {
        public static final int CHECK = 0;
        public static final int NEED_DOWNLOAD = 1;
        public static final int DOWNLOAD_COMPLETE = 2;
        public static final int INSTALL_COMPLETE = 3;
    }

    public static class DownloadState {
        public static final int CANCELED = -2;
        public static final int FAILED = -1;
        public static final int READY = 1;
        public static final int SUCCESS = 3;
    }
}
