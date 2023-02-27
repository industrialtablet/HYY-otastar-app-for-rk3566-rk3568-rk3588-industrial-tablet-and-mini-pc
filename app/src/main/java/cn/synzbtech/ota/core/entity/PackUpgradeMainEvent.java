package cn.synzbtech.ota.core.entity;

import java.io.Serializable;

import cn.synzbtech.ota.core.Api;
import lombok.Data;

@Data
public class PackUpgradeMainEvent implements Serializable {
    private int status;
    public PackUpgradeMainEvent() {
        this.status = Api.DownloadState.READY;
    }
    public PackUpgradeMainEvent(int status) {
        this.status = status;
    }
}
