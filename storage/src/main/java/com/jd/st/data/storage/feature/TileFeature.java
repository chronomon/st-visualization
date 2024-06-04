package com.jd.st.data.storage.feature;

import com.jd.st.data.storage.pyramid.TileLocation;

import java.time.Instant;

/**
 * 地图瓦片中的时空对象
 *
 * @author wangrubin
 */
public class TileFeature extends BaseFeature {

    // 横坐标(单位：像素)
    public final long tileX;

    // 纵坐标(单位：像素)
    public final long tileY;

    // 瓦片位置
    private TileLocation tileLocation = null;

    public TileFeature(String oid, Instant time, long tileX, long tileY) {
        super(oid, time);
        this.tileX = tileX;
        this.tileY = tileY;
        this.tileLocation = null;
    }

    public TileLocation getTileLocation() {
        return tileLocation;
    }

    public TileFeature setTileLocation(TileLocation tileLocation) {
        this.tileLocation = tileLocation;
        return this;
    }
}
