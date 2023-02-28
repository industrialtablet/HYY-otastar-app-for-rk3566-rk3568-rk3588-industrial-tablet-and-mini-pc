package cn.synzbtech.ota.core.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author owen
 * @version 0.0.1
 * @contact coowalt@sina.com
 * @company 政本科技有限公司
 * @date 2021-06-05 21:05
 */
@Data
public class ResultWrapper<T> implements Serializable {
    public String msg;
    private Integer code;
    private T data;
}
