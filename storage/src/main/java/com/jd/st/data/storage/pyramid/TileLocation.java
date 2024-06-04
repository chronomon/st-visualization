package com.jd.st.data.storage.pyramid;

/**
 * 地图瓦片在金字塔模型中的位置(瓦片坐标)
 *
 * @author wangrubin
 */
public class TileLocation {

    // 瓦片行号(对应地图纵坐标)
    public final int rowNum;

    // 瓦片列号(对应地图横坐标)
    public final int columnNum;

    // 瓦片所在地图等级
    public final int zoomLevel;

    public TileLocation(int zoomLevel, int columnNum, int rowNum) {
        this.rowNum = rowNum;
        this.columnNum = columnNum;
        this.zoomLevel = zoomLevel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TileLocation) {
            TileLocation other = (TileLocation) obj;
            return zoomLevel == other.zoomLevel && columnNum == other.columnNum && rowNum == other.rowNum;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s-%s-%s", zoomLevel, columnNum, rowNum);
    }

    /**
     * 将二维的行列号编码成一维的Z空间填充曲线
     *
     * @param columnNum 列号
     * @param rowNum    行号
     * @return Z填充曲线编码值
     */
    public static long zCurveEncode(int columnNum, int rowNum) {
        return split(columnNum) | split(rowNum) << 1;
    }

    /**
     * 将一维的Z空间填充曲线值解码成二维的行列号
     *
     * @param zVal Z填充曲线编码值
     * @return 列好与行号
     */
    public static int[] zCurveDecode(long zVal) {
        int[] columnAndRowNum = new int[2];
        columnAndRowNum[0] = combine(zVal);
        columnAndRowNum[1] = combine(zVal >> 1);
        return columnAndRowNum;
    }

    private static final long MaxMask = 0x7fffffffL;

    private static long split(long value) {
        long x = value & MaxMask;
        x = (x ^ (x << 32)) & 0x00000000ffffffffL;
        x = (x ^ (x << 16)) & 0x0000ffff0000ffffL;
        x = (x ^ (x << 8)) & 0x00ff00ff00ff00ffL;
        x = (x ^ (x << 4)) & 0x0f0f0f0f0f0f0f0fL;
        x = (x ^ (x << 2)) & 0x3333333333333333L;
        x = (x ^ (x << 1)) & 0x5555555555555555L;
        return x;
    }

    private static int combine(long value) {
        long x = value & 0x5555555555555555L;
        x = (x ^ (x >> 1)) & 0x3333333333333333L;
        x = (x ^ (x >> 2)) & 0x0f0f0f0f0f0f0f0fL;
        x = (x ^ (x >> 4)) & 0x00ff00ff00ff00ffL;
        x = (x ^ (x >> 8)) & 0x0000ffff0000ffffL;
        x = (x ^ (x >> 16)) & 0x00000000ffffffffL;
        return (int) x;
    }
}
