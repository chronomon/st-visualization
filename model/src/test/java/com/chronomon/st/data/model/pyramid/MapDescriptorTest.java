package com.chronomon.st.data.model.pyramid;

import com.chronomon.st.data.model.feature.GeodeticFeature;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.model.collection.PeriodCollection;
import com.chronomon.st.data.model.collection.PeriodOidCollection;
import com.chronomon.st.data.model.collection.PeriodTileCollection;
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
import java.util.stream.Collectors;

public class MapDescriptorTest {

    @Test
    public void testPyramidModel() throws URISyntaxException, IOException, ParseException {
        PyramidModel pyramidModel = new PyramidModel(20, 256);
        MapDescriptor mapDescriptor = pyramidModel.getBottomMapDescriptor();

        URL fileURL = this.getClass().getClassLoader().getResource("gps.txt");
        List<String> lines = Files.readAllLines(Paths.get(fileURL.toURI()));

        String oid = "oid";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<MapFeature> mapFeatureList = new ArrayList<>(lines.size());
        for (String line : lines) {
            String[] attrs = line.split("\t");
            double lon = Double.parseDouble(attrs[0]);
            double lat = Double.parseDouble(attrs[1]);
            Instant time = format.parse(attrs[2]).toInstant();

            MapFeature mapFeature = mapDescriptor.convert2MapFeature(new GeodeticFeature(oid, time, lon, lat));
            mapFeatureList.add(mapFeature);
        }

        // 先根据时间片分组
        List<PeriodCollection> periodCollectionList = PeriodCollection.groupByPeriod(mapFeatureList, ChronoUnit.HOURS);
        List<PeriodOidCollection> periodOidCollectionList = periodCollectionList.stream()
                .flatMap(periodCollection -> periodCollection.groupByOid().stream())
                .collect(Collectors.toList());
        List<PeriodTileCollection> periodTileCollectionList = periodCollectionList.stream()
                .flatMap(periodCollection -> periodCollection.groupByTile(mapDescriptor).stream())
                .collect(Collectors.toList());

        // 测试反序列化
        testFeatureOfOidSerialize(periodOidCollectionList.get(0));
        testFeatureOfTileSerialize(periodTileCollectionList.get(0));

        // 序列化成字节数组，并输出字节数组长度
        int oidByteSize = periodOidCollectionList.stream()
                .map(collection -> collection.serializeFeatures().length)
                .reduce(Integer::sum).orElse(0);
        int tileByteSize = periodTileCollectionList.stream()
                .map(collection -> collection.serializeFeatures().length)
                .reduce(Integer::sum).orElse(0);

        System.out.println("OID Byte Length:" + oidByteSize);
        System.out.println("Tile Byte Length:" + tileByteSize);
    }

    private void testFeatureOfTileSerialize(PeriodTileCollection periodTileCollection) {
        new PeriodTileCollection(periodTileCollection.periodStartTime, periodTileCollection.tileLocation, periodTileCollection.serializeFeatures());
    }

    private void testFeatureOfOidSerialize(PeriodOidCollection periodOidCollection) {
        new PeriodOidCollection(periodOidCollection.periodStartTime, periodOidCollection.oid, periodOidCollection.serializeFeatures());
    }
}
