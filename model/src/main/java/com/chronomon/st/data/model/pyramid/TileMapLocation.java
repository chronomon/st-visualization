package com.chronomon.st.data.model.pyramid;

/**
 * 瓦片在地图中的位置
 *
 * @author wangrubin
 */
public class TileMapLocation {

    /**
     * 瓦片行号(对应地图纵坐标)
     */
    public final int rowNum;

    /**
     * 瓦片列号(对应地图横坐标)
     */
    public final int columnNum;

    /**
     * 瓦片边长(单位：像素)
     */
    public final int tileExtent;

    public TileMapLocation(int columnNum, int rowNum, int tileExtent) {
        this.rowNum = rowNum;
        this.columnNum = columnNum;
        this.tileExtent = tileExtent;
    }

    public TileMapLocation(long zVal, int tileExtent) {
        int[] colRow = zCurveDecode(zVal);
        this.columnNum = colRow[0];
        this.rowNum = colRow[1];
        this.tileExtent = tileExtent;
    }

    public long tileStartMapX() {
        return (long) tileExtent * columnNum;
    }

    public long tileStartMapY() {
        return (long) tileExtent * rowNum;
    }

    /**
     * 获取瓦片的Z空间填充曲线编码
     *
     * @return 编码值
     */
    public long zCurveCode() {
        return zCurveEncode(columnNum, rowNum);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TileMapLocation) {
            TileMapLocation other = (TileMapLocation) obj;
            return columnNum == other.columnNum && rowNum == other.rowNum;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s-%s", columnNum, rowNum);
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
