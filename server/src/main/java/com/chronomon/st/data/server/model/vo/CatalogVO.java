package com.chronomon.st.data.server.model.vo;

import lombok.Data;

/**
 * 用户目录VO
 *
 * @author wangrubin
 */
@Data
public class CatalogVO {
    /**
     * 用户目录名
     */
    private String catalogName;

    /**
     * 用户目录访问Key
     */
    private String accessKey;

    /**
     * 金字塔模型做大层级，[0,20]之间的整数
     */
    private Integer maxZoomLevel;

    /**
     * 地图瓦片边长(单位：像素)，2的幂次方
     */
    private Integer tileExtent;

    /**
     * 时间片单位，粒度从小到大有：Minutes、Hours、HalfDays、Days
     */
    private String periodUnit;

    /**
     * 保留的时间片数量：比如最近的1000给时间片
     */
    private Long retentionPeriod;
}
