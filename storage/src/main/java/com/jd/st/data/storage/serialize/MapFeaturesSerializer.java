package com.jd.st.data.storage.serialize;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.jd.st.data.storage.feature.MapFeature;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.*;

public class MapFeaturesSerializer implements IFeaturesSerializer<MapFeature> {

    private final String oid;

    public MapFeaturesSerializer(String oid) {
        this.oid = oid;
    }

    @Override
    public byte[] serializeFeatures(List<MapFeature> features) {
        if (features.isEmpty()) {
            return new byte[0];
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);

        // 记录集合大小
        output.writeVarInt(features.size(), true);

        // 第一个对象记录原始值，mapX和mapY会很大，所以无需开启优化
        MapFeature firstFeature = features.get(0);
        output.writeVarLong(firstFeature.time.getEpochSecond(), true);
        output.writeVarLong(firstFeature.mapX, false);
        output.writeVarLong(firstFeature.mapY, false);

        // 记录后一个对象与前一个对象的相对地图坐标和相对时间
        MapFeature preFeature = firstFeature;
        for (int i = 1; i < features.size(); i++) {
            MapFeature feature = features.get(i);

            long timeInterval = feature.time.getEpochSecond() - preFeature.time.getEpochSecond();
            long mapXInterval = feature.mapX - preFeature.mapX;
            long mapYInterval = feature.mapY - preFeature.mapY;

            output.writeVarLong(timeInterval, true);
            output.writeVarLong(mapXInterval, true);
            output.writeVarLong(mapYInterval, true);

            preFeature = feature;
        }

        output.flush();
        return bos.toByteArray();
    }

    @Override
    public List<MapFeature> deserializeFeatures(byte[] bytes) {
        List<MapFeature> features;
        if (bytes == null || bytes.length == 0) {
            return Collections.emptyList();
        }

        try (Input input = new Input(bytes)) {

            // 读取集合大小
            int featureSize = input.readVarInt(true);
            features = new ArrayList<>(featureSize);

            // 读取第一个对象的原始值
            Instant time = Instant.ofEpochSecond(input.readVarLong(true));
            long mapX = input.readVarLong(false);
            long mapY = input.readVarLong(false);
            MapFeature firstFeature = new MapFeature(oid, time, mapX, mapY);
            features.add(firstFeature);

            // 读取并回复后续对象
            MapFeature preFeature = firstFeature;
            for (int i = 1; i < featureSize; i++) {

                long timeInterval = input.readVarLong(true);
                long mapXInterval = input.readVarLong(true);
                long mapYInterval = input.readVarLong(true);

                MapFeature feature = new MapFeature(
                        oid,
                        preFeature.time.plusSeconds(timeInterval),
                        preFeature.mapX + mapXInterval,
                        preFeature.mapY + mapYInterval
                );
                features.add(feature);
                preFeature = feature;
            }
        }

        return features;
    }
}
