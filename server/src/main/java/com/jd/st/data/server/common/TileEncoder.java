package com.jd.st.data.server.common;

import com.jd.st.data.storage.hbase.HBaseAdaptor;
import com.jd.st.data.storage.model.TileFeature;
import no.ecc.vectortile.VectorTileEncoder;
import org.locationtech.jts.geom.*;

import java.util.HashMap;
import java.util.List;

public class TileEncoder {

    public static byte[] generateDataTile(List<TileFeature.DataFeature> pixelGeomList) {
        GeometryFactory factory = new GeometryFactory();
        int extent = HBaseAdaptor.pyramid.z2Index.tileExtent;
        FixedVectorTileEncoder encoder = new FixedVectorTileEncoder(extent, 0, false, true);
        for (TileFeature.DataFeature feature : pixelGeomList) {
            HashMap<String, Object> properties = new HashMap<>(1);
            properties.put("oid", feature.oid);
            properties.put("time", feature.epochSeconds);
            Point geom = factory.createPoint(new Coordinate(feature.pixelX, feature.pixelY));
            encoder.addFeature("data-layer", properties, geom);
        }
        /*Envelope tileEnv = new Envelope(0, extent, 0, extent);
        Geometry tileGeom = new GeometryFactory().toGeometry(tileEnv);
        encoder.addFeature("layer", new HashMap<>(), tileGeom, 1);*/
        return encoder.encode();
    }

    public static byte[] generatePatchTile(List<TileFeature.CountFeature> pixelGeomList) {
        int extent = HBaseAdaptor.pyramid.z2Index.tileExtent;
        FixedVectorTileEncoder encoder = new FixedVectorTileEncoder(extent, 0, false, true);
        for (TileFeature.CountFeature feature : pixelGeomList) {
            HashMap<String, Object> properties = new HashMap<>(1);
            properties.put("count", feature.count);
            encoder.addFeature("patch-layer", properties, feature.geom);
        }
       /* Envelope tileEnv = new Envelope(0, extent, 0, extent);
        Geometry tileGeom = new GeometryFactory().toGeometry(tileEnv);
        encoder.addFeature("layer", new HashMap<>(), tileGeom, 1);*/
        return encoder.encode();
    }

    private static class FixedVectorTileEncoder extends VectorTileEncoder {
        FixedVectorTileEncoder(int extent, int clipBuffer, boolean autoScale, boolean autoincrementIds) {
            super(extent, clipBuffer, autoScale, autoincrementIds, -1.0);
        }

        protected Geometry clipGeometry(Geometry geometry) {
            // point do not need clip
            return geometry;
        }
    }
}
