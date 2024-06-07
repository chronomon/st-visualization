package com.chronomon.st.data.model.feature;

import java.time.Instant;

/**
 * WGS84球面坐标系下的时空对象
 *
 * @author wangrubin
 */
public class GeodeticFeature extends BaseFeature {
    // 经度
    public final double lon;

    // 纬度
    public final double lat;

    public GeodeticFeature(String oid, Instant time, double lon, double lat) {
        super(oid, time);
        this.lon = lon;
        this.lat = lat;
    }

    /**
     * 地理坐标转投影坐标
     *
     * @return 投影坐标系下的时空对象
     */
    public ProjectFeature toProjectFeature() {
        return new ProjectFeature(
                this.oid,
                this.time,
                MercatorConvertor.convert2ProjectX(lon),
                MercatorConvertor.convert2ProjectY(lat)
        );
    }
}
