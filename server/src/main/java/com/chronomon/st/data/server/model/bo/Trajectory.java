package com.chronomon.st.data.server.model.bo;

import com.chronomon.st.data.model.feature.GeodeticFeature;
import com.chronomon.st.data.server.utils.GeoJsonConverter;
import com.chronomon.st.data.server.utils.GeometryUtil;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.Comparator;
import java.util.List;

/**
 * 轨迹对象
 */
public class Trajectory {

    private final String oid;

    private final List<GeodeticFeature> gpsList;

    private static final SimpleFeatureType SFT;

    static {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("trajectory");
        builder.setCRS(DefaultGeographicCRS.WGS84);
        builder.add("oid", String.class);
        builder.add("geom", LineString.class);
        SFT = builder.buildFeatureType();
    }

    public Trajectory(String oid, List<GeodeticFeature> gpsList, boolean ordered) {
        this.oid = oid;
        this.gpsList = gpsList;
        if (!ordered) {
            // 如果是无序的，需要排序
            gpsList.sort(Comparator.comparing(o -> o.time));
        }
    }

    public int size(){
        return gpsList.size();
    }

    public String toText(){
        Coordinate[] coordinates = gpsList.stream()
                .map(mapFeature -> new CoordinateXYM(mapFeature.lon, mapFeature.lat, mapFeature.time.getEpochSecond()))
                .toArray(Coordinate[]::new);
        return GeometryUtil.createLineString(coordinates).toText();
    }

    public String toJson() {
        Coordinate[] coordinates = gpsList.stream()
                .map(mapFeature -> new CoordinateXYM(mapFeature.lon, mapFeature.lat, mapFeature.time.getEpochSecond()))
                .toArray(Coordinate[]::new);
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(SFT);
        builder.add(oid);
        builder.add(GeometryUtil.createLineString(coordinates));
        SimpleFeature feature = builder.buildFeature(oid);

        try {
            return GeoJsonConverter.toFeatureJSON(feature);
        } catch (Exception ignore) {
            return "";
        }
    }
}
