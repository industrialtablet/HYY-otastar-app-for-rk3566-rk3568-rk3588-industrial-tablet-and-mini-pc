package cn.synzbtech.ota.core.entity;

import lombok.Data;

@Data
public class DeviceInfo {
    private String deviceModel;
    private String kernelVersion;
    private String androidVersion;
    private String storage;
    private String serialNumber;
    private String buildDate;
    private String ethMacAddress;
    private String wifiMacAddress;
    private String cpuId;
    private String buildNumber;
    private String resolutionRatio;
    private String appVersion;
}
