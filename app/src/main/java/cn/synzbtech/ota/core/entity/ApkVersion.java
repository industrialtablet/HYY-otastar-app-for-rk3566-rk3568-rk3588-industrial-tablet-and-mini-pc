package cn.synzbtech.ota.core.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *  页面展示实体
 * </p>
 *
 * @author coowalt@sina.com
 * @version V0.0.1
 * @contact
 * @date 2021-10-16
 * @company 政本科技有限公司
 * @copyright 政本科技有限公司 · 项目架构部
 */
@Data
@EqualsAndHashCode
public class ApkVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String content;

    private String apkVersion;

    private Long otaFilesId;

    private String md5;

    private String url;

    private String apkFileSize;

    private Integer sort;
}
