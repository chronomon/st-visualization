package com.chronomon.st.data.model.feature;

/**
 * 球面坐标(WGS84)转平面坐标(Mercator)的转换器
 *
 * @author wangrubin
 */
public class MercatorConvertor {
    private static final double ORIGIN_SHIFT = 2 * Math.PI * 6378137 / 2.0;

    private static final double MIN_LAT = -85.05112877980659;
    private static final double MAX_LAT = 85.05112877980659;

    public static final double MIN_VALUE = -20037508.3427892;

    public static final double MAX_VALUE = 20037508.3427892;

    public static final double EXTENT = MAX_VALUE - MIN_VALUE;

    /**
     * 地理经度转投影坐标系下的横坐标
     *
     * @param lon 经度
     * @return 投影坐标系下的横坐标(单位米)
     */
    public static double convert2ProjectX(double lon) {
        return lon * ORIGIN_SHIFT / 180.0;
    }

    /**
     * 投影坐标系下的横坐标转地理经度
     *
     * @param projectX 投影坐标系下的横坐标(单位米)
     * @return 经度
     */
    public static double convert2Longitude(double projectX) {
        return projectX * 180.0 / ORIGIN_SHIFT;
    }

    /**
     * 地理纬度转投影坐标系下的纵坐标
     *
     * @param lat 纬度
     * @return 投影坐标系下的纵坐标(单位米)
     */
    public static double convert2ProjectY(double lat) {
        if (lat < MIN_LAT) {
            return MIN_VALUE;
        } else if (lat > MAX_LAT) {
            return MAX_VALUE;
        }
        double projectLat = Math.log(Math.tan((90 + lat) * Math.PI / 360.0)) / (Math.PI / 180.0);
        return projectLat * ORIGIN_SHIFT / 180;
    }

    /**
     * 投影坐标系下的纵坐标转地理纬度
     *
     * @param projectY 投影坐标系下的纵坐标(单位米)
     * @return 纬度
     */
    public static double convert2Latitude(double projectY) {
        double projectLat = projectY * 180.0 / ORIGIN_SHIFT;
        return 180 / Math.PI * (2 * Math.atan(Math.exp(projectLat * Math.PI / 180)) - Math.PI / 2);
    }
}
