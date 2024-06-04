package com.jd.st.data.storage.pyramid;

/**
 * 瓦片金字塔模型
 *
 * @author wangrubin
 */
public class PyramidModel {

    // 最大地图等级
    public final int maxZoomLevel;

    // 地图瓦片边长(单位：像素)
    public final int tileExtent;

    // 最底层地图的元信息
    private final MapDescriptor bottomMapDescriptor;

    public PyramidModel(int maxZoomLevel, int tileExtent) {
        this.maxZoomLevel = maxZoomLevel;
        this.tileExtent = tileExtent;
        this.bottomMapDescriptor = new MapDescriptor(maxZoomLevel, tileExtent);
    }

    public MapDescriptor getBottomMapDescriptor() {
        return bottomMapDescriptor;
    }

    @Override
    public String toString() {
        return maxZoomLevel + "-" + tileExtent;
    }
}
