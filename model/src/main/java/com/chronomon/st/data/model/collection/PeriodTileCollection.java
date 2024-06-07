package com.chronomon.st.data.model.collection;

import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.model.pyramid.TileMapLocation;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 时间片 + 瓦片分组的时空对象集合
 *
 * @author wangrubin
 */
public class PeriodTileCollection implements SerializableCollection {

    public final Instant periodStartTime;

    public final TileMapLocation tileLocation;

    private  List<MapFeature> featureList;

    public PeriodTileCollection(Instant periodStartTime,
                                TileMapLocation tileLocation,
                                List<MapFeature> featureList) {

        this.periodStartTime = periodStartTime;
        this.tileLocation = tileLocation;
        this.featureList = featureList;
    }

    public PeriodTileCollection(Instant periodStartTime,
                                TileMapLocation tileLocation,
                                byte[] featureBytes) {

        this.periodStartTime = periodStartTime;
        this.tileLocation = tileLocation;
        this.deserializeFeatures(featureBytes);
    }

    public List<MapFeature> getFeatureList() {
        return featureList;
    }

    @Override
    public byte[] serializeFeatures() {
        if (featureList.isEmpty()) {
            return new byte[0];
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);

        // 记录所有OID
        Map<String, Integer> oid2IndexMap = serializeOidList(output);

        // 记录集合大小
        output.writeVarInt(featureList.size(), true);

        // 瓦片最小坐标
        long tileStartMapX = tileLocation.tileStartMapX();
        long tileStartMapY = tileLocation.tileStartMapY();

        // 第一个对象记录原始值
        MapFeature firstFeature = featureList.get(0);
        output.writeVarInt(oid2IndexMap.get(firstFeature.oid), true);
        output.writeVarLong(firstFeature.time.getEpochSecond(), true);
        output.writeVarLong(firstFeature.mapX - tileStartMapX, true);
        output.writeVarLong(firstFeature.mapY - tileStartMapY, true);

        // 记录后一个对象与前一个对象的相对地图坐标和相对时间
        Instant preTime = firstFeature.time;
        for (int i = 1; i < featureList.size(); i++) {
            MapFeature feature = featureList.get(i);

            output.writeVarInt(oid2IndexMap.get(feature.oid), true);
            output.writeVarLong(feature.time.getEpochSecond() - preTime.getEpochSecond(), true);
            output.writeVarLong(feature.mapX - tileStartMapX, true);
            output.writeVarLong(feature.mapY - tileStartMapY, true);

            preTime = feature.time;
        }

        output.flush();
        return bos.toByteArray();
    }

    @Override
    public void deserializeFeatures(byte[] featureBytes) {
        if (featureBytes == null || featureBytes.length == 0) {
            return;
        }

        try (Input input = new Input(featureBytes)) {

            // 读取所有OID
            List<String> oidList = deserializeOidList(input);

            // 读取集合大小
            int featureSize = input.readVarInt(true);
            this.featureList = new ArrayList<>(featureSize);

            // 瓦片最小坐标
            long tileStartMapX = tileLocation.tileStartMapX();
            long tileStartMapY = tileLocation.tileStartMapY();

            // 读取第一个对象的原始值
            String oid = oidList.get(input.readVarInt(true));
            Instant time = Instant.ofEpochSecond(input.readVarLong(true));
            long mapX = tileStartMapX + input.readVarLong(true);
            long mapY = tileStartMapY + input.readVarLong(true);

            MapFeature firstFeature = new MapFeature(oid, time, mapX, mapY);
            featureList.add(firstFeature);

            // 读取并回复后续对象
            Instant preTime = firstFeature.time;
            for (int i = 1; i < featureSize; i++) {
                oid = oidList.get(input.readVarInt(true));
                time = preTime.plusSeconds(input.readVarLong(true));
                mapX = tileStartMapX + input.readVarLong(true);
                mapY = tileStartMapY + input.readVarLong(true);

                MapFeature feature = new MapFeature(oid, time, mapX, mapY);
                featureList.add(feature);
                preTime = feature.time;
            }
        }
    }

    private Map<String, Integer> serializeOidList(Output output) {
        //  记录所有OID
        Map<String, Integer> oid2IndexMap = new HashMap<>();
        for (MapFeature feature : featureList) {
            oid2IndexMap.putIfAbsent(feature.oid, oid2IndexMap.size());
        }
        List<String> oidList = oid2IndexMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        output.writeVarInt(oidList.size(), true);
        oidList.forEach(output::writeString);

        return oid2IndexMap;
    }

    private List<String> deserializeOidList(Input input) {
        // 读取所有OID
        int oidSize = input.readVarInt(true);
        List<String> oidList = new ArrayList<>(oidSize);
        while (oidSize-- > 0) {
            oidList.add(input.readString());
        }
        return oidList;
    }
}
