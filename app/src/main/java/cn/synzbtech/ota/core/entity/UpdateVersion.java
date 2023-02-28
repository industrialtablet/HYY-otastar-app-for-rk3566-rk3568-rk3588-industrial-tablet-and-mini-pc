package cn.synzbtech.ota.core.entity;

import lombok.Data;

@Data
public class UpdateVersion {
    private ApkVersion apkVersion;
    private OtaVersion otaVersion;
}
