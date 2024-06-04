package com.jd.st.data.storage.pyramid;

import com.jd.st.data.storage.feature.*;

/**
 * 电子地图的元信息
 *
 * @author wangrubin
 */
public class MapDescriptor {

    // 地图等级
    public final int zoomLevel;

    // 地图中的瓦片边长(单位：像素)
    public final int tileExtent;

    // 地图的边长(单位：像素)
    public final long mapExtent;

    // 地图分辨率(单位：米/像素)
    private final double resolution;

    public MapDescriptor(int zoomLevel, int tileExtent) {
        this.zoomLevel = zoomLevel;
        this.tileExtent = tileExtent;
        this.mapExtent = tileExtent * (1L << zoomLevel);
        this.resolution = MercatorConvertor.EXTENT / this.mapExtent;
    }

    /**
     * 地理坐标转地图像素坐标
     *
     * @param geodeticFeature 地理坐标系下的时空对象
     * @return 电子地图中的时空对象
     */
    public MapFeature convert2MapFeature(GeodeticFeature geodeticFeature) {
        return convert2MapFeature(geodeticFeature.toProjectFeature());
    }

    /**
     * 投影坐标转地图像素坐标
     *
     * @param projectFeature 投影坐标系下的时空对象
     * @return 电子地图中的时空对象
     */
    public MapFeature convert2MapFeature(ProjectFeature projectFeature) {
        // 投影坐标转地图的像素坐标
        long mapX = Math.round((projectFeature.projectX - MercatorConvertor.MIN_VALUE) / resolution);
        long mapY = Math.round((MercatorConvertor.MAX_VALUE - projectFeature.projectY) / resolution);
        return new MapFeature(projectFeature.oid, projectFeature.time, mapX, mapY);
    }

    /**
     * 地图像素坐标系转瓦片像素坐标系
     *
     * @param mapFeature 地图坐标系下的时空对象
     * @return 瓦片坐标系下的时空对象
     */
    public TileFeature convert2TileFeature(MapFeature mapFeature) {
        // 坐标所在地图瓦片行列号
        int columnNum = (int) (mapFeature.mapX / tileExtent);
        int rowNum = (int) (mapFeature.mapY / tileExtent);

        // 瓦片坐标系内的像素坐标
        long tileX = mapFeature.mapX - (long) columnNum * tileExtent;
        long tileY = mapFeature.mapY - (long) rowNum * tileExtent;

        TileLocation tileLocation = new TileLocation(this.zoomLevel, columnNum, rowNum);
        return new TileFeature(mapFeature.oid, mapFeature.time, tileX, tileY).setTileLocation(tileLocation);
    }
}
