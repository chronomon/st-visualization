package com.jd.st.data.storage.pyramid;

import com.jd.st.data.storage.collection.FeatureCollectionOfOid;
import com.jd.st.data.storage.collection.FeatureCollectionOfTile;
import com.jd.st.data.storage.feature.GeodeticFeature;
import com.jd.st.data.storage.feature.MapFeature;
import com.jd.st.data.storage.feature.TileFeature;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MapDescriptorTest {

    @Test
    public void testPyramidModel() throws URISyntaxException, IOException, ParseException {
        PyramidModel pyramidModel = new PyramidModel(18, 256);
        MapDescriptor mapDescriptor = pyramidModel.getBottomMapDescriptor();

        URL fileURL = this.getClass().getClassLoader().getResource("gps.txt");
        List<String> lines = Files.readAllLines(Paths.get(fileURL.toURI()));

        String oid = "oid";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<MapFeature> mapFeatureList = new ArrayList<>(lines.size());
        List<TileFeature> tileFeatureList = new ArrayList<>(lines.size());
        for (String line : lines) {
            String[] attrs = line.split("\t");
            double lon = Double.parseDouble(attrs[0]);
            double lat = Double.parseDouble(attrs[1]);
            Instant time = format.parse(attrs[2]).toInstant();

            MapFeature mapFeature = mapDescriptor.convert2MapFeature(new GeodeticFeature(oid, time, lon, lat));
            mapFeatureList.add(mapFeature);
            TileFeature tileFeature = mapDescriptor.convert2TileFeature(mapFeature);
            tileFeatureList.add(tileFeature);
        }

        // 先按照oid和tile分组，然后再按照period分片
        List<FeatureCollectionOfOid> collectionOfOidList = new FeatureCollectionOfOid(oid, mapFeatureList).groupByPeriod(ChronoUnit.HOURS, true);
        List<FeatureCollectionOfTile> collectionOfTileList = new ArrayList<>();
        Map<TileLocation, List<TileFeature>> tileLocation2FeaturesMap = tileFeatureList.stream()
                .collect(Collectors.groupingBy(TileFeature::getTileLocation));
        for (Map.Entry<TileLocation, List<TileFeature>> tileLocationAndFeatures : tileLocation2FeaturesMap.entrySet()) {
            FeatureCollectionOfTile collectionOfTile = new FeatureCollectionOfTile(tileLocationAndFeatures.getKey(), tileLocationAndFeatures.getValue());
            collectionOfTileList.addAll(collectionOfTile.groupByPeriod(ChronoUnit.HOURS, true));
        }

        // 测试反序列化
        testFeatureOfOidSerialize(collectionOfOidList.get(0));
        testFeatureOfTileSerialize(collectionOfTileList.get(0));

        // 序列化成字节数组，并输出字节数组长度
        int oidByteSize = collectionOfOidList.stream()
                .map(collection -> collection.featuresAsBytes().length)
                .reduce(Integer::sum).orElse(0);
        int tileByteSize = collectionOfTileList.stream()
                .map(collection -> collection.featuresAsBytes().length)
                .reduce(Integer::sum).orElse(0);

        System.out.println("OID Byte Length:" + oidByteSize);
        System.out.println("Tile Byte Length:" + tileByteSize);
    }

    private void testFeatureOfTileSerialize(FeatureCollectionOfTile collectionOfTile) {
        new FeatureCollectionOfTile(collectionOfTile.tileLocation, collectionOfTile.featuresAsBytes());
    }

    private void testFeatureOfOidSerialize(FeatureCollectionOfOid collectionOfOid) {
        new FeatureCollectionOfOid(collectionOfOid.oid, collectionOfOid.featuresAsBytes());
    }
}