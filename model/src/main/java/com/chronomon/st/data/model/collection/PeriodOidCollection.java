package com.chronomon.st.data.model.collection;

import com.chronomon.st.data.model.feature.MapFeature;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 时间片 + oid分组的时空对象集合
 *
 * @author wangrubin
 */
public class PeriodOidCollection implements SerializableCollection {

    public final Instant periodStartTime;


    public final String oid;

    private List<MapFeature> featureList;

    public PeriodOidCollection(Instant periodStartTime, String oid, List<MapFeature> featureList) {
        this.periodStartTime = periodStartTime;
        this.oid = oid;
        this.featureList = featureList;
    }

    public PeriodOidCollection(Instant periodStartTime, String oid, byte[] featureBytes) {
        this.periodStartTime = periodStartTime;
        this.oid = oid;
        this.deserializeFeatures(featureBytes);
    }

    public int size() {
        if (featureList == null) {
            return 0;
        } else {
            return featureList.size();
        }
    }

    @Override
    public byte[] serializeFeatures() {
        if (featureList.isEmpty()) {
            return new byte[0];
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);

        // 记录集合大小
        output.writeVarInt(featureList.size(), true);

        // 第一个对象记录原始值，mapX和mapY会很大，所以无需开启优化
        MapFeature firstFeature = featureList.get(0);
        output.writeVarLong(firstFeature.time.getEpochSecond(), true);
        output.writeVarLong(firstFeature.mapX, false);
        output.writeVarLong(firstFeature.mapY, false);

        // 记录后一个对象与前一个对象的相对地图坐标和相对时间
        MapFeature preFeature = firstFeature;
        for (int i = 1; i < featureList.size(); i++) {
            MapFeature feature = featureList.get(i);

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
    public void deserializeFeatures(byte[] featureBytes) {
        if (featureBytes == null || featureBytes.length == 0) {
            return;
        }

        try (Input input = new Input(featureBytes)) {

            // 读取集合大小
            int featureSize = input.readVarInt(true);
            featureList = new ArrayList<>(featureSize);

            // 读取第一个对象的原始值
            Instant time = Instant.ofEpochSecond(input.readVarLong(true));
            long mapX = input.readVarLong(false);
            long mapY = input.readVarLong(false);
            MapFeature firstFeature = new MapFeature(oid, time, mapX, mapY);
            featureList.add(firstFeature);

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
                featureList.add(feature);
                preFeature = feature;
            }
        }
    }
}
