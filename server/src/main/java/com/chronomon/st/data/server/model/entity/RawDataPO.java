package com.chronomon.st.data.server.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.chronomon.st.data.model.feature.MapFeature;
import lombok.Data;

import java.time.Instant;

/**
 * 用户表：原始的时空数据表
 *
 * @author wangrubin
 */
@Data
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
     * 地图横坐标(单位:像素)
     */
    private Long mapX;

    /**
     * 地图纵坐标(单位:像素)
     */
    private Long mapY;

    /**
     * 时间片内的相对时间戳
     */
    private long time;

    public MapFeature toMapFeature() {
        return new MapFeature(oid, Instant.ofEpochSecond(time), mapX, mapY);
    }
}
