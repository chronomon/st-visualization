package com.jd.st.data.storage.feature;

import java.time.Instant;

/**
 * 金字塔电子地图中的时空对象
 *
 * @author wangrubin
 */
public class MapFeature extends BaseFeature {

    // 横坐标(单位：像素)
    public final long mapX;

    // 纵坐标(单位：像素)
    public final long mapY;

    public MapFeature(String oid, Instant time, long mapX, long mapY) {
        super(oid, time);
        this.mapX = mapX;
        this.mapY = mapY;
    }
}
