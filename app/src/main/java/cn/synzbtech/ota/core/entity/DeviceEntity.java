package cn.synzbtech.ota.core.entity;

import lombok.Data;

@Data
public class DeviceEntity {

    private Long id;

    private Long otaSerialNoId;

    private String otaSerialNo;

    private Long otaBatchId;

    private String otaBatchNo;

    private String cpu;

    private String mac;

    private String wifiMac;

    private DeviceInfo detail;

    private Integer status;

    private Integer online;
}
