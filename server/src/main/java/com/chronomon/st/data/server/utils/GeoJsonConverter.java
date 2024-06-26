package com.chronomon.st.data.server.utils;

import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;
import java.io.StringWriter;

/**
 * GeoJSON转换器
 */
public class GeoJsonConverter {

    /**
     * 将Geometry转成JSON字符串
     *
     * @param geometry 空间对象
     * @return JSON字符串
     * @throws IOException
     */
    public static String toGeoJSON(Geometry geometry) throws IOException {
        StringWriter writer = new StringWriter();
        GeometryJSON g = new GeometryJSON();
        g.write(geometry, writer);
        return writer.toString();
    }

    /**
     * 将SimpleFeature转成JSON字符串
     *
     * @param feature 空间要素(包括空间属性和非空间属性)
     * @return JSON字符串
     * @throws IOException
     */
    public static String toFeatureJSON(SimpleFeature feature) throws IOException {
        StringWriter writer = new StringWriter();
        FeatureJSON featureJSON = new FeatureJSON();
        featureJSON.writeFeature(feature, writer);
        return writer.toString();
    }
}