package com.chronomon.st.data.server.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 用户表：原始的时空数据表
 *
 * @author wangrubin
 */
@TableName("t_user_gps_raw")
public class RawDataPO {
    /**
     * 对象ID
     */
    private String oid;

    /**
     * 所在瓦片行列号的Z填充曲线编码
     */
    private Long zVal;

    /**
     * 时间片起始时刻
     */
    private LocalDateTime periodStartTime;

    /**
     * 地图横坐标(单位:像素)
     */
    private Long tileX;

    /**
     * 地图纵坐标(单位:像素)
     */
    private Long tileY;

    /**
     * 时间片内的相对时间戳
     */
    private Integer timeOffset;
}
