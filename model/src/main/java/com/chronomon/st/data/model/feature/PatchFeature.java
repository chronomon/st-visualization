package com.chronomon.st.data.model.feature;

/**
 * 地图瓦片中的像素块对象
 *
 * @author wangrubin
 */
public class PatchFeature {

    // 包含的位置数据总数
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
