package com.chronomon.st.data.server.utils;

import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.model.feature.PatchFeature;
import com.chronomon.st.data.model.pyramid.TileMapLocation;
import no.ecc.vectortile.VectorTileEncoder;
import org.locationtech.jts.geom.*;

import java.util.HashMap;
import java.util.List;

public class TileEncoder {

    public static byte[] generateDataTile(List<MapFeature> mapFeatureList, TileMapLocation tileLocation) {
        GeometryFactory factory = new GeometryFactory();
        FixedVectorTileEncoder encoder = new FixedVectorTileEncoder(tileLocation.tileExtent, 0, false, true);
        long tileStartMapX = tileLocation.tileStartMapX();
        long tileStartMapY = tileLocation.tileStartMapY();
        for (MapFeature feature : mapFeatureList) {
            HashMap<String, Object> properties = new HashMap<>(1);
            properties.put("oid", feature.oid);
            properties.put("time", feature.time.getEpochSecond());
            Point geom = factory.createPoint(new Coordinate(feature.mapX - tileStartMapX, feature.mapY - tileStartMapY));
            encoder.addFeature("data", properties, geom);
        }
        return encoder.encode();
    }

    public static byte[] generatePatchTile(List<PatchFeature> pixelGeomList, TileMapLocation tileLocation) {
        GeometryFactory factory = new GeometryFactory();
        FixedVectorTileEncoder encoder = new FixedVectorTileEncoder(tileLocation.tileExtent, 0, false, true);
        long tileStartMapX = tileLocation.tileStartMapX();
        long tileStartMapY = tileLocation.tileStartMapY();
        for (PatchFeature feature : pixelGeomList) {
            HashMap<String, Object> properties = new HashMap<>(1);
            properties.put("count", feature.count);
            Geometry patchGeom = factory.toGeometry(new Envelope(
                    feature.minMapX - tileStartMapX,
                    feature.maxMapX - tileStartMapX,
                    feature.minMapY - tileStartMapY,
                    feature.maxMapY - tileStartMapY
            ));
            encoder.addFeature("patch", properties, patchGeom);
        }
        return encoder.encode();
    }

    private static class FixedVectorTileEncoder extends VectorTileEncoder {
        FixedVectorTileEncoder(int extent, int clipBuffer, boolean autoScale, boolean autoincrementIds) {
            super(extent, clipBuffer, autoScale, autoincrementIds);
        }

        protected Geometry clipGeometry(Geometry geometry) {
            // point do not need clip
            return geometry;
        }
    }
}
