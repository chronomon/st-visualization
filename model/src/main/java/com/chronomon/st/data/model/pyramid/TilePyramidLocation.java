package com.chronomon.st.data.model.pyramid;

/**
 * 瓦片在金字塔模型中的位置
 *
 * @author wangrubin
 */
public class TilePyramidLocation extends TileMapLocation {

    /**
     * 瓦片所在金字塔中的地图等级
     */
    public final int zoomLevel;

    public TilePyramidLocation(int zoomLevel, int columnNum, int rowNum) {
        super(columnNum, rowNum, -1);
        this.zoomLevel = zoomLevel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TilePyramidLocation) {
            TilePyramidLocation other = (TilePyramidLocation) obj;
            return zoomLevel == other.zoomLevel && super.equals(obj);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s-%s-%s", zoomLevel, columnNum, rowNum);
    }
}
