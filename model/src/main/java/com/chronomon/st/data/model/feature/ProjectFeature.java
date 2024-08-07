package com.chronomon.st.data.model.feature;

import java.time.Instant;

/**
 * Mercator平面投影坐标系下的时空对象
 *
 * @author wangrubin
 */
public class ProjectFeature extends BaseFeature {

    // 横坐标(单位：米)
    public final double projectX;

    // 纵坐标(单位：米)
    public final double projectY;

    public ProjectFeature(String oid, Instant time, double projectX, double projectY) {
        super(oid, time);
        this.projectX = projectX;
        this.projectY = projectY;
    }

    public GeodeticFeature toGeodeticFeature() {
        return new GeodeticFeature(this.oid, this.time,
                MercatorConvertor.convert2Longitude(projectX),
                MercatorConvertor.convert2Latitude(projectY)
        );
    }
}
