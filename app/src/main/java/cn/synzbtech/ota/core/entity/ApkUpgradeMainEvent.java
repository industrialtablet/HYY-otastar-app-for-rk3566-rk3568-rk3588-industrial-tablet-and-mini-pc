package cn.synzbtech.ota.core.entity;

import cn.synzbtech.ota.core.Api;
import lombok.Data;

@Data
public class ApkUpgradeMainEvent {
    private Integer status;

    public ApkUpgradeMainEvent() {
        this.status = Api.DownloadState.READY;
    }
    public ApkUpgradeMainEvent(Integer status) {
        this.status = status;
    }

}
