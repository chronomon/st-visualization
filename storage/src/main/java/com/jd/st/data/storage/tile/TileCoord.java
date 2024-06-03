package com.jd.st.data.storage.tile;

import com.jd.st.data.storage.index.PixelZ2Index;
import com.jd.st.data.storage.index.Z2Range;

public class TileCoord {

    public final int rowNum;

    public final int columnNum;

    public final int zoomLevel;

    public TileCoord(int rowNum, int columnNum, int zoomLevel) {
        this.rowNum = rowNum;
        this.columnNum = columnNum;
        this.zoomLevel = zoomLevel;
    }

    public Z2Range getDataZ2Range(int targetZoomLevel) {
        assert targetZoomLevel >= this.zoomLevel : "zoom level is illegal";
        long scale = (long) Math.pow(4, targetZoomLevel - this.zoomLevel);
        long minZ2 = getZ2() * scale;
        long maxZ2 = minZ2 + scale - 1;
        return new Z2Range(minZ2, maxZ2);
    }

    public long getZ2() {
        return PixelZ2Index.encodeTile(columnNum, rowNum);
    }

    public String toString() {
        return String.format("%s-%s-%s", zoomLevel, rowNum, columnNum);
    }
}
