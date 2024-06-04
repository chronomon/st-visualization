package com.jd.st.data.storage.feature;

/**
 * 球面坐标转Mercator平面坐标的转换器
 *
 * @author wangrubin
 */
public class MercatorConvertor {
    private static final double ORIGIN_SHIFT = 2 * Math.PI * 6378137 / 2.0;

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
     * 地理纬度转投影坐标系下的纵坐标
     *
     * @param lat 纬度
     * @return 投影坐标系下的纵坐标(单位米)
     */
    public static double convert2ProjectY(double lat) {
        double projectLat = Math.log(Math.tan((90 + lat) * Math.PI / 360.0)) / (Math.PI / 180.0);
        return projectLat * ORIGIN_SHIFT / 180;
    }
}
