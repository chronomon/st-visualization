package com.chronomon.st.data.model.feature;

/**
 * 金字塔电子地图中的时空对象
 *
 * @author wangrubin
 */
public class PatchFeature {

    public long count;

    // 横坐标(单位：像素)
    public long minMapX;

    // 纵坐标(单位：像素)
    public long minMapY;

    // 横坐标(单位：像素)
    public long maxMapX;

    // 纵坐标(单位：像素)
    public long maxMapY;

    public PatchFeature(long count) {
        this.count = count;
    }

}
