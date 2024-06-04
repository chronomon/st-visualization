package com.jd.st.data.storage.serialize;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.jd.st.data.storage.feature.TileFeature;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class TileFeatureSerializer implements IFeaturesSerializer<TileFeature> {

    @Override
    public byte[] serializeFeatures(List<TileFeature> features) {
        if (features.isEmpty()) {
            return new byte[0];
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);

        // 记录所有OID
        Map<String, Integer> oid2IndexMap = serializeOidList(output, features);

        // 记录集合大小
        output.writeVarInt(features.size(), true);

        // 第一个对象记录原始值
        TileFeature firstFeature = features.get(0);
        output.writeVarInt(oid2IndexMap.get(firstFeature.oid), true);
        output.writeVarLong(firstFeature.time.getEpochSecond(), true);
        output.writeVarLong(firstFeature.tileX, true);
        output.writeVarLong(firstFeature.tileY, true);

        // 记录后一个对象与前一个对象的相对地图坐标和相对时间
        Instant preTime = firstFeature.time;
        for (int i = 1; i < features.size(); i++) {
            TileFeature feature = features.get(i);

            output.writeVarInt(oid2IndexMap.get(feature.oid), true);
            output.writeVarLong(feature.time.getEpochSecond() - preTime.getEpochSecond(), true);
            output.writeVarLong(feature.tileX, true);
            output.writeVarLong(feature.tileX, true);

            preTime = feature.time;
        }

        output.flush();
        return bos.toByteArray();
    }

    @Override
    public List<TileFeature> deserializeFeatures(byte[] bytes) {
        List<TileFeature> features;
        if (bytes == null || bytes.length == 0) {
            return Collections.emptyList();
        }

        try (Input input = new Input(bytes)) {

            // 读取所有OID
            List<String> oidList = deserializeOidList(input);

            // 读取集合大小
            int featureSize = input.readVarInt(true);
            features = new ArrayList<>(featureSize);

            // 读取第一个对象的原始值
            String oid = oidList.get(input.readVarInt(true));
            Instant time = Instant.ofEpochSecond(input.readVarLong(true));
            long tileX = input.readVarLong(true);
            long tileY = input.readVarLong(true);

            TileFeature firstFeature = new TileFeature(oid, time, tileX, tileY);
            features.add(firstFeature);

            // 读取并回复后续对象
            Instant preTime = firstFeature.time;
            for (int i = 1; i < featureSize; i++) {
                oid = oidList.get(input.readVarInt(true));
                time = preTime.plusSeconds(input.readVarLong(true));
                tileX = input.readVarLong(true);
                tileY = input.readVarLong(true);

                TileFeature feature = new TileFeature(oid, time, tileX, tileY);
                features.add(feature);
                preTime = feature.time;
            }
        }

        return features;
    }

    protected Map<String, Integer> serializeOidList(Output output, List<TileFeature> features) {
        //  记录所有OID
        Map<String, Integer> oid2IndexMap = new HashMap<>();
        for (TileFeature feature : features) {
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

    protected List<String> deserializeOidList(Input input) {
        // 读取所有OID
        int oidSize = input.readVarInt(true);
        List<String> oidList = new ArrayList<>(oidSize);
        while (oidSize-- > 0) {
            oidList.add(input.readString());
        }
        return oidList;
    }
}
