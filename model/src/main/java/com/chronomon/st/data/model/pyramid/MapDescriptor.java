package com.chronomon.st.data.model.pyramid;

import com.chronomon.st.data.model.feature.GeodeticFeature;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.model.feature.MercatorConvertor;
import com.chronomon.st.data.model.feature.ProjectFeature;

/**
 * 电子地图的元信息
 *
 * @author wangrubin
 */
public class MapDescriptor {

    /**
     * 地图等级
     */
    public final int zoomLevel;

    /**
     * 地图中的瓦片边长(单位：像素)
     */
    public final int tileExtent;

    /**
     * 地图的边长(单位：像素)
     */
    public final long mapExtent;

    /**
     * 地图分辨率(单位：米/像素)
     */
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
     * 地图像素坐标转投影坐标
     *
     * @param mapFeature 电子地图中的时空对象
     * @return 投影坐标系下的时空对象
     */
    public ProjectFeature convert2ProjectFeature(MapFeature mapFeature) {
        // 投影坐标转地图的像素坐标
        double projectX = mapFeature.mapX * resolution + MercatorConvertor.MIN_VALUE;
        double projectY = MercatorConvertor.MAX_VALUE - mapFeature.mapY * resolution;
        return new ProjectFeature(mapFeature.oid, mapFeature.time, projectX, projectY);
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
     * 将时空对象落位到对应的地图瓦片
     *
     * @param mapFeature 时空对象
     * @return 地图瓦片位置
     */
    public TileMapLocation locate2Tile(MapFeature mapFeature) {
        // 坐标所在地图瓦片行列号
        int columnNum = (int) (mapFeature.mapX / tileExtent);
        int rowNum = (int) (mapFeature.mapY / tileExtent);

        return new TileMapLocation(columnNum, rowNum, tileExtent);
    }
}
