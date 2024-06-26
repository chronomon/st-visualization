package com.chronomon.st.data.server.utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public class GeometryUtil {

    private static final GeometryFactory FACTORY = new GeometryFactory();

    public static Point createPoint(double lon, double lat) {
        return FACTORY.createPoint(new Coordinate(lon, lat));
    }

    public static LineString createLineString(Coordinate[] coordinates) {
        return FACTORY.createLineString(coordinates);
    }
}
