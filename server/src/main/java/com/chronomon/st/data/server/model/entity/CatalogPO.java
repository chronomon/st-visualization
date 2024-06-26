package com.chronomon.st.data.server.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统表：用户目录元数据
 *
 * @author wangrubin
 */
@Data
@TableName("t_sys_catalog")
public class CatalogPO {
    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户目录名
     */
    private String catalogName;

    /**
     * 用户目录访问Key
     */
    private String catalogId;

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
     * 下一次待归档的时间片
     */
    private Long nextRollPeriod;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
